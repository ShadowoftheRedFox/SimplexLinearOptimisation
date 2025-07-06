package optim.optim.src.simplex.data;

/** Whether the solution for the simplex method can have negative values. */
public class NonNegativeValues implements SimplexData {
    /** Value stored. */
    private boolean nonNegative;

    /**
     * Default constructor.
     *
     * @param nonNegative Value to store.
     */
    public NonNegativeValues(boolean nonNegative) {
        this.nonNegative = nonNegative;
    }

    /**
     * Getter for {@link #nonNegative}.
     *
     * @return Get the stored value.
     */
    public final boolean getNonNegative() {
        return nonNegative;
    }
}
