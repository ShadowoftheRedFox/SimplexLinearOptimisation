package optim.optim.src.log;

import java.io.PrintStream;
import java.util.Arrays;

import optim.optim.src.Config;
import optim.optim.src.log.option.LoggerColor;
import optim.optim.src.log.option.LoggerOption;
import optim.optim.src.log.option.LoggerStream;
import optim.optim.src.log.option.LoggerStyle;
import optim.optim.src.simplex.SimplexTable;

/**
 * An aggregate of functions to print formated messages, with or without applied
 * styles.
 */
public abstract class Logger {
    /** Default constructor. */
    protected Logger() {
    }
    // TODO maybe add a log file where to log each call

    // #region Shorthand
    /**
     * Print a message with an error prefix.
     *
     * @param message Message to print.
     */
    public static void error(String message) {
        error(message, false);
    }

    /**
     * Print a message with a warning prefix.
     *
     * @param message Message to print.
     */
    public static void warn(String message) {
        warn(message, false);
    }

    /**
     * Print a message with an info prefix. By default, if
     * {@code Config.Debugger_Enabled()} is false, nothing will be printed.
     *
     * @param message Message to print.
     */
    public static void info(String message) {
        info(message, true);
    }

    /**
     * Print a message with a trace prefix. By default, if
     * {@code Config.Debugger_Enabled()} is false, nothing will be printed.
     *
     * @param message Message to print.
     */
    public static void trace(String message) {
        trace(message, true);
    }

    /**
     * Print a message with a failed prefix. By default, if
     * {@code Config.Debugger_Enabled()} is false, nothing will be printed.
     *
     * @param message Message to print.
     */
    public static void failed(String message) {
        failed(message, true);
    }

    /**
     * Print a message with a passed prefix. By default, if
     * {@code Config.Debugger_Enabled()} is false, nothing will be printed.
     *
     * @param message Message to print.
     */
    public static void passed(String message) {
        passed(message, true);
    }

    /**
     * Print a message with an error prefix.
     *
     * @param message   Message to print.
     * @param debugOnly If the message should only be printed if
     *                  {@code Config.Debugger_Enabled()} is true.
     */
    public static void error(String message, boolean debugOnly) {
        if (!debugOnly || Config.Debugger_Enabled()) {
            log("ERROR", message, LoggerStyle.BOLD, LoggerColor.RED);
        }
    }

    /**
     * Print a message with a warning prefix.
     *
     * @param message   Message to print.
     * @param debugOnly If the message should only be printed if
     *                  {@code Config.Debugger_Enabled()} is true.
     */
    public static void warn(String message, boolean debugOnly) {
        if (!debugOnly || Config.Debugger_Enabled()) {
            log("WARN", message, LoggerStyle.BOLD, LoggerColor.YELLOW);
        }
    }

    /**
     * Print a message with an info prefix.
     *
     * @param message   Message to print.
     * @param debugOnly If the message should only be printed if
     *                  {@code Config.Debugger_Enabled()} is true.
     */
    public static void info(String message, boolean debugOnly) {
        if (!debugOnly || Config.Debugger_Enabled()) {
            log("INFO", message, LoggerStyle.BOLD, LoggerColor.CYAN);
        }
    }

    /**
     * Print a message with a trace prefix.
     *
     * @param message   Message to print.
     * @param debugOnly If the message should only be printed if
     *                  {@code Config.Debugger_Enabled()} is true.
     */
    public static void trace(String message, boolean debugOnly) {
        if (!debugOnly || Config.Debugger_Enabled()) {
            log("TRACE", message, LoggerStyle.BOLD, LoggerColor.WHITE);
        }
    }

    /**
     * Print a message with a failed prefix.
     *
     * @param message   Message to print.
     * @param debugOnly If the message should only be printed if
     *                  {@code Config.Debugger_Enabled()} is true.
     */
    public static void failed(String message, boolean debugOnly) {
        if (!debugOnly || Config.Debugger_Enabled()) {
            log("FAILED", message, LoggerStyle.BOLD, LoggerColor.MAGENTA);
        }
    }

    /**
     * Print a message with a passed prefix.
     *
     * @param message   Message to print.
     * @param debugOnly If the message should only be printed if
     *                  {@code Config.Debugger_Enabled()} is true.
     */
    public static void passed(String message, boolean debugOnly) {
        if (!debugOnly || Config.Debugger_Enabled()) {
            log("PASSED", message, LoggerStyle.BOLD, LoggerColor.GREEN);
        }
    }
    // #endregion

    // #region Log
    /**
     * Print a message in {@code System.out}.
     *
     * @param message The message to print.
     */
    public static void log(String message) {
        log(null, message, LoggerStream.OUT);
    }

    /**
     * Print a message in with the given options.
     *
     * @param message The message to print.
     * @param options Different options to change the {@code PrintStream} of the
     *                style of the message.
     */
    @SuppressWarnings("rawtypes")
    public static void log(String message, LoggerOption... options) {
        log(null, message, options);
    }

    /**
     * Print a message with a prefix.
     *
     * @param prefix  A prefix before the message.
     * @param message The message to print.
     */
    public static void log(String prefix, String message) {
        log(prefix, message, LoggerStream.OUT);
    }

    /**
     * Print a message with a prefix, and the given options.
     * The default {@code PrintStream} is {@code System.out}.
     * If no message and no prefix are provided, nothing happens.
     *
     * @param prefix  A prefix of the message. If prefix is not null and not blank,
     *                the style will only be applied on the prefix. If prefix is
     *                null, the style is applied on the whole message.
     * @param message A message to print.
     * @param options Options, applied in order:
     *                <ul>
     *                <li>{@link LoggerColor}: Color of the prefix if provided,
     *                otherwise color of the message. the message.</li>
     *                <li>{@link LoggerStyle}: Style of the text, such as bold,
     *                underline, etc...</li>
     *                <li>{@link LoggerStream}: Stream where to print the
     *                message.</li>
     *                </ul>
     */
    @SuppressWarnings("rawtypes")
    public static void log(String prefix, String message, LoggerOption... options) {
        // nothing to print
        if ((message == null || message.isBlank()) &&
                (prefix == null || prefix.isBlank())) {
            return;
        }

        if (message == null) {
            message = "";
        }
        if (options == null) {
            options = new LoggerOption[] {};
        }

        // the stram where the message will be printed
        LoggerStream outputStream = LoggerStream.OUT;

        // fetch the styles, and pass through all options
        String styles = "";
        for (LoggerOption option : options) {
            if (option instanceof LoggerStream) {
                outputStream = (LoggerStream) option;
            } else if (option instanceof LoggerColor) {
                styles += ((LoggerColor) option).value();
            } else if (option instanceof LoggerStyle) {
                styles += ((LoggerStyle) option).value();
            }
        }

        // add the styles to the prefix
        if (prefix == null) {
            // if prefix is null, apply style to the whole message
            prefix = styles;
            message += LoggerColor.NORMAL.value();
        } else {
            prefix = "[" + styles + prefix + LoggerColor.NORMAL.value() + "] ";
        }

        outputStream.value().println(prefix + message);
    }

    // #endregion

    // #region Debug
    /**
     * Print a message in {@code System.out}, only if
     * {@code Config.Debugger_Enabled()} is true.
     *
     * @param message The message to print.
     */
    public static void debug(String message) {
        if (Config.Debugger_Enabled()) {
            debug("DEBUG", message);
        }
    }

    /**
     * Print a message in with the given options, only if
     * {@code Config.Debugger_Enabled()} is true.
     *
     * @param message The message to print.
     * @param options Different options to change the {@code PrintStream} of the
     *                style of the message.
     */
    @SuppressWarnings("rawtypes")
    public static void debug(String message, LoggerOption... options) {
        if (Config.Debugger_Enabled()) {
            log("DEBUG", message, options);
        }
    }

    /**
     * Print a message with a prefix, only if {@code Config.Debugger_Enabled()} is
     * true.
     *
     * @param prefix  A prefix before the message.
     * @param message The message to print.
     */
    public static void debug(String prefix, String message) {
        if (Config.Debugger_Enabled()) {
            log(prefix, message, LoggerStream.OUT,
                    LoggerColor.RED,
                    LoggerStyle.UNDERLINE,
                    LoggerStyle.BOLD);
        }
    }

    /**
     * Print a message with a prefix, and the given options, only if
     * {@code Config.Debugger_Enabled()} is true.
     * The default {@code PrintStream} is {@code System.out}.
     * If no message and no prefix are provided, nothing happens.
     *
     * @param prefix  A prefix of the message. If prefix is not null and not blank,
     *                the style will only be applied on the prefix. If prefix is
     *                null, the style is applied on the whole message.
     * @param message A message to print.
     * @param options Options, applied in order:
     *                <ul>
     *                <li>{@link LoggerColor}: Color of the prefix if provided,
     *                otherwise color of the message. the message.</li>
     *                <li>{@link LoggerStyle}: Style of the text, such as bold,
     *                underline, etc...</li>
     *                <li>{@link LoggerStream}: Stream where to print the
     *                message.</li>
     *                </ul>
     */
    @SuppressWarnings("rawtypes")
    public static void debug(String prefix, String message, LoggerOption... options) {
        if (Config.Debugger_Enabled()) {
            log(prefix, message, options);
        }
    }

    /**
     * Print the table, only if {@code Config.Debugger_Enabled()} is true.
     *
     * @param table Current table.
     */
    public static void debugTable(SimplexTable table) {
        debugStep(null, null, table, true);
    }

    /**
     * Print the table.
     *
     * @param table     Current table.
     * @param debugOnly If the message should only be printed if
     *                  {@code Config.Debugger_Enabled()} is true.
     */
    public static void debugTable(SimplexTable table, boolean debugOnly) {
        debugStep(null, null, table, debugOnly);
    }

    /**
     * Print the table and which pivot is being used, only if
     * {@code Config.Debugger_Enabled()} is true.
     *
     * @param inCol  Pivot column.
     * @param outRow Pivot row.
     * @param table  Current table.
     */
    public static void debugStep(Integer inCol, Integer outRow, SimplexTable table) {
        debugStep(inCol, outRow, table, true);
    }

    /**
     * Print the table and which pivot is being used.
     *
     * @param inCol     Pivot column.
     * @param outRow    Pivot row.
     * @param table     Current table.
     * @param debugOnly If the message should only be printed if
     *                  {@code Config.Debugger_Enabled()} is true.
     */
    public static void debugStep(Integer inCol, Integer outRow, SimplexTable table, boolean debugOnly) {
        if (debugOnly && !Config.Debugger_Enabled()) {
            return;
        }

        if (table == null) {
            trace("Table is null", debugOnly);
        } else {
            String message = (inCol != null || outRow != null
                    ? "Pivoting col " + inCol + " with row " + outRow + "\n"
                    : "") +
                    "Table: " + table.getWidth() + "x" + table.getHeight() +
                    " | Basic: " + table.getBasicVariables() + "\n";

            message += alignTable(table.getSimplexTable());
            trace(message, debugOnly);
        }
    }

    // #endregion

    // #region Format
    /**
     * Given a 2D array, will return a multiline response with the array content
     * aligned by column.
     * <p>
     * The default separator {@code " | "} is used (vertical bar between one space
     * on each sides). There is no sperator at the end of each line.
     * <p>
     * If table is null, "null" is returned. If the array length is {@code 0},
     * "empty" is returned. If {@code table[i]} is null, "null" is printed instead
     * of the whole line.
     *
     * @param table A 2D array.
     * @return Formated, aligned table.
     */
    public static String alignTable(Object[][] table) {
        return alignTable(table, " | ");
    }

    /**
     * Given a 2D array, will return a multiline response with the array content
     * aligned by column.
     * <p>
     * The default separator is {@code " | "} (vertical bar between one space
     * on each sides). There is no sperator at the end of each line.
     * <p>
     * If table is null, {@code "null"} is returned. If the array length is 0,
     * {@code "empty"} is returned. If {@code table[i]} is null, {@code "null"} is
     * printed instead of the whole line.
     *
     * @param table     A 2D array.
     * @param separator A separator between columns. If null, the default is used
     *                  instead.
     * @return Formated and aligned table.
     */
    public static String alignTable(final Object[][] table, String separator) {
        if (table == null) {
            return "null";
        }

        final int height = table.length;
        if (height == 0) {
            return "empty";
        }

        if (separator == null) {
            separator = " | ";
        }

        // get the max width, even if some items are null
        int width = 0;
        for (int i = 0; i < height; i++) {
            if (table[i] != null && table[i].length > width) {
                width = table[i].length;
            }
        }

        // usefull for debugging: aligned columns
        // create an array of int for each column gap
        int[] maxColWidth = new int[width - 1];
        Arrays.fill(maxColWidth, 1);
        // for each column, get the max column width
        for (int i = 0; i < height; i++) {
            if (table[i] == null) {
                continue;
            }
            for (int j = 0; j < width - 1; j++) {
                int entryLength = String.valueOf(table[i][j]).length();
                if (maxColWidth[j] < entryLength) {
                    maxColWidth[j] = entryLength;
                }
            }
        }

        String response = "";

        // when printing each entry, look at the max column length, and add the
        // necessary space to reach said max width
        for (int i = 0; i < height; i++) {
            if (table[i] == null) {
                response += "null\n";
                continue;
            }
            for (int j = 0; j < width; j++) {
                final String entry = String.valueOf(table[i][j]);
                response += entry;
                // add spaces if we're not at the last col
                if (j != maxColWidth.length) {
                    int l = entry.length();
                    // make l a full width
                    final int f = (maxColWidth[j] - l) % maxColWidth[j];
                    for (int k = 0; k < f; k++) {
                        response += " ";
                    }
                    response += separator;
                }
            }
            response += "\n";
        }

        return response;
    }

    /**
     * Apply directly the styles on nothing, but does not print a new line, and does
     * not revert the style changes at the end.
     *
     * @param options Options, applied in order:
     *                <ul>
     *                <li>{@link LoggerColor}: Color of the prefix if provided,
     *                otherwise color of the message. the message.</li>
     *                <li>{@link LoggerStyle}: Style of the text, such as bold,
     *                underline, etc...</li>
     *                <li>{@link LoggerStream}: Stream where to print the
     *                message.</li>
     *                </ul>
     */
    @SuppressWarnings("rawtypes")
    public static void applyStyle(LoggerOption... options) {
        if (options == null) {
            options = new LoggerOption[] {};
        }
        // the stram where the message will be printed
        LoggerStream outputStream = LoggerStream.OUT;

        // fetch the styles, and pass through all options
        String styles = "";
        for (LoggerOption option : options) {
            if (option instanceof LoggerStream) {
                outputStream = (LoggerStream) option;
            } else if (option instanceof LoggerColor) {
                styles += ((LoggerColor) option).value();
            } else if (option instanceof LoggerStyle) {
                styles += ((LoggerStyle) option).value();
            }
        }

        outputStream.value().print(styles);
    }

    /**
     * Revert all changes made on the text style, without a new line.
     */
    public static void clearStyle() {
        clearStyle(null);
    }

    /**
     * Revert all changes made on the text style on the given stream, without a new
     * line.
     *
     * @param stream Stream to reset the chanegs on.
     */
    public static void clearStyle(PrintStream stream) {
        PrintStream outputStream = LoggerStream.OUT.value();
        if (stream != null) {
            outputStream = stream;
        }
        outputStream.print(LoggerColor.NORMAL.value());
    }
    // #endregion
}
