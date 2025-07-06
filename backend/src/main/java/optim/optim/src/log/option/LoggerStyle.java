package optim.optim.src.log.option;

/**
 * Some style may not be implemented on your system, or may do a different
 * effect. Though, the ones here are either standard or widely implemented.
 */
public enum LoggerStyle implements LoggerOption<String> {
    /** Bold or increased text intensity. */
    BOLD("1"),
    /** Bold or decreased or dim text intensity. */
    FAINT("2"),
    /** Normal text intensity. */
    NORMAL_INTENSITY("22"),

    /** Italic text. */
    ITALIC("3"),
    /** Reset italic text. */
    NOT_ITALIC("23"),

    /** Underline text. */
    UNDERLINE("4"),
    /** Doubly underlined text. */
    DOUBLE_UNDERLINE("21"),
    /** Reset underlined text. */
    NOT_UNDERLINE("24"),

    /** Striked text. */
    STRIKE("9"),
    /** REset striked text. */
    NOT_STRIKE("29"),

    /** Framed text. */
    FRAME("51"),
    /** Encircled text. */
    ENCIRCLE("52"),
    /** Reset framed and encricled text. */
    NOT_ENCIRCLE("54"),

    /** Overlined text. */
    OVERLINE("53"),
    /** Reset overlined text. */
    NOT_OVERLINE("55"),

    /** Reset all styles. */
    NORMAL("0");

    private final String value;

    /**
     * Contructor when getting the enum.
     *
     * @param value The value of the enum.
     */
    private LoggerStyle(String value) {
        this.value = value;
    }

    /**
     * Getter for {@link #value}.
     *
     * @return The value as an escape sequence.
     */
    public String value() {
        return LoggerOption.ESC + value + "m";
    }

    @Override
    public String toString() {
        return value();
    }
}
