package optim.optim.src.simplex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.UnboundedSolutionException;

import optim.optim.src.error.MalformedConstraintError;
import optim.optim.src.log.Logger;
import optim.optim.src.simplex.data.Constraint;
import optim.optim.src.simplex.data.GoalType;
import optim.optim.src.simplex.data.ObjectiveFunction;

/**
 * A table to use in the Simplex method.
 *
 * <p>
 * Example:
 *
 * <pre>
 *| RHS | L  |  x1  |  x2  | y1 |  y2
 * -----------------------------------
 *   0    -1     0     0     0     0   &lt;= objective
 *  -6    -1     0     1     1     0   &lt;= constraint 1
 *   9    -1     1     0     0     1   &lt;= constraint 2
 * </pre>
 *
 * RHS: Objective function column |
 * L: lambda for phase 1 |
 * x1 &amp; x2: Decision variables |
 * y1 &amp; y2: Slack/Surplus variables
 */
public class SimplexTable {
    /**
     * BigInteger of the max integer value. Used for solving overflow operations.
     */
    public final static BigInteger MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    /**
     * BigInteger of the min integer value. Used for solving overflow operations.
     */
    public final static BigInteger MIN = BigInteger.valueOf(Integer.MIN_VALUE);

    /** Linear objective function. */
    private final ObjectiveFunction f;

    /** Linear constraints. */
    private final List<Constraint> constraints;

    /** Whether to restrict the variables to non-negative values. */
    private final boolean restrictToNonNegative;

    /** The variables each column represents */
    private List<String> columnLabels;

    /** Value of the lambda label. */
    public final static String LAMBDA = "L";

    /** Value of the objective function label. */
    public final static String OBJECTIVE_FUNCTION = "RHS";

    /**
     * Value of the decision variable label. They will be followed by their indice.
     */
    public final static String DECISION_VARIABLE = "x";

    /**
     * Value of the slack variable label. They will be followed by their indice.
     */
    public final static String SLACK_VARIABLE = "y";

    /**
     * Value of the integer constraint label. They will be followed by their indice.
     */
    public final static String INTEGER_CONSTRAINT = "i";

    /** Simplex table. */
    private transient MatrixFractions table;

    /** Number of decision variables. */
    private final int numDecisionVariables;

    /** Number of slack variables. */
    private final int numSlackVariables;

    /** Maps basic variables row they are basic in to the variable column. */
    private HashMap<Integer, Integer> basicMap;
    /** Maps basic variables column to the row they are basic in. */
    private HashMap<Integer, Integer> invertedBasicMap;

    /**
     * Create a new table for the simplex method.
     * <p>
     * It is initialized by default for the phase 1, and will perform the phase 1
     * until {@link #morphToPhase2()} is called, to prepare the table for the phase
     * 2.
     *
     * @param f                     The objective function of the problem.
     * @param constraints           A set of constraints.
     * @param goalType              Whether to maximize or minimize the objective
     *                              function.
     * @param restrictToNonNegative {@code true} if the possible solution should be
     *                              restricted to positive values only. <b> Not
     *                              implememted for {@code false} yet!</b>
     * @throws NullPointerException If any of the parameters are null, or if
     *                              constraints contains a null value.
     */
    public SimplexTable(final ObjectiveFunction f,
            final Collection<Constraint> constraints,
            final GoalType goalType,
            final boolean restrictToNonNegative) throws NullPointerException {
        Objects.requireNonNull(f, "f can't be null");
        Objects.requireNonNull(constraints, "constraints can't be null");
        Objects.requireNonNull(goalType, "goaltype can't be null");
        this.f = f;
        this.constraints = normalizeConstraints(constraints);
        this.restrictToNonNegative = restrictToNonNegative;
        // TODO implement non negative
        if (!restrictToNonNegative) {
            throw new UnsupportedOperationException("restrictToNonNegative = false not implemented");
        }
        this.numDecisionVariables = f.getCoefficients().length;// + (restrictToNonNegative ? 0 : 1);
        this.numSlackVariables = constraints.size();
        initialiseColumnLabels();
        initialiseBasicVariables();
        this.table = createTable(goalType == GoalType.MAXIMIZE);
    }

    /**
     * Get new versions of the constraints which have LEQ relationship.
     *
     * @param originalConstraints Original (not normalized) constraints.
     * @return New versions of the constraints.
     * @throws NullPointerException if a constraint is null.
     */
    public List<Constraint> normalizeConstraints(Collection<Constraint> originalConstraints)
            throws NullPointerException {
        // a set to remove (exact) duplicates if they ever happens
        // FIXME test if it doesn't cause error with the hashCode
        HashSet<Constraint> normalized = new HashSet<Constraint>(originalConstraints.size());
        for (Constraint constraint : originalConstraints) {
            Objects.requireNonNull(constraint, "a constraint can't be null");
            if (constraint.getCoefficients().length != f.getCoefficients().length) {
                throw new DimensionMismatchException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE,
                        constraint.getCoefficients().length, f.getCoefficients().length);
            }

            if (constraint.getRelationship() == Relationship.EQ) {
                Constraint[] temp = constraint.normalize();
                normalized.add(temp[0]);
                normalized.add(temp[1]);
            } else {
                normalized.add(constraint.normalize()[0]);
            }
        }
        // transorm to a list and return
        return normalized.stream().toList();
    }

    /**
     * Create the tableau by itself.
     *
     * @param maximize if true, goal is to maximize the objective function
     * @return created tableau
     */
    protected MatrixFractions createTable(final boolean maximize) {
        // +2 for :
        // one for the objective function
        // one for the lambda for phase 1
        final int width = numDecisionVariables + numSlackVariables + 2;
        final int height = numSlackVariables + 1; // +1 for the objective row
        MatrixFractions matrix = new MatrixFractions(height, width);

        // initialize the the objective function row
        matrix.setEntryFraction(0, 0, Fraction.ZERO);
        matrix.setEntryFraction(0, 1, Fraction.MINUS_ONE);

        for (int i = 0; i < numDecisionVariables + numSlackVariables; i++) {
            matrix.setEntryFraction(0, getColOffset()
                    + i, Fraction.ZERO);
        }

        // initialize constraints rows
        for (int i = 0; i < numSlackVariables; i++) {
            matrix.setEntryFraction(getRowOffset() + i, 0, constraints.get(i).getValue());
            matrix.setEntryFraction(getRowOffset() + i, 1, Fraction.MINUS_ONE);
            for (int j = 0; j < numDecisionVariables; j++) {
                matrix.setEntryFraction(getRowOffset() + i, getColOffset() +
                        j, constraints.get(i).getCoefficients()[j]);
            }
            for (int j = 0; j < numSlackVariables; j++) {
                matrix.setEntryFraction(getRowOffset() + i, getColOffset() +
                        numDecisionVariables + j, (i == j ? Fraction.ONE : Fraction.ZERO));
            }
        }

        return matrix;
    }

    /**
     * Inittialize the basic variables, who are the slack variables when starting.
     */
    protected void initialiseBasicVariables() {
        basicMap = HashMap.newHashMap(getNumSlackVariables());
        invertedBasicMap = HashMap.newHashMap(getNumSlackVariables());
        for (int i = 0; i < getNumSlackVariables(); i++) {
            // on the row i, the i th slack variable is basic.
            final int col = getColOffset() + getNumDecisionVariables() + i;
            final int row = getRowOffset() + i;
            basicMap.put(row, col);
            invertedBasicMap.put(col, row);
        }
    }

    /**
     * Initialize the label of each columns.
     */
    protected void initialiseColumnLabels() {
        columnLabels = new ArrayList<String>();
        // objective function
        columnLabels.add(OBJECTIVE_FUNCTION);
        // lambda
        columnLabels.add(LAMBDA);
        for (int i = 0; i < getNumDecisionVariables(); i++) {
            columnLabels.add(DECISION_VARIABLE + i);
        }
        for (int i = 0; i < getNumSlackVariables(); i++) {
            columnLabels.add(SLACK_VARIABLE + i);
        }
        columnLabels = Collections.unmodifiableList(columnLabels);
    }

    /**
     * Get the dual of this matrix.
     *
     * @return The dual matrix of this problem.
     * @see https://en.wikipedia.org/wiki/Duality
     * @see https://en.wikipedia.org/wiki/Duality_(optimization)
     */
    public MatrixFractions getDual() {
        // TODO needed for the primal dual, maybe add the new constraint as a parameter?
        return null;
    }

    /**
     * Add a constraint to the table, following the primal-dual method. It can only
     * be done if {@link #isOptimal()} returns {@code true}. Added constraint are
     * supposed basic.
     * <p>
     * If the constraint is preformated, it will directly be added to the table. If
     * not, it will be formated to fit in the table, including normalization. Then
     * it will check if the constraint is satisfied, and if the table is optimal. If
     * one of those checks fail, the primal/dual method will be performed.
     * <p>
     * Same as {@link #addConstraint(Constraint, boolean)} where
     * {@code isPreformated} is false.
     *
     * @param constraint The new constraint.
     * @throws NullPointerException       if the given constraint is null.
     * @throws IllegalCallerException     if the table is not optimal.
     * @throws DimensionMismatchException if the given constraint has not the same
     *                                    amount of coefficients than either
     *                                    {@link #f} or
     *                                    {@link #getWidth()}.
     * @throws MalformedConstraintError   if {@code isPreformated} is true and the
     *                                    last coefficient is not 1.
     */
    public void addConstraint(Constraint constraint)
            throws NullPointerException,
            IllegalCallerException,
            DimensionMismatchException,
            MalformedConstraintError {
        addConstraint(constraint, false);
    }

    /**
     * Add a constraint to the table, following the primal-dual method. It can only
     * be done if {@link #isOptimal()} returns {@code true}. Added constraint are
     * supposed basic.
     * <p>
     * If the constraint is preformated, it will directly be added to the table. If
     * not, it will be formated to fit in the table, including normalization. Then
     * it will check if the constraint is satisfied, and if the table is optimal. If
     * one of those checks fail, the primal/dual method will be performed.
     *
     * @param constraint    The new constraint.
     * @param isPreformated If the contraint is preformated. A preformated
     *                      constraint has a lenght of the current
     *                      {@link #getWidth()}, and will not be edited.
     * @throws NullPointerException       if the given constraint is null.
     * @throws IllegalCallerException     if the table is not optimal or is in phase
     *                                    1.
     * @throws DimensionMismatchException if the given constraint has not the same
     *                                    amount of coefficients than either
     *                                    {@link #f} or
     *                                    {@link #getWidth()}.
     * @throws MalformedConstraintError   if {@code isPreformated} is true and the
     *                                    last coefficient is not 1.
     */
    public void addConstraint(Constraint constraint, boolean isPreformated)
            throws NullPointerException,
            IllegalCallerException,
            DimensionMismatchException,
            MalformedConstraintError {
        if (!isOptimal()) {
            throw new IllegalCallerException("the table is not optimal");
        }
        if (isPhase1()) {
            throw new IllegalCallerException("the table is in phase 1");
        }
        Objects.requireNonNull(constraint);

        // check if it's either a "short" constraint: only on the decision variables
        if (!isPreformated && constraint.getCoefficients().length != getNumDecisionVariables()) {
            throw new DimensionMismatchException(LocalizedFormats.DIMENSIONS_MISMATCH,
                    constraint.getCoefficients().length, getNumDecisionVariables());
        }
        // or a long, so we can just copy it insides (coef on every variables, including
        // the one we will add)
        if (isPreformated) {
            if (constraint.getCoefficients().length != getWidth()) {
                throw new DimensionMismatchException(LocalizedFormats.DIMENSIONS_MISMATCH,
                        constraint.getCoefficients().length, getWidth());
            }
            // expected to have the last col as 1 because we add it as a basic variable
            if (!constraint.getCoefficients()[getWidth() - 1].isOne()) {
                throw new MalformedConstraintError(
                        "expected last coefficient to be 1, because this constraint is preformated and is supposed to be basic");
            }
        }

        Logger.info("new constraint: " + Arrays.toString(constraint.getCoefficients()) +
                " " + constraint.getRelationship().name() +
                " " + constraint.getValue(), false);

        // normalize the constraint, by adding one after the other if necessary
        if (!isPreformated && constraint.getRelationship() != Relationship.LEQ) {
            Constraint[] normalized = constraint.normalize();
            if (normalized.length > 1) {
                addConstraint(normalized[1], false);
            }
            constraint = normalized[0];
        }

        // a new line and a new column
        final int newWidth = getWidth() + 1;
        final int newHeight = getHeight() + 1;

        // edit the column labels if it's not already done (in the case it's an integer
        // cut from addIntegerConstraint(int))
        if (columnLabels.size() != newWidth) {
            ArrayList<String> newColumnLabels = new ArrayList<>(columnLabels);
            newColumnLabels.add(SLACK_VARIABLE + (getNumSlackVariables() + 1));
            columnLabels = Collections.unmodifiableList(newColumnLabels);
        }

        // the new constraint line for this new variable
        Fraction[] line = new Fraction[newWidth];

        line[0] = constraint.getValue();
        if (isPreformated) {
            for (int i = 0; i < newWidth - 1; i++) {
                line[i + getColOffset()] = constraint.getCoefficients()[i];
            }
        } else {
            // TODO rewrite the constraint depending on non basic vbariables
            for (int i = 0; i < getNumDecisionVariables(); i++) {
                line[i + getColOffset()] = constraint.getCoefficients()[i];
            }
            for (int i = getNumDecisionVariables() + getColOffset(); i < newWidth - 1; i++) {
                line[i] = Fraction.ZERO;
            }
            // since we add this new constraint as basic
            line[newWidth - 1] = Fraction.ONE;
        }

        // copy table and add a line in the table for the Gomory method
        // the new column is filled with 0, except on the last column, where it is one
        MatrixFractions matrix = new MatrixFractions(newHeight, newWidth);
        for (int i = 0; i < newWidth - 1; i++) {
            for (int j = 0; j < newHeight - 1; j++) {
                matrix.setEntryFraction(j, i, getEntry(j, i));
            }
        }
        // fill the new column with 0
        for (int i = 0; i < newHeight; i++) {
            matrix.setEntryFraction(i, newWidth - 1, Fraction.ZERO);
        }
        // fill our new line
        for (int i = 0; i < newWidth; i++) {
            matrix.setEntryFraction(newHeight - 1, i, line[i]);
        }

        table = matrix;
        // add our new basic variable to the map
        basicMap.put(newHeight - 1, newWidth - 1);
        invertedBasicMap.put(newWidth - 1, newHeight - 1);

        // check if the new constraint is satisfied, meaning the inequality is
        // true
        boolean satisfied = true;
        Fraction sum = constraint.getValue();
        for (int i = 0; i < getNumDecisionVariables(); i++) {
            if (!isBasicCol(i)) {
                sum = sum.subtract(getEntry(0, getColOffset() + i).multiply(constraint.getCoefficients()[i]));
                if (sum.isNegative()) {
                    satisfied = false;
                    break;
                }
            }
        }

        // and if yes, check if the table is optimal
        if (satisfied && isOptimal()) {
            return;
        }

        // not optimal, need to apply primal-dual
        // solve the problem in the dual for one step
        // we force the entry of our new variable
        // for the leaving, we get the minimum ratio between our new variable row and
        // the objective row, to stay in the constraints

        int pivotRow = -1;
        Fraction minPivot = null;
        for (int i = 1; i < newWidth; i++) {
            if (isBasicCol(i)) {
                continue;
            }

            final Fraction entry = getEntry(newHeight - 1, i);
            // can't divide by 0, for the future operation
            if (entry.isZero()) {
                continue;
            }

            final Fraction objectiveRowValue = getEntry(0, i);
            // we can't take 0 from a netagive entry
            if (objectiveRowValue.isZero() && entry.isNegative()) {
                continue;
            }
            final Fraction ratio = objectiveRowValue.divide(entry);
            // invalid sign
            if (ratio.isNegative()) {
                continue;
            }

            if (pivotRow == -1 || ratio.compareTo(minPivot) == -1) {
                minPivot = ratio;
                pivotRow = i;
                // we can't find lower than 0
                if (ratio.isZero()) {
                    break;
                }
            }
        }

        // TODO truly unbounded or unfeasible?
        if (pivotRow == -1) {
            throw new UnboundedSolutionException();
        }

        performDualRowOperations(newHeight - 1, pivotRow);
    }

    /**
     * Change the internal table to drop each row and columns that are here solely
     * for the phase 1.
     */
    protected void morphToPhase2() throws NoFeasibleSolutionException {
        if (!isPhase1()) {
            return;
        }

        // make sure lambda is basic
        if (isBasicCol(1)) {
            throw new NoFeasibleSolutionException();
        }
        // width of our new matrix
        final int width = getWidth() - 1;

        // we write the new objective function with the non basic variables
        Fraction constant = f.getConstant();
        Fraction[] coeff = new Fraction[width - 1];
        Arrays.fill(coeff, Fraction.ZERO);

        // convert only if the lambda column isn't filled with only -1
        boolean skippedPhase1 = true;
        for (int i = 0; i < getHeight(); i++) {
            if (!getEntry(i, 1).equals(Fraction.MINUS_ONE)) {
                skippedPhase1 = false;
                break;
            }
        }

        // if the phase 1 has been computed, then we need to get back to our initial
        // problem. for each of the f coefficients, if the variable is basic, we need to
        // convert it using the non basics variables
        if (!skippedPhase1) {
            // for each coefficients of the objective
            for (int coefIndex = 0; coefIndex < f.getCoefficients().length; coefIndex++) {
                final Fraction fCoef = f.getCoefficients()[coefIndex];
                // everything will be 0, so we can skip
                if (fCoef.isZero()) {
                    continue;
                }

                // if it is basic, rewrite it under non basic variables
                // +2 because 0 is the objective col, 1 is L
                final Integer row = invertedBasicMap.get(coefIndex + 2);
                if (row != null) {
                    // add the line coef multiplied by the f coeff, for each non basic variable
                    for (int i = 0; i < width - 1; i++) {
                        final int I = i + 2;
                        // if we are at the column we're rewriting, substract the constant
                        if (i == coefIndex) {
                            constant = constant.subtract(getEntry(row, 0).multiply(fCoef));
                        } else if (!invertedBasicMap.containsKey(I)) {
                            // add each lines multiplied by the coef to the objective coefs
                            coeff[i] = coeff[i].subtract(getEntry(row, I).multiply(fCoef));
                        }
                    }
                }
            }
        } else {
            // if we skipped phase one, the coefficients are the original ones
            for (int i = 0; i < getNumDecisionVariables(); i++) {
                coeff[i] = f.getCoefficients()[i];
            }
        }

        // we can now redo our matrix, which is the same, except for the L column, and a
        // change of coefficients for the objecttive function row if there has been a
        // phase 1
        MatrixFractions matrix = new MatrixFractions(getHeight(), width);

        // fill our formated objective function
        matrix.setEntryFraction(0, 0, constant);
        for (int i = 1; i < width; i++) {
            matrix.setEntryFraction(0, i, coeff[i - 1]);
        }

        // then just copy the rest of the table under
        for (int y = 1; y < getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                // apply the correct position for the old table (the lambda column)
                final int X = x + (x == 0 ? 0 : 1);
                matrix.setEntryFraction(y, x, table.getEntryFraction(y, X));
            }
        }

        table = matrix;
        // since L has been removed, we need to move each value of the basic map to the
        // left (-1), as well as the same for the invertedBasicMap, but for the keys
        basicMap.replaceAll((k, v) -> v - 1);
        HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
        invertedBasicMap.forEach((k, v) -> {
            temp.put(k - 1, v);
        });
        invertedBasicMap = temp;

        // remove lambda from the labels
        ArrayList<String> labels = new ArrayList<>(columnLabels);
        labels.remove(LAMBDA);
        columnLabels = Collections.unmodifiableList(labels);
    }

    /**
     * Add a special constraint in the table to solve the integer problem. It will
     * only constraint the a basic constrinat depending of the non basic variables.
     * This is only for the Gomory method.
     *
     * @param row The basic slack variable row to constraint.
     */
    public void addIntegerConstraint(int row) {
        MatrixUtils.checkRowIndex(table, row);
        // check if the given row is basic, and is a slack variable
        if (!isBasicRow(row)) {
            throw new IllegalArgumentException("Given row is not basic: " + row);
        }

        final String integerConstraintLabel = INTEGER_CONSTRAINT
                // current table width
                + (getWidth() -
                // minus the width expected without any integer constraint
                        (getNumDecisionVariables() +
                                getNumSlackVariables() +
                                getColOffset())
                        // +1 to start the indices at 1
                        + 1);

        // a new line and a new column
        final int newWidth = getWidth() + 1;

        // edit the column labels
        ArrayList<String> newColumnLabels = new ArrayList<>(columnLabels);
        newColumnLabels.add(integerConstraintLabel);
        columnLabels = Collections.unmodifiableList(newColumnLabels);

        // the new constraint line for this new variable
        Fraction[] constraint = new Fraction[newWidth - 1];
        // get the negated decimal value of the RHS
        final Fraction value = getEntry(row, 0).getDecimalPart().negate();

        // TODO multiply the whole constraint with an integer to make it stronger
        // from "Strengthening Chvátal–Gomory cuts" by Adam N. Letchforda, Andrea Lodi

        // get the last place as 1, since this new variable is basic
        constraint[newWidth - 2] = Fraction.ONE;

        // for each non basic variables, get the negated decimal parts
        for (int i = 1; i < newWidth - 1; i++) {
            if (!isBasicCol(i)) {
                constraint[i - 1] = getEntry(row, i).getDecimalPart().negate();
            } else {
                constraint[i - 1] = Fraction.ZERO;
            }
        }

        addConstraint(new Constraint(constraint, Relationship.LEQ, value), true);
    }

    /**
     * Given a column and a row, the table will perfom the simplex operation, which
     * is to get the value where the column and the row intersect, called the pivot.
     * Then divide the row by the pivot value. And then substract every row until
     * the pivot column is filled with 0.
     *
     * @param pivotCol The pivot column, or the entering variable.
     * @param pivotRow The pivot row, or the leaving variable.
     */
    protected void performRowOperations(int pivotCol, int pivotRow) {
        // divide the pivot row by the value of the pivot
        divideRow(pivotRow, getEntry(pivotRow, pivotCol));

        // subtract every other row so the pivot column has only zeros
        for (int i = 0; i < getHeight(); i++) {
            if (i != pivotRow) {
                subtractRow(i, pivotRow, getEntry(i, pivotCol));
            }
        }

        // update the basic variable mappings
        putBasicVariables(pivotRow, pivotCol);
    }

    /**
     * Same as {@link #performRowOperations(int, int)}, but do it in the dual of
     * the current table. Which means, in the dual, the current columns are the dual
     * rows and the current rows are the dual columns.
     *
     * @param pivotDualCol The pivot column in the dual, or the entering variable.
     * @param pivotDualRow The pivot row in the dual, or the leaving variable.
     */
    protected void performDualRowOperations(int pivotDualCol, int pivotDualRow) {
        // in the code, we can compute on the current table if we see the dualCol as row
        // and dualRow as col
        final int col = pivotDualRow;
        final int row = pivotDualCol;

        // divide the pivot col by the value of the pivot
        divideCol(col, getEntry(row, col));

        // subtract every other col so the pivot row has only zeros
        for (int i = 0; i < getWidth(); i++) {
            if (i != row) {
                subtractCol(i, col, getEntry(row, i));
            }
        }

        // update the basic variable mappings
        putBasicVariables(row, col);
    }

    /**
     * Put the new basic variable in the corresponding map.
     *
     * @param variableRow The row of the new basic variable.
     * @param variableCol The column of the new basic variable.
     */
    protected void putBasicVariables(final int variableRow, final int variableCol) {
        if (!isBasicRow(variableRow)) {
            throw new Error("given variable (row" + variableRow + ") is not basic"); // TODO create it's own error?
        }
        final Integer oldCol = basicMap.put(variableRow, variableCol);
        invertedBasicMap.remove(oldCol);
        invertedBasicMap.put(variableCol, variableRow);
    }

    /**
     * Divides one row by a given divisor.
     * <p>
     * After application of this operation, the following will hold:
     *
     * <pre>
     * dividedRow = dividedRow / divisor
     * </pre>
     *
     * @param dividedRowIndex Index of the row.
     * @param divisor         Value of the divisor.
     */
    protected void divideRow(final int dividedRowIndex, Fraction divisor) {
        for (int j = 0; j < getWidth(); j++) {
            table.setEntryFraction(dividedRowIndex, j, getEntry(dividedRowIndex, j).divide(divisor));
        }
    }

    /**
     * Subtracts a multiple of one row from another.
     * <p>
     * After application of this operation, the following will hold:
     *
     * <pre>
     * substractedRow = substractedRow - subtractorRow * multiplier
     * </pre>
     *
     * @param substractedRowIndex Row index that is subtracted to.
     * @param subtractorRowIndex  Row index that will substract.
     * @param multiplier          Multiplication factor of the subtractor.
     */
    protected void subtractRow(final int substractedRowIndex, final int subtractorRowIndex, final Fraction multiplier) {
        // early return, since we will subtract 0
        if (multiplier.isZero()) {
            return;
        }

        final Fraction[] substractedRow = getRow(substractedRowIndex);
        final Fraction[] substractorRow = getRow(subtractorRowIndex);
        for (int i = 0; i < getWidth(); i++) {
            final Fraction subtractor = substractorRow[i];
            if (!subtractor.isZero()) {
                table.setEntryFraction(substractedRowIndex, i,
                        substractedRow[i].subtract(subtractor.multiply(multiplier)));
            }
        }
    }

    /**
     * Divides one column by a given divisor.
     * <p>
     * After application of this operation, the following will hold:
     *
     * <pre>
     * dividedCol = dividedCol / divisor
     * </pre>
     *
     * @param dividedColIndex Index of the column.
     * @param divisor         Value of the divisor.
     */
    protected void divideCol(final int dividedColIndex, Fraction divisor) {
        // since we can't "get" the column, iterate through it
        for (int i = 0; i < getHeight(); i++) {
            table.setEntryFraction(i, dividedColIndex, getEntry(i, dividedColIndex).divide(divisor));
        }
    }

    /**
     * Subtracts a multiple of one column from another.
     * <p>
     * After application of this operation, the following will hold:
     *
     * <pre>
     * substractedCol = substractedCol - subtractorCol * multiplier
     * </pre>
     *
     * @param substractedColIndex Column index that is subtracted to.
     * @param subtractorColIndex  Column index that will substract.
     * @param multiplier          Multiplication factor of the subtractor.
     */
    protected void subtractCol(final int substractedColIndex, final int subtractorColIndex, final Fraction multiplier) {
        // early return, since we will subtract 0
        if (multiplier.isZero()) {
            return;
        }

        for (int i = 0; i < getHeight(); i++) {
            final Fraction subtractor = getEntry(i, subtractorColIndex);
            if (!subtractor.isZero()) {
                table.setEntryFraction(i, substractedColIndex,
                        getEntry(i, substractedColIndex).subtract(subtractor.multiply(multiplier)));
            }
        }
    }

    /**
     * Check if the given column is a basic variable. Meaning it's a key in the
     * invertedBasicMap, or the given column contain only one 1 and all 0 otherwise.
     *
     * @param col The column we want to if it's basic or no.
     * @return True if the column is basic.
     */
    public final boolean isBasicCol(final int col) {
        return invertedBasicMap.containsKey(col);
    }

    /**
     * Check if the given row is a basic variable. Meaning it's a row between 0 and
     * {@link #getHeight()}, both excluded.
     *
     * @param row The row we want to if it's basic or no.
     * @return True if the row is basic.
     */
    public final boolean isBasicRow(final int row) {
        return row > 0 && row < getHeight();
    }

    // #region Getters
    /**
     * Get the width of the table.
     *
     * @return width of the table
     */
    public final int getWidth() {
        return table.getColumnDimension();
    }

    /**
     * Get the height of the table.
     *
     * @return height of the table
     */
    public final int getHeight() {
        return table.getRowDimension();
    }

    /**
     * Get the current offset created by the objective function column, and the
     * lambda column if it is still here. The resulting number is the column index
     * of the first decision variable.
     *
     * @return The column offset.
     */
    protected final int getColOffset() {
        return isPhase1() ? 2 : 1;
    }

    /**
     * Get the current offset created by the objective function row. The
     * resulting number is the row index of the first constraint.
     *
     * @return The row offset.
     */
    protected final int getRowOffset() {
        return 1;
    }

    /**
     * Returns the variable that is basic in this row.
     *
     * @param row The index of the row to check.
     * @return The variable that is basic for this row.
     */
    protected Integer getBasicVariableCol(final int row) {
        return basicMap.get(row);
    }

    /**
     * Returns the variable that is basic in this column.
     *
     * @param col The index of the column to check.
     * @return The variable that is basic for this column.
     */
    protected Integer getBasicVariableRow(final int col) {
        return invertedBasicMap.get(col);
    }

    /**
     * Get a list of columns, where the list index correspond to the constraint
     * indice, and the value at this index the column of the basic variable.
     *
     * @return A list of basic variable column.
     */
    public final List<Integer> getBasicVariables() {
        return basicMap.values().stream().toList();
    }

    /**
     * Return the number of decision variables, which is equals to the number of
     * coefficients of the objective function and of each constraints.
     *
     * @return The number of decision variables.
     */
    protected final int getNumDecisionVariables() {
        return numDecisionVariables;
    }

    /**
     * Return the number of slack variables, which is also the number of
     * constraints. Those are added columns to use the simplex resolution method.
     *
     * @return The number of slack variables.
     */
    protected final int getNumSlackVariables() {
        return numSlackVariables;
    }

    /**
     * Get the value on the given row and column.
     *
     * @param row The row to get the entry at.
     * @param col The column to get the entry at.
     * @return The value of the entry.
     */
    public final Fraction getEntry(int row, int col) {
        return table.getEntryFraction(row, col);
    }

    /**
     * Get the raw inner table value.
     *
     * @return The inner table.
     */
    public final Fraction[][] getSimplexTable() {
        return table.getDataFraction();
    }

    /**
     * Get the row from the table.
     *
     * @param row the row index
     * @return the reference to the underlying row data
     */
    protected final Fraction[] getRow(int row) {
        return table.getDataRef()[row];
    }

    /**
     * Get the current phase of the table.
     *
     * @return True if the table is in phase 1, false otherwise.
     */
    public final boolean isPhase1() {
        return table == null || // this check for internal call before we set the table
                columnLabels.contains(LAMBDA);
    }

    /**
     * Verify if the phase 1 is needed.
     *
     * @return Return true if the phase 1 is needed. False if not or if the table is
     *         in phase 2 already.
     */
    public boolean needPhase1() {
        if (!isPhase1()) {
            return false;
        }
        // for launching phase 1 resolution, we must lookup if any value in the RHS is
        // negative
        for (int i = getRowOffset(); i < getHeight(); i++) {
            if (getEntry(i, 0).isNegative()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current table is optimal.
     *
     * @return True is the current table is optimal for this phase, false otherwise.
     */
    public boolean isOptimal() {
        // we check the objective function row only has zero or negative values
        for (int i = 1; i < getWidth(); i++) {
            if (getEntry(0, i).isPositive()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the list of column labels.
     *
     * @return An unmodifiable list.
     */
    public final List<String> getLabels() {
        return columnLabels;
    }
    // #endregion
}
