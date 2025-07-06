package optim.optim.src;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

import optim.optim.src.log.Logger;

/**
 * Load the config file and store the values staticly.
 */
public abstract class Config {
    /** Default constructor. */
    protected Config() {
    }

    /** Name of the config file loaded in the maven resources folder. */
    public static final String CONFIG_FILE_NAME = "application.config";

    /** If the config has already been loaded before. */
    private static boolean isLoaded = false;

    /**
     * Load the config, and store it internally.
     *
     * @return true if the config has been loaded correctly. False otherwise.
     */
    public static boolean load() {
        // config already loaded
        if (isLoaded) {
            return true;
        }
        return forceLoad();
    }

    /**
     * Force load the config. It will re read the configuration file(s) and update
     * ths class content.
     * <p>
     * <b>WARNING</b> Old references may be broken.
     *
     * @return true if the config has been loaded correctly. False otherwise.
     */
    public static boolean forceLoad() {
        boolean res = true;
        // initial capacity is deduced from the number of fields in the config file
        // even if we could "automate" it using Config.class.getDeclaredFields().length
        // minus the the two first properties, but it's already too overkill to input
        // the initial capacity
        Properties prop = new Properties(7);
        try {
            // get the file from the resources folder
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream(CONFIG_FILE_NAME);
            prop.load(is);
            isLoaded = true;
        } catch (Exception e) {
            Logger.error("Couldn't load config file! Message: " + e.getMessage());
            res = false;
            // in case of catch, values will be defaulted
        }

        Debugger_Enabled = Boolean.valueOf((String) prop.getProperty(
                "debugger.enabled",
                "false"));

        UnitTest_Simplex_Enabled = Boolean.valueOf((String) prop.getProperty(
                "unittests.simplex.enabled",
                "true"));

        UnitTest_Simplex_ContinueWithoutOutput = Boolean.valueOf((String) prop.getProperty(
                "unittests.simplex.continue_without_output",
                "false"));

        UnitTest_Simplex_TestsFolder = (String) prop.getProperty(
                "unittests.simplex.tests_folder",
                "/tests/unitTests/");

        UnitTest_Simplex_RelaxTestsFolder = (String) prop.getProperty(
                "unittests.simplex.relax_tests_folder",
                "/tests/relaxUnitTests/");

        UnitTest_Simplex_TestsToRun = (String) prop.getProperty(
                "unittests.simplex.tests_torun",
                "toRun");

        Simplex_MaxCoefficients = parseInt(prop.getProperty(
                "simplex.max_coefficients", "50"),
                50,
                (v) -> v > 0);

        Simplex_MaxConstraints = parseInt(prop.getProperty(
                "simplex.max_coefficients", "50"),
                50,
                (v) -> v > 0);

        return res;
    }

    // #region Debugger

    /**
     * If debug messages should be printed.
     * Default: false
     */
    private static boolean Debugger_Enabled;

    /**
     * If debug messages should be printed.
     *
     * @return Default: false
     */
    public static boolean Debugger_Enabled() {
        return Debugger_Enabled;
    };

    // #endregion

    // #region Unit tests

    /**
     * If the tests should be performed at all.
     * Default: true
     */
    private static boolean UnitTest_Simplex_Enabled;

    /**
     * If the tests should be performed at all.
     *
     * @return Default: true
     */
    public static boolean UnitTest_Simplex_Enabled() {
        return UnitTest_Simplex_Enabled;
    }

    /**
     * Stop before the test when loading unit test, its expected output file isn't
     * found.
     * Default: false
     */
    private static boolean UnitTest_Simplex_ContinueWithoutOutput;

    /**
     * If the tests should stop if not all tests have an expected output.
     *
     * @return Default: false
     */
    public static boolean UnitTest_Simplex_ContinueWithoutOutput() {
        return UnitTest_Simplex_ContinueWithoutOutput;
    }

    /**
     * Path to the folder containing the simplex unit tests files.
     * Default: /tests/unitTests/
     */
    private static String UnitTest_Simplex_TestsFolder;

    /**
     * Path to the folder containing the simplex unit tests files.
     *
     * @return Default: /tests/unitTests/
     */
    public static String UnitTest_Simplex_TestsFolder() {
        return UnitTest_Simplex_TestsFolder;
    }

    /**
     * Path to the folder containing the simplex relaxed unit tests files.
     * Default: /tests/relaxUnitTests/
     */
    private static String UnitTest_Simplex_RelaxTestsFolder;

    /**
     * Path to the folder containing the simplex relaxed unit tests files.
     *
     * @return Default: /tests/relaxUnitTests/
     */
    public static String UnitTest_Simplex_RelaxTestsFolder() {
        return UnitTest_Simplex_RelaxTestsFolder;
    }

    /**
     * Name of the file containing a list for file name to test. The files will be
     * searched in the {@link #UnitTest_Simplex_TestsFolder()} folder.
     * Default: toRun
     */
    private static String UnitTest_Simplex_TestsToRun;

    /**
     * Name of the file containing a list for file name to test. The files will be
     * searched in the {@link #UnitTest_Simplex_TestsFolder()} folder.
     *
     * @return Default: toRun
     */
    public static String UnitTest_Simplex_TestsToRun() {
        return UnitTest_Simplex_TestsToRun;
    }

    // #endregion

    // #region Simplex

    /**
     * The maximum number of coefficients accepted. This is mainly a performance and
     * memory issue.
     * Default: 50
     */
    private static int Simplex_MaxCoefficients;

    /**
     * The maximum number of coefficients accepted. This is mainly a performance and
     * memory issue.
     *
     * @return Default: 50
     */
    public static int Simplex_MaxCoefficients() {
        return Simplex_MaxCoefficients;
    }

    /**
     * The maximum number of constraints accepted. This is mainly a performance
     * and memory issue.
     * Default: 50
     */
    private static int Simplex_MaxConstraints;

    /**
     * The maximum number of constraints accepted. This is mainly a performance
     * and memory issue.
     *
     * @return Default: 50
     */
    public static int Simplex_MaxConstraints() {
        return Simplex_MaxConstraints;
    }

    // #endregion

    /**
     * Check if the config has been loaded.
     *
     * @return True if the config has been loaded.
     */
    public static boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Convert value into an int. If the value is not parsable, then the default
     * value is used instead.
     *
     * @param value        The value to parse.
     * @param defaultValue The default value.
     * @return The parsed value, or the default value.
     */
    public static int parseInt(Object value, int defaultValue) {
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convert value into an int, and apply a condition on the result. If the value
     * is not parsable, or the condition return false with the parsed value, then
     * the default value is used instead.
     * <p>
     * <b>Important:</b> Even if the {@code defaultValue} applied to the
     * {@code condition} evaluate to false, it will return the default value.
     *
     * @param value        The value to parse.
     * @param defaultValue The default value.
     * @param condition    The condition to apply on the parsed value.
     * @return The parsed value, or the default value.
     * @throws NullPointerException if condition is null.
     */
    public static int parseInt(Object value, int defaultValue, Function<Integer, Boolean> condition)
            throws NullPointerException {
        Objects.requireNonNull(condition, "condition required");
        int response = parseInt(value, defaultValue);
        Boolean evaluation = condition.apply(response);
        if (evaluation != null && evaluation) {
            return response;
        }
        return defaultValue;
    }
}
