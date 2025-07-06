package optim.optim.src.simplex.data;

/**
 * Type of method used to solve the integer problem with the simplex method.
 * As a reminder, solving the integer problem <b>NP hard</b>. The amount
 * of computation is exponential proportionnal to the amount of constraint and
 * variables.
 *
 * @see https://en.wikipedia.org/wiki/NP-hardness
 * @see https://en.wikipedia.org/wiki/NP-completeness
 */
public enum IntegerMethod implements SimplexData {
    /** Value used as default. It will not solve the integer problem. */
    NONE,
    /**
     * With this method, the next constraint is made from the maximum decimal part
     * of each basic variables, to "remove" the overflow of real numbers.
     * <p>
     * This method will always end, since we always chose the largest decimal part
     * of each candidates.
     */
    GOMORY,
    /**
     * TODO
     * Create a tree with a lower and upper bound of [SOME VARIABLE], to have a
     * pruning of solutions.
     */
    BRANCH_AND_BOUND,
    /**
     * TODO
     * Nearly the same as {@link #BRANCH_AND_BOUND}, but if the current optimisic
     * evaluation is way higher than [ANOTHER ONE], "cut", by evaluating more
     * precisely the branch.
     */
    BRANCH_AND_CUT
}
