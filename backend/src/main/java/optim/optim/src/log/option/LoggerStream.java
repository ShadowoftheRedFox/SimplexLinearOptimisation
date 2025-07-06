package optim.optim.src.log.option;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Different kind of print stream for the Logger to print into. Custom print
 * stream can be wrapped into this class.
 */
public class LoggerStream implements LoggerOption<PrintStream> {
    /** Will log on {@code System.out}. */
    public static final LoggerStream OUT = new LoggerStream(System.out);

    /** Will log on {@code System.err}. */
    public static final LoggerStream ERR = new LoggerStream(System.err);

    /** Saved print stream. It will be printed onto. */
    private final PrintStream stream;

    /**
     * Create a new loggerType with a custom {@code PrintStream}.
     *
     * @param stream The print stream.
     * @throws NullPointerException if stream is null.
     */
    private LoggerStream(PrintStream stream) {
        Objects.requireNonNull(stream);
        this.stream = stream;
    }

    /**
     * Getter for {@link #stream}.
     *
     * @return The stream.
     */
    public PrintStream value() {
        return stream;
    }
}
