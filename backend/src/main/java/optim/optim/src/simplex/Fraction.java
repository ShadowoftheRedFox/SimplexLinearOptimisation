/**
 * Edited to use BigInteger instead of just int.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package optim.optim.src.simplex;

import java.math.BigInteger;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.fraction.FractionConversionException;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.FastMath;

/**
 * Representation of a rational number.
 *
 * implements Serializable since 2.0
 *
 * @since 1.1
 */
public class Fraction
        extends Number
        implements Comparable<Fraction> {

    /** A fraction representing "2 / 1". */
    public static final Fraction TWO = new Fraction(2, 1);

    /** A fraction representing "1". */
    public static final Fraction ONE = new Fraction(1, 1);

    /** A fraction representing "0". */
    public static final Fraction ZERO = new Fraction(0, 1);

    /** A fraction representing "4/5". */
    public static final Fraction FOUR_FIFTHS = new Fraction(4, 5);

    /** A fraction representing "1/5". */
    public static final Fraction ONE_FIFTH = new Fraction(1, 5);

    /** A fraction representing "1/2". */
    public static final Fraction ONE_HALF = new Fraction(1, 2);

    /** A fraction representing "1/4". */
    public static final Fraction ONE_QUARTER = new Fraction(1, 4);

    /** A fraction representing "1/3". */
    public static final Fraction ONE_THIRD = new Fraction(1, 3);

    /** A fraction representing "3/5". */
    public static final Fraction THREE_FIFTHS = new Fraction(3, 5);

    /** A fraction representing "3/4". */
    public static final Fraction THREE_QUARTERS = new Fraction(3, 4);

    /** A fraction representing "2/5". */
    public static final Fraction TWO_FIFTHS = new Fraction(2, 5);

    /** A fraction representing "2/4". */
    public static final Fraction TWO_QUARTERS = new Fraction(2, 4);

    /** A fraction representing "2/3". */
    public static final Fraction TWO_THIRDS = new Fraction(2, 3);

    /** A fraction representing "-1 / 1". */
    public static final Fraction MINUS_ONE = new Fraction(-1, 1);

    /** Serializable version identifier */
    private static final long serialVersionUID = 3698073679419233275L;

    /** The default epsilon used for convergence. */
    private static final double DEFAULT_EPSILON = 1e-5;

    /** The denominator. */
    private final BigInteger denominator;

    /** The numerator. */
    private final BigInteger numerator;

    /**
     * Create a fraction given the double value.
     *
     * @param value the double value to convert to a fraction.
     * @throws FractionConversionException if the continued fraction failed to
     *                                     converge.
     */
    public Fraction(double value) throws FractionConversionException {
        this(value, DEFAULT_EPSILON, 100);
    }

    /**
     * Create a fraction given the double value and maximum error allowed.
     * <p>
     * References:
     * <ul>
     * <li><a href="http://mathworld.wolfram.com/ContinuedFraction.html">
     * Continued Fraction</a> equations (11) and (22)-(26)</li>
     * </ul>
     *
     * @param value         the double value to convert to a fraction.
     * @param epsilon       maximum error allowed. The resulting fraction is within
     *                      {@code epsilon} of {@code value}, in absolute terms.
     * @param maxIterations maximum number of convergents
     * @throws FractionConversionException if the continued fraction failed to
     *                                     converge.
     */
    public Fraction(double value, double epsilon, int maxIterations)
            throws FractionConversionException {
        this(value, epsilon, Integer.MAX_VALUE, maxIterations);
    }

    /**
     * Create a fraction given the double value and maximum denominator.
     * <p>
     * References:
     * <ul>
     * <li><a href="http://mathworld.wolfram.com/ContinuedFraction.html">
     * Continued Fraction</a> equations (11) and (22)-(26)</li>
     * </ul>
     *
     * @param value          the double value to convert to a fraction.
     * @param maxDenominator The maximum allowed value for denominator
     * @throws FractionConversionException if the continued fraction failed to
     *                                     converge
     */
    public Fraction(double value, int maxDenominator)
            throws FractionConversionException {
        this(value, 0, maxDenominator, 100);
    }

    /**
     * Create a fraction given the double value and either the maximum error
     * allowed or the maximum number of denominator digits.
     * <p>
     *
     * NOTE: This constructor is called with EITHER
     * - a valid epsilon value and the maxDenominator set to Integer.MAX_VALUE
     * (that way the maxDenominator has no effect).
     * OR
     * - a valid maxDenominator value and the epsilon value set to zero
     * (that way epsilon only has effect if there is an exact match before
     * the maxDenominator value is reached).
     * </p>
     * <p>
     *
     * It has been done this way so that the same code can be (re)used for both
     * scenarios. However this could be confusing to users if it were part of
     * the public API and this constructor should therefore remain PRIVATE.
     * </p>
     *
     * See JIRA issue ticket MATH-181 for more details:
     *
     * https://issues.apache.org/jira/browse/MATH-181
     *
     * @param value          the double value to convert to a fraction.
     * @param epsilon        maximum error allowed. The resulting fraction is within
     *                       {@code epsilon} of {@code value}, in absolute terms.
     * @param maxDenominator maximum denominator value allowed.
     * @param maxIterations  maximum number of convergents
     * @throws FractionConversionException if the continued fraction failed to
     *                                     converge.
     */
    private Fraction(double value, double epsilon, int maxDenominator, int maxIterations)
            throws FractionConversionException {
        long overflow = Integer.MAX_VALUE;
        double r0 = value;
        long a0 = (long) FastMath.floor(r0);
        if (FastMath.abs(a0) > overflow) {
            throw new FractionConversionException(value, a0, 1l);
        }

        // check for (almost) integer arguments, which should not go to iterations.
        if (FastMath.abs(a0 - value) < epsilon) {
            this.numerator = BigInteger.valueOf(a0);
            this.denominator = BigInteger.ONE;
            return;
        }

        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        long p2 = 0;
        long q2 = 1;

        int n = 0;
        boolean stop = false;
        do {
            ++n;
            double r1 = 1.0 / (r0 - a0);
            long a1 = (long) FastMath.floor(r1);
            p2 = (a1 * p1) + p0;
            q2 = (a1 * q1) + q0;

            if ((FastMath.abs(p2) > overflow) || (FastMath.abs(q2) > overflow)) {
                // in maxDenominator mode, if the last fraction was very close to the actual
                // value
                // q2 may overflow in the next iteration; in this case return the last one.
                if (epsilon == 0.0 && FastMath.abs(q1) < maxDenominator) {
                    break;
                }
                throw new FractionConversionException(value, p2, q2);
            }

            double convergent = (double) p2 / (double) q2;
            if (n < maxIterations && FastMath.abs(convergent - value) > epsilon && q2 < maxDenominator) {
                p0 = p1;
                p1 = p2;
                q0 = q1;
                q1 = q2;
                a0 = a1;
                r0 = r1;
            } else {
                stop = true;
            }
        } while (!stop);

        if (n >= maxIterations) {
            throw new FractionConversionException(value, maxIterations);
        }

        if (q2 < maxDenominator) {
            this.numerator = BigInteger.valueOf(p2);
            this.denominator = BigInteger.valueOf(q2);
        } else {
            this.numerator = BigInteger.valueOf(p1);
            this.denominator = BigInteger.valueOf(q1);
        }

    }

    /**
     * Create a fraction from an int.
     * The fraction is num / 1.
     *
     * @param num The numerator.
     */
    public Fraction(int num) {
        this(num, 1);
    }

    /**
     * Create a fraction from an int.
     * The fraction is num / 1.
     *
     * @param num The numerator.
     */
    public Fraction(BigInteger num) {
        this(num, BigInteger.ONE);
    }

    /**
     * Create a fraction given the numerator and denominator. The fraction is
     * reduced to lowest terms.
     *
     * @param num The numerator.
     * @param den The denominator.
     * @throws MathArithmeticException if the denominator is {@code zero}.
     */
    public Fraction(int num, int den) {
        if (den == 0) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION,
                    num, den);
        }
        if (den < 0) {
            if (num == Integer.MIN_VALUE ||
                    den == Integer.MIN_VALUE) {
                throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION,
                        num, den);
            }
            num = -num;
            den = -den;
        }
        // reduce numerator and denominator by greatest common denominator.
        final int d = ArithmeticUtils.gcd(num, den);
        if (d > 1) {
            num /= d;
            den /= d;
        }

        // move sign to numerator.
        if (den < 0) {
            num = -num;
            den = -den;
        }
        this.numerator = BigInteger.valueOf(num);
        this.denominator = BigInteger.valueOf(den);
    }

    /**
     * Create a fraction given the numerator and denominator. The fraction is
     * reduced to lowest terms.
     *
     * @param num The numerator.
     * @param den The denominator.
     * @throws MathArithmeticException if the denominator is {@code zero}.
     */
    public Fraction(BigInteger num, BigInteger den) {
        if (den.compareTo(BigInteger.ZERO) == 0) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION,
                    num, den);
        }
        if (den.compareTo(BigInteger.ZERO) == -1) {
            num = num.negate();
            den = den.negate();
        }
        // reduce numerator and denominator by greatest common denominator.
        final BigInteger d = num.gcd(den);
        if (d.compareTo(BigInteger.ONE) == 1) {
            num = num.divide(d);
            den = den.divide(d);
        }

        this.numerator = num;
        this.denominator = den;

    }

    /**
     * Returns the absolute value of this fraction.
     *
     * @return the absolute value.
     */
    public Fraction abs() {
        Fraction ret;
        if (numerator.compareTo(BigInteger.ZERO) == 1) {
            ret = this;
        } else {
            ret = negate();
        }
        return ret;
    }

    /**
     * Compares this object to another based on size.
     *
     * @param object the object to compare to
     * @return -1 if this is less than {@code object}, +1 if this is greater
     *         than {@code object}, 0 if they are equal.
     */
    public int compareTo(Fraction object) {
        BigInteger nOd = numerator.multiply(object.denominator);
        BigInteger dOn = denominator.multiply(object.numerator);
        return nOd.compareTo(dOn);
    }

    /**
     * Compares this object to another based on size.
     *
     * @param integer the BigInteger to compare to
     * @return -1 if this is less than {@code integer}, +1 if this is greater
     *         than {@code integer}, 0 if they are equal.
     */
    public int compareTo(BigInteger integer) {
        return compareTo(new Fraction(integer));
    }

    /**
     * Compares this object to another based on size.
     *
     * @param integer the BigInteger to compare to
     * @return -1 if this is less than {@code integer}, +1 if this is greater
     *         than {@code integer}, 0 if they are equal.
     */
    public int compareTo(int integer) {
        return compareTo(new Fraction(integer));
    }

    /**
     * Shorthand to check is this {@code Fraction} is 0.
     *
     * @return True if this {@code Fraction} is 0.
     */
    public boolean isZero() {
        return this.numerator.signum() == 0;
    }

    /**
     * Shorthand to check is this {@code Fraction} is 1.
     *
     * @return True if this {@code Fraction} is 1.
     */
    public boolean isOne() {
        return this.numerator.equals(BigInteger.ONE) && this.denominator.equals(BigInteger.ONE);
    }

    /**
     * Shorthand to check is this {@code Fraction} is positive (strictly).
     *
     * @return True if this {@code Fraction} is greater than 0.
     */
    public boolean isPositive() {
        return this.numerator.signum() == 1;
    }

    /**
     * Shorthand to check is this {@code Fraction} is negative (strictly).
     *
     * @return True if this {@code Fraction} is lesser than 0.
     */
    public boolean isNegative() {
        return this.numerator.signum() == -1;
    }

    /**
     * Shorthand to check is this {@code Fraction} has a 1 as denominator.
     *
     * @return True if this {@code Fraction} denominator is 1.
     */
    public boolean isInteger() {
        return this.denominator.compareTo(BigInteger.ONE) == 0;
    }

    /**
     * Gets the fraction as a {@code double}. This calculates the fraction as
     * the numerator divided by denominator.
     *
     * @return the fraction as a {@code double}
     */
    @Override
    public double doubleValue() {
        return numerator.doubleValue() / denominator.doubleValue();
    }

    /**
     * Test for the equality of two fractions. If the lowest term
     * numerator and denominators are the same for both fractions, the two
     * fractions are considered to be equal.
     *
     * @param other fraction to test for equality to this fraction
     * @return true if two fractions are equal, false if object is
     *         {@code null}, not an instance of {@link Fraction}, or not equal
     *         to this fraction instance.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Fraction) {
            // since fractions are always in lowest terms, numerators and
            // denominators can be compared directly for equality.
            Fraction rhs = (Fraction) other;
            return (numerator.equals(rhs.numerator)) &&
                    (denominator.equals(rhs.denominator));
        }
        return false;
    }

    /**
     * Gets the fraction as a {@code float}. This calculates the fraction as
     * the numerator divided by denominator.
     *
     * @return the fraction as a {@code float}
     */
    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    /**
     * Access the denominator.
     *
     * @return the denominator.
     */
    public BigInteger getDenominator() {
        return denominator;
    }

    /**
     * Access the numerator.
     *
     * @return the numerator.
     */
    public BigInteger getNumerator() {
        return numerator;
    }

    /**
     * Compute de decimal part of the fraction, meaning that {@code this} minus it's
     * decimal part will give the largest integer lower than this. e.g. :
     *
     * <pre>
     *{4}    = 0
     *{3/4}  = 3/4
     *{7/4}  = 3/4
     *{-7/4} = 1/4
     * </pre>
     *
     * @return The decimal part.
     */
    public Fraction getDecimalPart() {
        if (denominator.compareTo(BigInteger.ONE) == 0) {
            return ZERO;
        }
        return subtract(getWholePart());
    }

    /**
     * Get the whole part of this fraction, in other words, get the maximum fraction
     * that is an integer lower than this.
     *
     * @return The floor of this fraction.
     */
    public Fraction getWholePart() {
        return new Fraction(numerator.subtract(numerator.mod(denominator)), denominator);
    }

    /**
     * Get the minimum fraction that is an integer greater than this.
     *
     * @return The ceil of this fraction.
     */
    public Fraction getCeil() {
        return getWholePart().add(1);
    }

    /**
     * Gets a hashCode for the fraction.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return numerator.hashCode() ^ denominator.hashCode();
    }

    /**
     * Gets the fraction as an {@code int}. This returns the whole number part
     * of the fraction.
     *
     * @return the whole number fraction part
     */
    @Override
    public int intValue() {
        return (int) doubleValue();
    }

    /**
     * Gets the fraction as a {@code long}. This returns the whole number part
     * of the fraction.
     *
     * @return the whole number fraction part
     */
    @Override
    public long longValue() {
        return (long) doubleValue();
    }

    /**
     * Return the additive inverse of this fraction.
     *
     * @return the negation of this fraction.
     */
    public Fraction negate() {
        return new Fraction(numerator.negate(), denominator);
    }

    /**
     * Return the multiplicative inverse of this fraction.
     *
     * @return the reciprocal fraction
     */
    public Fraction reciprocal() {
        return new Fraction(denominator, numerator);
    }

    /**
     * <p>
     * Adds the value of this fraction to another, returning the result in reduced
     * form.
     * The algorithm follows Knuth, 4.5.1.
     * </p>
     *
     * @param fraction the fraction to add, must not be {@code null}
     * @return a {@code Fraction} instance with the resulting values
     * @throws NullArgumentException   if the fraction is {@code null}
     * @throws MathArithmeticException if the resulting numerator or denominator
     *                                 exceeds
     *                                 {@code Integer.MAX_VALUE}
     */
    public Fraction add(Fraction fraction) {
        return addSub(fraction, true /* add */);
    }

    /**
     * Add an integer to the fraction.
     *
     * @param i the {@code integer} to add.
     * @return this + i
     */
    public Fraction add(final int i) {
        return add(BigInteger.valueOf(i));
    }

    /**
     * Add an integer to the fraction.
     *
     * @param i the {@code BigInteger} to add.
     * @return this + i
     */
    public Fraction add(final BigInteger i) {
        return new Fraction(numerator.add(denominator.multiply(i)), denominator);
    }

    /**
     * <p>
     * Subtracts the value of another fraction from the value of this one,
     * returning the result in reduced form.
     * </p>
     *
     * @param fraction the fraction to subtract, must not be {@code null}
     * @return a {@code Fraction} instance with the resulting values
     * @throws NullArgumentException   if the fraction is {@code null}
     * @throws MathArithmeticException if the resulting numerator or denominator
     *                                 cannot be represented in an {@code int}.
     */
    public Fraction subtract(Fraction fraction) {
        return addSub(fraction, false /* subtract */);
    }

    /**
     * Subtract an integer from the fraction.
     *
     * @param i the {@code integer} to subtract.
     * @return this - i
     */
    public Fraction subtract(final int i) {
        return subtract(BigInteger.valueOf(i));
    }

    /**
     * Subtract an integer from the fraction.
     *
     * @param i the {@code BigInteger} to subtract.
     * @return this - i
     */
    public Fraction subtract(final BigInteger i) {
        return new Fraction(numerator.subtract(denominator.multiply(i)), denominator);
    }

    /**
     * Implement add and subtract using algorithm described in Knuth 4.5.1.
     *
     * @param fraction the fraction to subtract, must not be {@code null}
     * @param isAdd    true to add, false to subtract
     * @return a {@code Fraction} instance with the resulting values
     * @throws NullArgumentException   if the fraction is {@code null}
     * @throws MathArithmeticException if the resulting numerator or denominator
     *                                 cannot be represented in an {@code int}.
     */
    private Fraction addSub(Fraction fraction, boolean isAdd) {
        if (fraction == null) {
            throw new NullArgumentException(LocalizedFormats.FRACTION);
        }
        // zero is identity for addition.
        if (numerator.compareTo(BigInteger.ZERO) == 0) {
            return isAdd ? fraction : fraction.negate();
        }
        if (fraction.numerator.compareTo(BigInteger.ZERO) == 0) {
            return this;
        }
        // if denominators are randomly distributed, d1 will be 1 about 61%
        // of the time.
        BigInteger gcd = denominator.gcd(fraction.denominator);
        if (gcd.compareTo(BigInteger.ONE) == 0) {
            // result is ( (u*v' +/- u'v) / u'v')
            BigInteger uvp = numerator.multiply(fraction.denominator);
            BigInteger upv = fraction.numerator.multiply(denominator);
            return new Fraction(isAdd ? uvp.add(upv) : uvp.subtract(upv), denominator.multiply(fraction.denominator));
        }

        BigInteger uvp = numerator.multiply(fraction.denominator);
        BigInteger upv = fraction.numerator.multiply(denominator);
        BigInteger num = isAdd ? uvp.add(upv) : uvp.subtract(upv);
        BigInteger den = denominator.multiply(fraction.denominator);

        return new Fraction(num.divide(gcd),
                den.divide(gcd));
    }

    /**
     * <p>
     * Multiplies the value of this fraction by another, returning the
     * result in reduced form.
     * </p>
     *
     * @param fraction the fraction to multiply by, must not be {@code null}
     * @return a {@code Fraction} instance with the resulting values
     * @throws NullArgumentException   if the fraction is {@code null}
     * @throws MathArithmeticException if the resulting numerator or denominator
     *                                 exceeds
     *                                 {@code Integer.MAX_VALUE}
     */
    public Fraction multiply(Fraction fraction) {
        if (fraction == null) {
            throw new NullArgumentException(LocalizedFormats.FRACTION);
        }
        if (numerator.compareTo(BigInteger.ZERO) == 0 || fraction.numerator.compareTo(BigInteger.ZERO) == 0) {
            return ZERO;
        }
        // we can't overflow, juste multiply both, divide by gcd, and swap signs
        BigInteger num = numerator.multiply(fraction.numerator);
        BigInteger den = denominator.multiply(fraction.denominator);
        BigInteger gcd = num.gcd(den);
        num = num.divide(gcd);
        den = den.divide(gcd);

        if (den.compareTo(BigInteger.ZERO) == -1) {
            num = num.negate();
            den = den.negate();
        }

        return new Fraction(num, den);
    }

    /**
     * Multiply the fraction by an integer.
     *
     * @param i the {@code integer} to multiply by.
     * @return this * i
     */
    public Fraction multiply(final int i) {
        return multiply(new Fraction(i));
    }

    /**
     * <p>
     * Divide the value of this fraction by another.
     * </p>
     *
     * @param fraction the fraction to divide by, must not be {@code null}
     * @return a {@code Fraction} instance with the resulting values
     * @throws IllegalArgumentException if the fraction is {@code null}
     * @throws MathArithmeticException  if the fraction to divide by is zero
     * @throws MathArithmeticException  if the resulting numerator or denominator
     *                                  exceeds
     *                                  {@code Integer.MAX_VALUE}
     */
    public Fraction divide(Fraction fraction) {
        if (fraction == null) {
            throw new NullArgumentException(LocalizedFormats.FRACTION);
        }
        if (fraction.numerator.compareTo(BigInteger.ZERO) == 0) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_FRACTION_TO_DIVIDE_BY,
                    fraction.numerator, fraction.denominator);
        }
        return multiply(fraction.reciprocal());
    }

    /**
     * Divide the fraction by an integer.
     *
     * @param i the {@code integer} to divide by.
     * @return this * i
     */
    public Fraction divide(final int i) {
        return divide(new Fraction(i));
    }

    /**
     * <p>
     * Gets the fraction percentage as a {@code double}. This calculates the
     * fraction as the numerator divided by denominator multiplied by 100.
     * </p>
     *
     * @return the fraction percentage as a {@code double}.
     */
    public double percentageValue() {
        return 100 * doubleValue();
    }

    /**
     * <p>
     * Creates a {@code Fraction} instance with the 2 parts
     * of a fraction Y/Z.
     * </p>
     *
     * <p>
     * Any negative signs are resolved to be on the numerator.
     * </p>
     *
     * @param numerator   the numerator, for example the three in 'three sevenths'
     * @param denominator the denominator, for example the seven in 'three sevenths'
     * @return a new fraction instance, with the numerator and denominator reduced
     * @throws MathArithmeticException if the denominator is {@code zero}.
     */
    public static Fraction getReducedFraction(int numerator, int denominator) {
        if (denominator == 0) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION,
                    numerator, denominator);
        }
        if (numerator == 0) {
            return ZERO; // normalize zero.
        }
        // allow 2^k/-2^31 as a valid fraction (where k>0)
        if (denominator == Integer.MIN_VALUE && (numerator & 1) == 0) {
            numerator /= 2;
            denominator /= 2;
        }
        if (denominator < 0) {
            if (numerator == Integer.MIN_VALUE ||
                    denominator == Integer.MIN_VALUE) {
                throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION,
                        numerator, denominator);
            }
            numerator = -numerator;
            denominator = -denominator;
        }
        // simplify fraction.
        int gcd = ArithmeticUtils.gcd(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;
        return new Fraction(numerator, denominator);
    }

    /**
     * <p>
     * Returns the {@code String} representing this fraction, ie
     * "num / dem" or just "num" if the denominator is one.
     * </p>
     *
     * @return a string representation of the fraction.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String str = null;
        if (denominator.compareTo(BigInteger.ONE) == 0) {
            str = numerator.toString();
        } else if (numerator.compareTo(BigInteger.ZERO) == 0) {
            str = "0";
        } else {
            str = numerator.toString() + " / " + denominator.toString();
        }
        return str;
    }
}
