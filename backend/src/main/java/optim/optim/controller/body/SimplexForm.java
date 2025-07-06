package optim.optim.controller.body;

/**
 * Request body for the simplex solver endpoint.
 */
public class SimplexForm {
    /** Default constructor. */
    public SimplexForm() {
    }

    /** Amount of constraint. */
    public int m = 0;
    /** Amount of variables. */
    public int n = 0;
    /** Indices of loose variables. */
    public int[] loose = null;
    /** Indices of tight variables. */
    public int[] tight = null;
    /** Right hand side constants. */
    public double[] constants = null;
    /** Objective coefficients. The value at index is the objective constant. */
    public double[] objective = null;
    /**
     * Relation between variable coefficients and their constants, such as
     * {@code coefficients RELATIONSHIP constant}.
     */
    public String[] relationships = null;
    /** Loose variables coefficients. */
    public double[][] coefs = null;
    /** Whether to maximise or minimise the problem. */
    public boolean toMaximise = true;
    /**
     * Whether or not to solve the integer problem. The default method will be
     * {@code GOMORY}.
     */
    public boolean toInteger = false;
    /** Advanced, optionnal options. */
    public AdvancedOptions advanced = new AdvancedOptions();
}
