package optim.optim.src.simplex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.UnboundedSolutionException;
import org.apache.commons.math3.util.IntegerSequence.Incrementor;
import org.apache.commons.math3.util.IntegerSequence.Incrementor.MaxCountExceededCallback;
import org.apache.commons.math3.util.Pair;

import optim.optim.response.SimplexResponse;
import optim.optim.src.log.Logger;
import optim.optim.src.simplex.data.Constraint;
import optim.optim.src.simplex.data.ConstraintSet;
import optim.optim.src.simplex.data.GoalType;
import optim.optim.src.simplex.data.IntegerMethod;
import optim.optim.src.simplex.data.NonNegativeValues;
import optim.optim.src.simplex.data.ObjectiveFunction;
import optim.optim.src.simplex.data.PivotSelectionRule;
import optim.optim.src.simplex.data.SimplexData;

// TODO find worst case

/**
 * This class enable to solve a linear problem that is the simplex. It keeps
 * track of each operations to show the step by step process. The value used are
 * supposed "exact" in the way that value used are Fraction, with a numerator
 * and denominator. So they are exact as long as their integer values aren't
 * overflowing.
 * <p>
 * This is slower than using native types, but we care about
 * precision, not speed. The {@code SimplexSolver} by <b>Apache</b> in math3 is
 * doing the job
 * well already.
 * <p>
 * This is heavily inspired from the {@code SimplexSolver} by <b>Apache</b> in
 * math3, only
 * using fraction, handling primal-dual to get integer solutions, branch and
 * bound, solving the dual of the given problem (the primal) and of course,
 * following each steps.
 *
 * @see org.apache.commons.math3.optim.linear.SimplexSolver
 */
public class SimplexSolver {
    /** Default pivot selection rule. */
    public static final PivotSelectionRule defaultPivotRule = PivotSelectionRule.DANTZIG;

    /** Default pivot selection rule. */
    public static final GoalType defaultGoal = GoalType.MAXIMIZE;

    /** Default method to solve the integer problem. */
    public static final IntegerMethod defaultIntgerMethod = IntegerMethod.NONE;

    /** Default maximum amount of iterations. */
    public static final int defaultMaxIterations = Integer.MAX_VALUE;

    /** Default for non negative values. */
    public static final boolean defaultNonNegative = true;

    /** The pivot selection method to use. */
    private PivotSelectionRule pivotSelectionRule = defaultPivotRule;

    /** Linear objective function. */
    private ObjectiveFunction objectiveFunction;

    /** Linear constraints. */
    private List<Constraint> linearConstraints;

    /** Whether to restrict the variables to non-negative values. */
    private boolean nonNegative = defaultNonNegative;

    /** Type of optimization. */
    private GoalType goal = defaultGoal;

    /** Whether we solve for the integer problem, and if yes, with which method. */
    private IntegerMethod integerMethod = defaultIntgerMethod;

    /** True if we are currently trying to solve the integer problem. */
    private boolean solvingInteger = false;

    /** Iterations counter. */
    private Incrementor iterations;

    /** Maximum number of iterations to do. */
    private int maxIterations;

    /** DEBUG to remove */
    private long consecutiveDegenerate = 0;
    private long consecutiveDegenerateThreshold = 100;

    /**
     * A simplex response that will be used to track each steps made during the
     * resolution.
     */
    private SimplexResponse resolutionSteps = null;

    /** Builds a simplex solver with default settings. */
    public SimplexSolver() {
        this(defaultMaxIterations);
    }

    /**
     * Create a new solver with the given amount of maximum iterations.
     *
     * @param maxIterations The maximum amount of iterations to do when solving.
     */
    public SimplexSolver(final int maxIterations) {
        setMaxIterations(maxIterations);
        resetIterations();
    }

    /**
     * Reset the iteration incrementor with the given maximum iteration.
     */
    private void resetIterations() {
        this.iterations = Incrementor.create()
                .withStart(0)
                .withIncrement(1)
                .withMaximalCount(maxIterations)
                .withCallback(new MaxCountExceededCallback() {
                    @Override
                    public void trigger(int maximalCount) throws MaxCountExceededException {
                        throw new TooManyIterationsException(maximalCount);
                    }
                });
    }

    /**
     * Set the maximum amount of iterations to do when solving. This will apply on
     * the next solve call, and not when one is currently running.
     *
     * @param maxIterations The maximum amount of iterations to do.
     */
    public void setMaxIterations(int maxIterations) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations is less or equal to 0");
        }
        this.maxIterations = maxIterations;
    }

    /**
     * Get the maximum amount of iterations that can be made while solving.
     * <p>
     * Note that this is the last value set, and may not reflect the current value
     * used if the solving algorithm is still running.
     *
     * @return The maximum amount of iterations.
     */
    public int getMaxIterations() {
        return maxIterations;
    }

    /**
     * Returns the column with the most positive coefficient in the objective
     * function row, excluding negative coefficient.
     *
     * @param table The simplex table before the iteration.
     * @return the column with the most positive coefficient.
     */
    protected Integer getPivotColumn(final SimplexTable table) {
        Fraction maxValue = Fraction.ZERO;
        Integer maxPos = null;

        if (pivotSelectionRule == PivotSelectionRule.BLAND) {
            final boolean isPhase1 = table.isPhase1();
            for (int i = table.getColOffset(); i < table.getWidth(); i++) {
                final Fraction entry = table.getEntry(0, i);
                if (entry.compareTo(maxValue) == 1) {
                    maxPos = i;
                    maxValue = entry;

                    // check for Bland's pivot rule
                    // which chose the column with the lowest index
                    if (isValidPivotColumn(table, i)) {
                        break;
                    }

                    // prefer lambda over anaything in phase 1 (useless?)
                    if (isPhase1 && maxPos == 1) {
                        break;
                    }
                }
            }
        } else if (pivotSelectionRule == PivotSelectionRule.DANTZIG
                // TODO implement greedy
                || pivotSelectionRule == PivotSelectionRule.GREEDY) {
            for (int i = table.getColOffset(); i < table.getWidth(); i++) {
                final Fraction entry = table.getEntry(0, i);
                if (entry.compareTo(maxValue) == 1) {
                    maxPos = i;
                    maxValue = entry;
                }
            }
        } else if (pivotSelectionRule == PivotSelectionRule.RANDOM) {
            // get all valid columns
            ArrayList<Integer> positions = new ArrayList<>();
            for (int i = table.getColOffset(); i < table.getWidth(); i++) {
                final Fraction entry = table.getEntry(0, i);
                if (entry.isPositive()) {
                    positions.add(i);
                }
            }
            // get a random value in the array
            maxPos = positions.get((int) (Math.random() * positions.size()));
        }

        return maxPos;
    }

    /**
     * Returns the pair column/row of the pivot. It selects the lowest positive
     * ration, including negative objective line values, contrary to
     * {@link #getPivotColumn(SimplexTable)}.
     *
     * @param table The simplex table before the iteration.
     * @return Pair column/row giving the pivot for this iteration.
     */
    protected Pair<Integer, Integer> getIntegerPivot(final SimplexTable table) {
        // TODO implement pivot selection rules
        // get the list of the ratio, and we will get the minimum out of it
        List<Pair<Integer, Integer>> minRatioPositions = new ArrayList<>();
        Fraction minRatio = null;
        Integer pivotCol = null;
        Integer pivotRow = null;

        // TODO check if for a integer variable (the false thing to do), the newly
        // created constraint is equivalent to another one

        // TODO special cut of the objective line if z0 is not integer

        // check only the original constraints
        for (int col = table.getColOffset(); col < table.getNumDecisionVariables() +
                table.getNumSlackVariables(); col++) {
            // skip 0
            // BUG we're supposed to check the bl and not cl?
            Fraction pivotColEntry = table.getEntry(0, col);
            // skip if the col already an integer
            if (pivotColEntry.isInteger()) {
                continue;
            }

            pivotCol = col;
            for (int row = table.getRowOffset(); row < table.getHeight(); row++) {
                pivotRow = row;

                final Fraction entry = table.getEntry(row, col);
                // non acceptable entry
                if (entry.isZero()) {
                    continue;
                }

                final Fraction objectiveRowValue = table.getEntry(row, 0);
                // using multiply with reciprocal because there are less internal evaluations
                // than with divide
                final Fraction ratio = objectiveRowValue.multiply(entry.reciprocal());

                // we still want a positive ratio
                if (ratio.isNegative()) {
                    continue;
                }

                if (minRatio == null) {
                    minRatio = ratio;
                    minRatioPositions.add(new Pair<Integer, Integer>(pivotCol, pivotRow));
                } else {
                    // compare both value and store them accordingly
                    final int cmp = ratio.compareTo(minRatio);
                    if (cmp == 0) {
                        // found two ratios that are the same
                        minRatioPositions.add(new Pair<Integer, Integer>(pivotCol, pivotRow));
                    } else if (cmp == -1) {
                        // found a new min, remove the old ones
                        minRatioPositions.clear();
                        minRatioPositions.add(new Pair<Integer, Integer>(pivotCol, pivotRow));
                        minRatio = ratio;
                    }
                }
            }
        }

        if (minRatioPositions.size() == 0) {
            consecutiveDegenerate = 0;
            // return for unbounded solution
            return new Pair<Integer, Integer>(pivotCol, null);
        } else if (minRatioPositions.size() == 1) {
            consecutiveDegenerate = 0;
            return minRatioPositions.get(0);
        } else {
            // we found multiple times the same ratio
            // we are at a degenerated edge
            // TODO optimization? like a shake of those points
            Logger.warn("degenerate point");

            // we apply Bland's rule to prevent cycling:
            // we take the row with the corresponding basic variable that has the smallest
            // index

            consecutiveDegenerate++;
            if (consecutiveDegenerate >= consecutiveDegenerateThreshold) {
                throw new RuntimeException(
                        "stuck at degenerate point for more than " + consecutiveDegenerateThreshold + " loops");
            }

            // since we put them in order, the first one has the lowest indice
            return minRatioPositions.get(0);
            // Pair<Integer, Integer> minRowIndicePivot = null;
            // int minIndex = table.getWidth();
            // for (Pair<Integer, Integer> pivot : minRatioPositions) {
            // final int basicVarIndex = table.getBasicVariableCol(pivot.getSecond());
            // if (basicVarIndex < minIndex) {
            // minIndex = basicVarIndex;
            // minRowIndicePivot = pivot;
            // }
            // }
            // return minRowIndicePivot;
        }
    }

    /**
     * Checks whether the given column is valid pivot column, i.e. will result
     * in a valid pivot row.
     * <p>
     * When applying Bland's rule to select the pivot column, it may happen that
     * there is no corresponding pivot row. This method will check if the selected
     * pivot column will return a valid pivot row.
     *
     * @param table The simplex table for the problem.
     * @param col   The column to test.
     * @return {@code true} if the pivot column is valid, {@code false} otherwise.
     */
    public boolean isValidPivotColumn(SimplexTable table, int col) {
        for (int i = table.getColOffset(); i < table.getHeight(); i++) {
            // check, like getPivotRow, if there is a positive ratio
            final Fraction entry = table.getEntry(i, col);
            if (entry.isZero()) {
                continue;
            }
            final Fraction objectiveRowValue = table.getEntry(i, 0);
            if (objectiveRowValue.isZero() && entry.isNegative()) {
                continue;
            }
            final Fraction ratio = objectiveRowValue.multiply(entry.reciprocal());
            if (ratio.getNumerator().compareTo(BigInteger.ZERO) >= 0) {
                // at least 1 valid option
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the row with the minimum ratio as given by the minimum ratio test
     * (MRT).
     *
     * @param table    Simplex table for the problem.
     * @param pivotCol Column to test the ratio of (see
     *                 {@link #getPivotColumn(SimplexTable)}).
     * @return The row with the minimum ratio.
     */
    protected Integer getPivotRow(final SimplexTable table, final int pivotCol) {
        // get the list of the ratio, and we will get the minimum out of it
        List<Integer> minRatioPositions = new ArrayList<Integer>();
        Fraction minRatio = null;

        for (int i = table.getRowOffset(); i < table.getHeight(); i++) {
            final Fraction entry = table.getEntry(i, pivotCol);
            // non acceptable entry
            if (entry.isZero()) {
                continue;
            }

            final Fraction objectiveRowValue = table.getEntry(i, 0);
            // we accept 0, but not from a negative entry
            if (objectiveRowValue.isZero() && entry.isNegative()) {
                continue;
            }

            // using multiply with reciprocal because there are less internal evaluations
            // than with divide
            final Fraction ratio = objectiveRowValue.multiply(entry.reciprocal());

            // negative ratio, or negative multiplied by 0
            if (ratio.isNegative()) {
                continue;
            }

            // compare both value and store them accordingly
            if (minRatio == null) {
                minRatio = ratio;
                minRatioPositions.add(i);
            } else {
                final int cmp = ratio.compareTo(minRatio);
                if (cmp == 0) {
                    // found two ratios that are the same
                    minRatioPositions.add(i);
                } else if (cmp == -1) {
                    // found a new min, remove the old ones
                    minRatioPositions.clear();
                    minRatioPositions.add(i);
                    minRatio = ratio;
                }
            }
        }

        if (minRatioPositions.size() == 0) {
            return null;
        } else if (minRatioPositions.size() == 1) {
            return minRatioPositions.get(0);
        } else {
            // we found multiple times the same ratio
            // we are at a degenerated edge
            // TODO optimization? like a shake of those points
            Logger.warn("degenerate point");

            // we apply Bland's rule to prevent cycling:
            // we take the row with the corresponding basic variable that has the smallest
            // index

            // since we put them in order, the first one has the lowest indice
            return minRatioPositions.get(0);
            // Integer minRow = null;
            // int minIndex = table.getWidth();
            // for (int row : minRatioPositions) {
            // final int basicVarIndex = table.getBasicVariableCol(row);
            // if (basicVarIndex < minIndex) {
            // minIndex = basicVarIndex;
            // minRow = row;
            // }
            // }
            // return minRow;
        }
    }

    /**
     * Perform a simplex iteration, meaning getting the column and row to pivot, and
     * pivoting.
     *
     * @param table The simplex table before the iteration.
     */
    protected void doIteration(final SimplexTable table) {
        iterations.increment();

        Integer pivotCol = null;
        Integer pivotRow = null;
        // solving the integer problem includes checking the objective line values,
        // including negatives values.
        if (solvingInteger) {
            Pair<Integer, Integer> pivot = getIntegerPivot(table);
            pivotCol = pivot.getFirst();
            pivotRow = pivot.getSecond();
        } else {
            pivotCol = getPivotColumn(table);
            pivotRow = getPivotRow(table, pivotCol);
        }

        if (pivotRow == null) {
            // add a step
            resolutionSteps.addStep(pivotCol, pivotRow, table);
            throw new UnboundedSolutionException();
        }

        table.performRowOperations(pivotCol, pivotRow);
        // add a step
        resolutionSteps.addStep(pivotCol, pivotRow, table);
    }

    /**
     * Sole the phase 1 for the simplex method. We use an auxiliary problem where we
     * add an artificial variable lambda (L in the labels). We for the entry of
     * lambda and get the most negative value on the RHS column.
     * <p>
     * It is then just about solving the table as usual until the end. If lambda is
     * basic, the problem is not feasible. Otherwise, transform the table for phase
     * 2.
     *
     * @param table The phase 1 table.
     * @throws NoFeasibleSolutionException if lambda is basic at the end of the
     *                                     resolution.
     */
    protected void solvePhase1(final SimplexTable table) throws NoFeasibleSolutionException {
        if (!table.isPhase1()) {
            return;
        }

        iterations.increment();

        final int LambdaCol = table.getLabels().indexOf(SimplexTable.LAMBDA);
        // force the entry of lambda
        final int pivotCol = LambdaCol;
        int pivotRow = -1;
        Fraction pivotValue = Fraction.ZERO;
        // find the most negative value in the objective column RHS
        for (int i = table.getRowOffset(); i < table.getHeight(); i++) {
            final Fraction entry = table.getEntry(i, 0);
            if (entry.compareTo(pivotValue) == -1) {
                pivotRow = i;
                pivotValue = entry;
            }
        }

        // perform the change
        table.performRowOperations(pivotCol, pivotRow);

        // add a step
        resolutionSteps.addStep(pivotCol, pivotRow, table);

        // normal simplex operations after
        while (!table.isOptimal()) {
            doIteration(table);
        }

        // check if lambda is basic
        // if yes, that means lambda != 0, so no solution
        if (table.isBasicCol(LambdaCol)) {
            throw new NoFeasibleSolutionException();
        }
    }

    /**
     * Solve the integer problem. It will run until all value on the RHS column are
     * integers.
     *
     * @param table The final table of the relaxed problem.
     * @throws NoFeasibleSolutionException if we can't perform an iteration.
     * @throws TooManyIterationsException  if too many iterations have been made.
     *                                     Pay special attention to
     *                                     {@link #maxIterations}, since solving the
     *                                     integer problem is NP hard, and
     *                                     exponentially costly.
     * @see IntegerMethod
     */
    protected void solveInteger(final SimplexTable table)
            throws NoFeasibleSolutionException, TooManyIterationsException {
        solvingInteger = true;

        // check if integer, if not, continue
        while (getIntegerSolution(table) == null) {
            iterations.increment();

            // add constraint to table depending on the method
            if (integerMethod == IntegerMethod.GOMORY) {
                int maxRow = 0;
                Fraction max = Fraction.ZERO;
                // we loop through each basic variables
                for (int i = table.getColOffset(); i < table.getWidth(); i++) {
                    // if a variable is basic, get the maximal decimal parts of the variables
                    if (table.isBasicCol(i)) {
                        final int row = table.getBasicVariableRow(i);
                        final Fraction entry = table.getEntry(row, 0).getDecimalPart();
                        if (entry.compareTo(max) == 1) {
                            max = entry;
                            maxRow = row;
                        }
                    }
                }
                // if no not integer basic variable found, check if we can use the objective
                // line, if not, somethings wrong BUG
                if (maxRow == 0 && table.getEntry(0, 0).isInteger()) {
                    throw new NoFeasibleSolutionException();
                }

                table.addIntegerConstraint(maxRow);

                // add a step
                resolutionSteps.addStep(null, maxRow, table);
            } else {
                // TODO implement other integer methods
                throw new UnsupportedOperationException("IntegerMethod = " + integerMethod.name() + " not implemented");
            }

            // run the simplex until optimal, for the integer method
            while (!table.isOptimal()) {
                doIteration(table);
            }
        }
    }

    /**
     * Get the integer solution of the given table. If the coefficients found are
     * not integers, return null.
     *
     * @param table The table to get the solution from.
     * @return A pair of the optimum and the
     */
    protected PointFractionPair getIntegerSolution(final SimplexTable table) {
        for (int i = 0; i < table.getHeight(); i++) {
            final Fraction entry = table.getEntry(i, 0);
            // check if the col contains only integer
            if (entry.isInteger()) {
                return null;
            }
        }

        return getSolution(table);
    }

    /**
     * Get the solution of the given table. It <b>does not</b> check if this table
     * is optimal or not.
     *
     * @param table The table to get the data from.
     * @return A pair of the optimum and the optimal point.
     */
    protected PointFractionPair getSolution(final SimplexTable table) {
        // TODO min/max goal tranformation?
        // for each decision variable, if it's not basic, their value is 0
        // otherwise, it's the value in the RHS column on the it's basic row
        Fraction[] optimalPoint = new Fraction[table.getNumDecisionVariables()];
        for (int i = 0; i < table.getNumDecisionVariables(); i++) {
            final Integer basicRow = table.getBasicVariableRow(i + table.getColOffset());
            if (basicRow != null) {
                optimalPoint[i] = table.getEntry(basicRow, 0);
            } else {
                optimalPoint[i] = Fraction.ZERO;
            }
        }
        // the optimum is the value of the objective column and row, negated
        return new PointFractionPair(optimalPoint, table.getEntry(0, 0).negate());
    }

    /**
     * Solve the given problem, giving each steps made with the simplex table.
     *
     * @param datas datas An array of data to setup:
     *              <ul>
     *              <li>{@link ObjectiveFunction}: The objective function to
     *              minimize/maximize</li>
     *              <li>{@link Constraint}: The constraints that variables must
     *              meet.</li>
     *              <li>(Optionnal) {@link PivotSelectionRule}: A rule for selecting
     *              pivot. Default: {@code DANTZIG}</li>
     *              <li>(Optionnal) {@link NonNegativeValues}: If we restrict to
     *              positive values only. Default: {@code true}</li>
     *              <li>(Optionnal) {@link GoalType}: If we want to maximize or
     *              minimize the objective function. Default: {@code MAXIMIZE}</li>
     *              </ul>
     * @return A pair of values, the optimal point coordinates and the optimum
     *         value.
     * @throws TooManyIterationsException  if the maximum number of iterations is
     *                                     exceeded.
     * @throws UnboundedSolutionException  if the solution escapes to infinity
     *                                     (constraints too loose).
     * @throws NoFeasibleSolutionException if no solution fulfills the constraints
     *                                     (constraints too tight).
     */
    public PointFractionPair solve(SimplexData... datas)
            throws TooManyIterationsException,
            UnboundedSolutionException,
            NoFeasibleSolutionException { // reset datas
        linearConstraints = new ArrayList<Constraint>();
        resetIterations();

        // parse provided datas
        parseDatas(datas);

        // check everything is set to solve
        checkDatas();

        // create a new response if none have been made
        resolutionSteps = setResolutionSteps(resolutionSteps);

        // create the simplex table
        final SimplexTable table = new SimplexTable(objectiveFunction, linearConstraints, goal, nonNegative);

        // solve phase 1 if needed
        if (table.needPhase1()) {
            Logger.trace("Solving phase 1");
            // first table of the phase 1
            resolutionSteps.addStep(null, null, table);

            solvePhase1(table);
        }

        // if phase 1 ok, remove unused columns and rows
        table.morphToPhase2();

        // first table of the phase 2
        resolutionSteps.addStep(null, null, table);

        while (!table.isOptimal()) {
            doIteration(table);
        }

        // TODO not standard: if coeff is 0 for non basic var, infinity of solution

        if (integerMethod != IntegerMethod.NONE) {
            Logger.trace("Solving integer: " + integerMethod.name());
            solveInteger(table);
            // DEBUG usefull?
            if (getIntegerSolution(table) == null) {
                throw new UnboundedSolutionException();
            }
        } else {
            Logger.info("Not solving integer");
        }

        return getSolution(table);
    }

    /**
     * Parse data provided to the solver to setup the simplex problem and how to
     * solve it.
     *
     * @param datas An array of data to setup:
     *              <ul>
     *              <li>{@link ObjectiveFunction}: The objective function to
     *              minimize/maximize</li>
     *              <li>{@link Constraint}: The constraints that variables must
     *              meet.</li>
     *              <li>(Optionnal) {@link PivotSelectionRule}: A rule for selecting
     *              pivot. Default: {@code DANTZIG}</li>
     *              <li>(Optionnal) {@link NonNegativeValues}: If we restrict to
     *              positive values only. Default: {@code true}</li>
     *              <li>(Optionnal) {@link GoalType}: If we want to maximize or
     *              minimize the objective function. Default: {@code MAXIMIZE}</li>
     *              </ul>
     */
    private void parseDatas(SimplexData[] datas) {
        if (datas == null) {
            return;
        }

        for (SimplexData data : datas) {
            if (data == null) {
                continue;
            }
            if (data instanceof ObjectiveFunction) {
                this.objectiveFunction = (ObjectiveFunction) data;
                continue;
            }
            if (data instanceof ConstraintSet) {
                this.linearConstraints.addAll(((ConstraintSet) data).getConstraints());
                continue;
            }
            if (data instanceof NonNegativeValues) {
                this.nonNegative = ((NonNegativeValues) data).getNonNegative();
                continue;
            }
            if (data instanceof PivotSelectionRule) {
                this.pivotSelectionRule = (PivotSelectionRule) data;
                continue;
            }
            if (data instanceof IntegerMethod) {
                this.integerMethod = (IntegerMethod) data;
                continue;
            }
            if (data instanceof GoalType) {
                this.goal = (GoalType) data;
                continue;
            }
        }
    }

    /**
     * Check if the provided datas are set and have correct values.
     *
     * @throws NullPointerException       if objectiveFunction or linearConstraints
     *                                    have not been set.
     * @throws DimensionMismatchException if the number of coefficients in a
     *                                    constraints is different than the amount
     *                                    of coefficients in the objective function.
     */
    private void checkDatas() throws NullPointerException, DimensionMismatchException {
        Objects.requireNonNull(objectiveFunction, "objectiveFunction has not been provided");
        if (linearConstraints == null || linearConstraints.size() == 0) {
            throw new NullPointerException("no linearConstraints has not been provided");
        }
        final int coefAmount = objectiveFunction.getCoefficients().length;
        for (int i = 0; i < linearConstraints.size(); i++) {
            final int I = i;
            if (linearConstraints.get(I).getCoefficients().length != objectiveFunction.getCoefficients().length) {
                throw new DimensionMismatchException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE,
                        linearConstraints.get(I).getCoefficients().length, coefAmount);
            }
        }
    }

    /**
     * Get the steps made during the resolution of this problem.
     * If non were given before starting, a new instance have been created.
     *
     * @return The filled steps.
     */
    public SimplexResponse getResolutionSteps() {
        return resolutionSteps;
    }

    /**
     * Set the class that will be filled with each steps made during the resolution.
     * If null if provided, a new instance is be created an returned.
     *
     * @param steps The steps to fill.
     * @return The instance given, or the instance created.
     */
    public SimplexResponse setResolutionSteps(SimplexResponse steps) {
        this.resolutionSteps = (steps == null ? new SimplexResponse() : steps);
        return this.resolutionSteps;
    }
}
