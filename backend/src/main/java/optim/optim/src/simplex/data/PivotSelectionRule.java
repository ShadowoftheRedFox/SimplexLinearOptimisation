package optim.optim.src.simplex.data;

/** An enum of pivot selection rules for the column. */
public enum PivotSelectionRule implements SimplexData {
    /**
     * The classical rule, the variable with the most positive coefficient
     * in the objective function row will be chosen as entering variable.
     */
    DANTZIG,
    /**
     * The first variable with a positive coefficient in the objective function
     * row will be chosen as entering variable. This rule guarantees to prevent
     * cycles, but may take longer to find an optimal solution.
     */
    BLAND,
    /**
     * The variable that makes the objective function increase the most will be
     * chosen as entering variable. It must do more calculation, as it needs to
     * check each objective if the choosent variable where the one it is looking at.
     *
     * <b>NOT IMPLEMENTED</b>, will falls back on {@code DANTZIG}
     */
    GREEDY,
    /**
     * The variable selected is random amongst the possible valid ones. It may
     * reduce drasticly the efficiency for a random problem, or increase it, if the
     * current problem is known to be a worst case scenario (e.g. the Klee-Minty
     * Cube).
     */
    RANDOM
}
