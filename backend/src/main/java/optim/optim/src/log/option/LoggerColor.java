package optim.optim.src.log.option;

/**
 * Some style may not be implemented on your system, or may do a different
 * effect.
 * Bright colors are not standard.
 */
public enum LoggerColor implements LoggerOption<String> {
    /** Reset all styles. */
    NORMAL("0"),
    /** Default foreground color. */
    DEFAULT_FOREGROUND("39"),
    /** Default background color. */
    DEFAULT_BACKGROUND("49"),

    /** Red text color. */
    RED("31"),
    /** Green text color. */
    GREEN("32"),
    /** Yellow text color. */
    YELLOW("33"),
    /** Blue text color. */
    BLUE("34"),
    /** Magenta text color. */
    MAGENTA("35"),
    /** Cyan text color. */
    CYAN("36"),
    /** White text color. */
    WHITE("37"),
    /** Gray, or bright black, text color. */
    GRAY("37"),

    /** Bright red text color. */
    BRIGHT_RED("91"),
    /** Bright green text color. */
    BRIGHT_GREEN("92"),
    /** Bright yellow text color. */
    BRIGHT_YELLOW("93"),
    /** Bright blue text color. */
    BRIGHT_BLUE("94"),
    /** Bright magenta text color. */
    BRIGHT_MAGENTA("95"),
    /** Bright cyan text color. */
    BRIGHT_CYAN("96"),
    /** Bright white text color. */
    BRIGHT_WHITE("97"),

    /** Black background color. */
    BACKGROUND_BLACK("40"),
    /** Red background color. */
    BACKGROUND_RED("41"),
    /** Green background color. */
    BACKGROUND_GREEN("42"),
    /** Yellow background color. */
    BACKGROUND_YELLOW("43"),
    /** Blue background color. */
    BACKGROUND_BLUE("44"),
    /** Magenta background color. */
    BACKGROUND_MAGENTA("45"),
    /** Cyan background color. */
    BACKGROUND_CYAN("46"),
    /** White background color. */
    BACKGROUND_WHITE("47"),
    /** Gray, bright black, background color. */
    BACKGROUND_GRAY("100"),

    /** Bright red background color. */
    BACKGROUND_BRIGHT_RED("101"),
    /** Bright green background color. */
    BACKGROUND_BRIGHT_GREEN("102"),
    /** Bright yellow background color. */
    BACKGROUND_BRIGHT_YELLOW("103"),
    /** Bright blue background color. */
    BACKGROUND_BRIGHT_BLUE("104"),
    /** Bright magenta background color. */
    BACKGROUND_BRIGHT_MAGENTA("105"),
    /** Bright cyan background color. */
    BACKGROUND_BRIGHT_CYAN("106"),
    /** Bright white background color. */
    BACKGROUND_BRIGHT_WHITE("107");

    private final String value;

    /**
     * Contructor when getting the enum.
     *
     * @param value The value of the enum.
     */
    private LoggerColor(String value) {
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
