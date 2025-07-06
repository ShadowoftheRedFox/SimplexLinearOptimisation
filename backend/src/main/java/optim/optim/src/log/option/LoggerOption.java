package optim.optim.src.log.option;

/**
 * Base interface to passe options to the Logger.
 *
 * @param T type of value expected from {@link #value()}.
 */
public interface LoggerOption<T> {
    /**
     * Get the value saved in the logger option.
     *
     * @return The value.
     */
    public T value();

    /** The "Escape", starts all escape sequences. */
    public static String ESC = "\u001B[";
}
