package optim.optim.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.UnboundedSolutionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import optim.optim.controller.body.AdvancedOptions;
import optim.optim.controller.body.SimplexForm;
import optim.optim.response.SimplexResponse;
import optim.optim.response.SimplexResponse.Feasibility;
import optim.optim.src.Config;
import optim.optim.src.log.Logger;
import optim.optim.src.simplex.Fraction;
import optim.optim.src.simplex.PointFractionPair;
import optim.optim.src.simplex.SimplexSolver;
import optim.optim.src.simplex.data.Constraint;
import optim.optim.src.simplex.data.ConstraintSet;
import optim.optim.src.simplex.data.GoalType;
import optim.optim.src.simplex.data.IntegerMethod;
import optim.optim.src.simplex.data.NonNegativeValues;
import optim.optim.src.simplex.data.ObjectiveFunction;
import optim.optim.src.simplex.data.PivotSelectionRule;

/**
 * Service managing the simplex request between the controller and the logic.
 */
@Service
public class SimplexService {
    /** Default constructor. */
    public SimplexService() {
    }

    /**
     * Solve the simplex request, or return an error if it is malformated or non
     * logical.
     *
     * @param form The simplex request form.
     * @return A simplex response of the problem.
     */
    public SimplexResponse solve(SimplexForm form) {
        SimplexResponse res = new SimplexResponse();

        if (form == null) {
            return res.setStatus(HttpStatus.NOT_ACCEPTABLE, "no data received");
        }

        Logger.trace("m constr.:  " + form.m);
        Logger.trace("n coeffs:   " + form.n);
        Logger.trace("basic:      " + Arrays.toString(form.tight));
        Logger.trace("nonbasic:   " + Arrays.toString(form.loose));
        Logger.trace("const:      " + Arrays.toString(form.constants));
        Logger.trace("objective:  " + Arrays.toString(form.objective));
        Logger.trace("coefs: " + TwoDArrayToString(form.coefs));
        Logger.trace("toMaximise: " + form.toMaximise);
        Logger.trace("toInteger:  " + form.toInteger);

        Logger.trace("Advanced:");
        Logger.trace("\tmaxIterations:  " + form.advanced.maxIterations);
        Logger.trace("\tpivot rule:     " + form.advanced.pivotSelectionRule);
        Logger.trace("\tinteger method: " + form.advanced.integerMethod);

        // check values are valid (a JSON object is returned as status)
        if (form.m < 1) {
            return res.setStatus("{\"m\":{\"min\":1,\"received\":" + form.m + "}}");
        }
        if (form.n < 1) {
            return res.setStatus("{\"n\":{\"min\":1,\"received\":" + form.n + "}}");
        }
        if (form.m > Config.Simplex_MaxConstraints()) {
            return res.setStatus(
                    "{\"n\":{\"max\":" + Config.Simplex_MaxConstraints() + ",\"received\":" + form.n + "}}");
        }
        if (form.n > Config.Simplex_MaxCoefficients()) {
            return res.setStatus(
                    "{\"n\":{\"max\":" + Config.Simplex_MaxCoefficients() + ",\"received\":" + form.n + "}}");
        }
        // we don't really care about it for the moment, since they give what value to
        // map to the index for the user to display
        // TODO well we care, they give which variables are basic at the start
        // if (form.tight.length < form.m) {
        // return res.setStatus("{\"basic\":{\"expected\":" + form.m +
        // ",\"received\":" + form.tight.length + "}}");
        // }
        // if (form.loose.length < form.n) {
        // return res.setStatus("{\"nonbasic\":{\"expected\":" + form.m +
        // ",\"received\":" + form.loose.length + "}}");
        // }
        if (form.constants == null || form.constants.length < form.m) {
            return res.setStatus("{\"constants\":{\"expected\":" + form.m +
                    ",\"received\":" + form.constants.length + "}}");
        }
        if (form.relationships == null || form.relationships.length < form.m) {
            return res.setStatus("{\"relationships\":{\"expected\":" + form.m +
                    ",\"received\":" + form.constants.length + "}}");
        }
        Relationship[] relationships = new Relationship[form.m];
        for (int i = 0; i < form.m; i++) {
            final String relation = form.relationships[i];
            try {
                relationships[i] = Relationship.valueOf(relation.trim().toUpperCase());
            } catch (Exception e) {
                return res.setStatus("{\"relationships\":{\"received\":\"" +
                        relation + "\",\"expected\":" +
                        SimplexResponse.arrayToString(Relationship.values()) +
                        ",\"index\":" + i + "}}");
            }

        }
        if (form.coefs == null || form.coefs.length < form.m) {
            return res.setStatus("{\"coefficients\":{\"expected\":" + form.m +
                    ",\"received\":" + form.coefs.length + "}}");
        }
        for (int i = 0; i < form.coefs.length; i++) {
            final double[] coefs = form.coefs[i];
            if (coefs.length < form.n) {
                return res.setStatus("{\"coefficients\":{\"" + i + "\":{\"expected\":" + form.n +
                        ",\"received\":" + coefs.length + "}}}");
            }
        }
        if (form.objective == null || form.objective.length < form.n + 1) {
            return res.setStatus("{\"objective\":{\"expected\":" + (form.n + 1) +
                    ",\"received\":" + form.objective.length + "}}");
        }
        // set default
        if (form.advanced == null) {
            form.advanced = new AdvancedOptions();
        }
        if (form.advanced.maxIterations != null && form.advanced.maxIterations <= 0) {
            return res.setStatus("{\"advanced\":{\"maxIterations\":{\"min\":1,\"received\":" +
                    form.advanced.maxIterations + "}}}");
        }
        PivotSelectionRule pivotSelectionRule = AdvancedOptions.pivotSelectionRuleDefault;
        try {
            if (form.advanced.pivotSelectionRule != null) {
                pivotSelectionRule = PivotSelectionRule.valueOf(form.advanced.pivotSelectionRule.trim().toUpperCase());
            }
        } catch (Exception e) {
            return res.setStatus("{\"advanced\":{\"pivotSelectionRule\":{\"received\":\"" +
                    form.advanced.maxIterations + "\",\"expected\":" +
                    SimplexResponse.arrayToString(PivotSelectionRule.values()) +
                    "}}}");
        }
        IntegerMethod integerMethod = AdvancedOptions.integerMethodDefault;
        try {
            if (form.toInteger && form.advanced.integerMethod != null) {
                integerMethod = IntegerMethod.valueOf(form.advanced.integerMethod.trim().toUpperCase());
            }
        } catch (Exception e) {
            return res.setStatus("{\"advanced\":{\"integerMethod\":{\"received\":\"" +
                    form.advanced.maxIterations + "\",\"expected\":" +
                    SimplexResponse.arrayToString(IntegerMethod.values()) +
                    "}}}");
        }

        // once everything has been checked, we can start looking at the values
        final double[] objectiveCoefs = Arrays.copyOfRange(form.objective, 1, form.n + 1);

        // TODO Count number of constraints, if more than necessary, warn for degeneracy

        // setup the solver
        SimplexSolver solver = new SimplexSolver(form.advanced.maxIterations);
        solver.setResolutionSteps(res);
        PointFractionPair optSolution = null;

        try {
            // creating class for the solver data
            ObjectiveFunction f = new ObjectiveFunction(
                    doubleToFraction(objectiveCoefs, false),
                    new Fraction(form.objective[0]));

            ArrayList<Constraint> constraints = new ArrayList<Constraint>(form.m);
            for (int i = 0; i < form.m; i++) {
                constraints.add(new Constraint(
                        doubleToFraction(form.coefs[i], true),
                        relationships[i],
                        new Fraction(form.constants[i])));
            }

            // the optSolution is a struct that have in getFirst the array of double
            // describing the point coordinates
            // and as second the value
            // in our case, first are the optimal values, and second is the optimum
            optSolution = solver.solve(
                    f,
                    new ConstraintSet(constraints),
                    form.toMaximise ? GoalType.MAXIMIZE : GoalType.MINIMIZE,
                    new NonNegativeValues(true),
                    integerMethod,
                    pivotSelectionRule);

            // get our response
            res = solver.getResolutionSteps();

            final Fraction optimum = optSolution.getSecond();
            Logger.trace("Optimum: " + optimum);
            Logger.trace("Points: " + Arrays.toString(optSolution.getFirst()));

            // fill the responses and send
            res.optimum = optimum;
            Fraction[] values = new Fraction[optSolution.getFirst().length];
            for (int i = 0; i < optSolution.getFirst().length; i++) {
                values[i] = optSolution.getFirst()[i];
            }
            res.values = values;
            res.feasibility = Feasibility.FEASIBLE;
        } catch (NoFeasibleSolutionException nfse) {
            // handle not feasible
            Logger.trace(nfse.getMessage() + " (origin: " + nfse.getStackTrace()[0] + ")", false);
            if (Config.Debugger_Enabled()) {
                nfse.printStackTrace();
            }
            res.feasibility = Feasibility.INFEASIBLE;
        } catch (UnboundedSolutionException use) {
            // handle unbounded
            Logger.trace(use.getMessage() + " (origin: " + use.getStackTrace()[0] + ")", false);
            if (Config.Debugger_Enabled()) {
                use.printStackTrace();
            }
            res.feasibility = Feasibility.UNBOUNDED;
        } catch (TooManyIterationsException tmie) {
            // handle the error
            Logger.warn("ITERATIONS: " + tmie.getMessage());
            res.feasibility = Feasibility.ITERATIONS;
            res.setStatus("{\"iterations\":" + solver.getMaxIterations() + "}");
        } catch (MathIllegalStateException e) {
            // math error here
            Logger.error("MATHS: " + e.getMessage() + " (origin: " + e.getStackTrace()[0] + ")");
            if (Config.Debugger_Enabled()) {
                e.printStackTrace();
            }
            res.feasibility = Feasibility.UNKNWON;
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR, "maths");
        } catch (Throwable unrecoverableError) {
            // if debug mode is enabled, crash
            // otherwise, trace and handle the error to prevent said crash
            if (!Config.Debugger_Enabled()) {
                Logger.error("Fatal error recovered because of disabled debug mode: ");
                Logger.error(unrecoverableError.getMessage());
                unrecoverableError.printStackTrace();
                res.feasibility = Feasibility.UNKNWON;
                res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                throw new Error(unrecoverableError);
            }
        }

        return res;
    }

    /**
     * Transform a 2D array of double into it's string representation.
     *
     * @param a A 2D array of double.
     * @return A string representation.
     */
    public static String TwoDArrayToString(double[][] a) {
        ArrayList<String> temp = new ArrayList<>();
        for (double[] a2 : a) {
            temp.add("\n\t" + Arrays.toString(a2));
        }
        temp.add("\n");
        return temp.toString();
    }

    /**
     * Transform an array of double from the input standard, to an array of Fraction
     * in the simplex standard.
     * <p>
     * Which means all coeffs are negated.
     *
     * @param arr              An array of double of coefficients.
     * @param isConstraintCoef Whether the array contains constraints coefficients.
     *                         If
     *                         true, it will negate all coefficient.
     * @return An array of Fraction of coefficients.
     */
    public static Fraction[] doubleToFraction(double[] arr, boolean isConstraintCoef) {
        if (arr == null) {
            return null;
        }
        Fraction[] res = new Fraction[arr.length];
        for (int i = 0; i < arr.length; i++) {
            // negate because in the SimplexForm representation,
            // to the standard form, we go from:
            // Bi = cj + coeffs...
            // to:
            // cj = -coeffs... + Bi
            // but only for constraint, ond not the objective functions coefs
            res[i] = new Fraction(arr[i] * (isConstraintCoef ? -1 : 1));
        }
        return res;
    }
}
