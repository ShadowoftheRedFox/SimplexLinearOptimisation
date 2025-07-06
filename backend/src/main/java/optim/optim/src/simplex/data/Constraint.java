package optim.optim.src.simplex.data;

import java.util.Arrays;

import org.apache.commons.math3.optim.linear.Relationship;

import optim.optim.src.simplex.Fraction;

/**
 * This represent an constraint to be used to solve a simplex problem.
 * <p>
 * With {@code n} the length of the coefficients array, the function is:
 *
 * <pre>
 * coef_0*x_0 + coef_1*x_1 + ... + coef_n*x_n relationship value
 * </pre>
 *
 * Where {@code relation} is either {@code <=}, {@code =} or {@code >=}.
 */
public class Constraint implements SimplexData {
    /** Coefficients of each variables. */
    protected final Fraction[] coefficients;
    /** Relation ship between left and right hand sides. */
    protected final Relationship relationship;
    /** Value of the right hand side. */
    protected final Fraction value;

    /**
     * Create a new constraint with the given parameters.
     *
     * @param coefficients Constraint coefficients.
     * @param relationship Constraint relationship between both sides.
     * @param value        Constraint value of the right side.
     * @throws NullPointerException if any of the parameters are null, or if one of
     *                              the coefficient is null.
     */
    public Constraint(Fraction[] coefficients, Relationship relationship, Fraction value) throws NullPointerException {
        if (coefficients == null) {
            throw new NullPointerException("coefficients is null");
        }
        if (relationship == null) {
            throw new NullPointerException("relation is null");
        }
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        for (int i = 0; i < coefficients.length; i++) {
            if (coefficients[i] == null) {
                throw new NullPointerException("coefficients " + i + "/" + (coefficients.length - 1) + " is null");
            }
        }

        this.coefficients = coefficients;
        this.relationship = relationship;
        this.value = value;
    }

    /**
     * Normalize this constraint for the simplex method, meaning transforming this
     * constraint in a new one that will be:
     *
     * <pre>
     * coef_0 * x_0 + coef_1 * x_1 + ... + coef_n * x_n &lt;= value
     * </pre>
     *
     * @return One new constraint if {@code relationship} is {@code <=} or
     *         {@code >=}.
     *         Two new constraints if {@code relationship} is {@code =}.
     */
    public Constraint[] normalize() {
        // TODO reduce values with a GCD
        Constraint[] result = new Constraint[relationship == Relationship.EQ ? 2 : 1];
        switch (relationship) {
            case EQ:
                result[0] = new Constraint(coefficients, Relationship.LEQ, value);
                // oppose all coefficients
                Fraction[] opositeCoefficientsEQ = new Fraction[coefficients.length];
                for (int i = 0; i < opositeCoefficientsEQ.length; i++) {
                    opositeCoefficientsEQ[i] = coefficients[i].negate();
                }
                result[1] = new Constraint(opositeCoefficientsEQ, Relationship.LEQ, value.negate());
                break;
            case LEQ:
                // same as current, but a clone
                result[0] = new Constraint(coefficients, Relationship.LEQ, value);
                break;
            case GEQ:
                // oppose all coefficients
                Fraction[] opositeCoefficientsGEQ = new Fraction[coefficients.length];
                for (int i = 0; i < opositeCoefficientsGEQ.length; i++) {
                    opositeCoefficientsGEQ[i] = coefficients[i].negate();
                }
                result[0] = new Constraint(opositeCoefficientsGEQ, Relationship.LEQ, value.negate());
                break;
            default:
                throw new Error("Unknown relationship");
        }
        return result;
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
     * Get the value.
     *
     * @return The value.
     */
    public final Fraction getValue() {
        return value;
    }

    /**
     * Get the relation.
     *
     * @return The relation.
     */
    public final Relationship getRelationship() {
        return relationship;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (obj instanceof Constraint) {
            return Arrays.equals(((Constraint) obj).getCoefficients(), coefficients) &&
                    relationship.equals(((Constraint) obj).getRelationship()) &&
                    value.equals(((Constraint) obj).getValue());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coefficients) ^
                relationship.hashCode() ^
                value.hashCode();
    }
}
