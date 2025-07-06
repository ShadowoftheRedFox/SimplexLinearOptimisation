package optim.optim.src.simplex.data;

import optim.optim.src.simplex.Fraction;

/**
 * This represent an objective function to be used to solve a simplex problem.
 * <p>
 * With {@code n} the length of the coefficients array, the function is:
 *
 * <pre>
 * f(x) = coef_0*x_0 + coef_1*x_1 + ... + coef_n*x_n + constant
 * </pre>
 */
public class ObjectiveFunction implements SimplexData {
    /** Coefficient of the function. */
    protected final Fraction[] coefficients;
    /** Constant of the function */
    protected final Fraction constant;

    /**
     * Create a new objective function, with the constant equal to 0.
     *
     * @param coefficients Function coefficients.
     * @throws NullPointerException if coefficients is null, or any of the
     *                              coefficient is null.
     */
    public ObjectiveFunction(Fraction[] coefficients) throws NullPointerException {
        this(coefficients, Fraction.ZERO);
    }

    /**
     * Create a new objective function.
     *
     * @param coefficients Function coefficients.
     * @param constant     Function constant.
     * @throws NullPointerException if coefficients is null, or any of the
     *                              coefficient is null, or constant is null.
     */
    public ObjectiveFunction(Fraction[] coefficients, Fraction constant) throws NullPointerException {
        if (coefficients == null) {
            throw new NullPointerException("coefficients is null");
        }
        if (constant == null) {
            throw new NullPointerException("constant is null");
        }
        for (int i = 0; i < coefficients.length; i++) {
            if (coefficients[i] == null) {
                throw new NullPointerException("coefficients " + i + " is null");
            }
        }
        this.coefficients = coefficients;
        this.constant = constant;
    }

    /**
     * Get the coefficients array.
     *
     * @return The coefficients.
     */
    public final Fraction[] getCoefficients() {
        return coefficients;
    }

    /**
     * Get the constant.
     *
     * @return The constant.
     */
    public final Fraction getConstant() {
        return constant;
    }
}
