package optim.optim.src.error;

/**
 * To be invoked when a constraint doesn't match the expected format required.
 */
public class MalformedConstraintError extends Error {
    /** {@inheritDoc} */
    public MalformedConstraintError() {
    }

    /** {@inheritDoc} */
    public MalformedConstraintError(String message) {
        super(message);
    }

    /** {@inheritDoc} */
    public MalformedConstraintError(String message, Throwable cause) {
        super(message, cause);
    }

    /** {@inheritDoc} */
    public MalformedConstraintError(Throwable cause) {
        super(cause);
    }
}
