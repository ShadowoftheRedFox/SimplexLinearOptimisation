package simplex.simplex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.UnboundedSolutionException;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import optim.optim.controller.body.SimplexForm;
import optim.optim.response.SimplexResponse;
import optim.optim.response.SimplexResponse.Feasibility;
import optim.optim.service.SimplexService;
import optim.optim.src.Config;
import optim.optim.src.log.Logger;
import optim.optim.src.log.option.LoggerColor;
import optim.optim.src.log.option.LoggerStyle;
import optim.optim.src.simplex.Fraction;
import optim.optim.src.simplex.PointFractionPair;
import optim.optim.src.simplex.SimplexSolver;
import optim.optim.src.simplex.data.Constraint;
import optim.optim.src.simplex.data.ConstraintSet;
import optim.optim.src.simplex.data.GoalType;
import optim.optim.src.simplex.data.IntegerMethod;
import optim.optim.src.simplex.data.NonNegativeValues;
import optim.optim.src.simplex.data.ObjectiveFunction;
import optim.optim.src.simplex.data.PivotSelectionRule;

public class SimplexTests {
    /**
     * Manual test for benchmarking the simplex implementation with the Klee Minty
     * cube.
     *
     * @param args Args.
     */
    public static void main(String[] args) {
        Config.load();
        SimplexTests test = new SimplexTests();
        Logger.info("Relaxed:", false);
        test.runRelaxUnitTests();
        Logger.info("Integer:", false);
        test.runIntegerUnitTests();
        // test.benchmark();
    }

    /**
     * Manual test for benchmarking the simplex implementation with the Klee Minty
     * cube.
     */
    public void benchmark() {
        SimplexTests test = new SimplexTests();
        int dimension = 50;
        for (int i = 1; i <= dimension; i++) {
            long start = System.currentTimeMillis();
            test.kleeMintyCube(i);
            long end = System.currentTimeMillis();
            System.out.println("Time taken for " + i + " dimensions: " + (end - start) + "ms");
        }
        /**
         * TODO Make those times better!
         * -> Pivot selection rule: Random
         * Time taken for 1 dimensions: 61ms
         * Time taken for 2 dimensions: 1ms
         * Time taken for 3 dimensions: 2ms
         * Time taken for 4 dimensions: 2ms
         * Time taken for 5 dimensions: 3ms
         * Time taken for 6 dimensions: 4ms
         * Time taken for 7 dimensions: 6ms
         * Time taken for 8 dimensions: 27ms
         * Time taken for 9 dimensions: 29ms
         * Time taken for 10 dimensions: 38ms
         * Time taken for 11 dimensions: 77ms
         * Time taken for 12 dimensions: 136ms
         * Time taken for 13 dimensions: 226ms
         * Time taken for 14 dimensions: 677ms
         * Time taken for 15 dimensions: 1137ms
         * Time taken for 16 dimensions: 1954ms
         * Time taken for 17 dimensions: 4442ms
         * Time taken for 18 dimensions: 10367ms
         * 19: heap out of space
         */
    }

    /**
     * Test from the data from /tests/man.txt.
     */
    @Test
    public void simplexBasic()
            throws TooManyIterationsException, NoFeasibleSolutionException, UnboundedSolutionException {
        // TODO add the basic/nonbasic data lines once implemented
        SimplexSolver solver = new SimplexSolver(100);

        final Fraction[] objectiveCoefs = new Fraction[] { new Fraction(3), new Fraction(5), new Fraction(4) };
        final Fraction[][] constraintsCoefs = new Fraction[][] {
                { new Fraction(2), new Fraction(3), new Fraction(0) },
                { new Fraction(0), new Fraction(2), new Fraction(5) },
                { new Fraction(3), new Fraction(2), new Fraction(4) }
        };
        final Fraction[] constants = new Fraction[] { new Fraction(8), new Fraction(10), new Fraction(15) };
        final Fraction objectiveConstant = new Fraction(0);

        ObjectiveFunction f = new ObjectiveFunction(objectiveCoefs, objectiveConstant);

        Collection<Constraint> constraints = new ArrayList<Constraint>();
        for (int i = 0; i < constraintsCoefs.length; i++) {
            constraints.add(new Constraint(constraintsCoefs[i], Relationship.LEQ, constants[i]));
        }

        PointFractionPair optSolution = solver.solve(
                f,
                new ConstraintSet(constraints),
                GoalType.MAXIMIZE,
                new NonNegativeValues(true));

        assertEquals(new Fraction(765, 41), optSolution.getValue());
        assertEquals(3, optSolution.getPoint().length);
        assertEquals(new Fraction(89, 41), optSolution.getPoint()[0]);
        assertEquals(new Fraction(50, 41), optSolution.getPoint()[1]);
        assertEquals(new Fraction(62, 41), optSolution.getPoint()[2]);
    }

    /**
     * Run the Klee-Minty cube to see if we don't loop infinitely, and that we're
     * somewhat efficient.
     *
     * @param dimension The number of dimensions of the cube. We expect to travel
     *                  2^dimenion vertices.
     * @see https://en.wikipedia.org/wiki/Klee%E2%80%93Minty_cube
     */
    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 5, 10 })
    public void kleeMintyCube(int dimensions) {
        if (dimensions < 1) {
            throw new IllegalArgumentException("dimensions lower than 1");
        }

        ArrayList<Constraint> constraints = new ArrayList<>();

        for (int i = 0; i < dimensions; i++) {
            Fraction[] coefficients = new Fraction[dimensions];
            // powers goes from 2^D, then 2^D-1 ... 4 (2^2), 1 (2^0), e.g.:
            // 1, 0, 0, ..., 0 <= 5^1 (i=0)
            // 4, 1, 0, ..., 0 <= 5^2 (i=1)
            // 8, 4, 1, ..., 0 <= 5^3 (i=2)
            // 2^D, 2^(D-1), 2^(D-2), ..., 1 <= 5^D (i=D-1)
            for (int j = 0; j < dimensions; j++) {
                if (j == i) {
                    coefficients[j] = new Fraction(BigInteger.ONE);
                } else if (i < j) {
                    coefficients[j] = new Fraction(BigInteger.ZERO);
                } else {
                    coefficients[j] = new Fraction(BigInteger.TWO.pow(i + 1 - j));
                }
            }

            constraints.add(
                    new Constraint(coefficients, Relationship.LEQ, new Fraction(BigInteger.valueOf(5).pow(i + 1))));
        }

        Fraction[] objectives = new Fraction[dimensions];
        for (int i = 0; i < dimensions; i++) {
            objectives[i] = new Fraction(BigInteger.TWO.pow(dimensions - i));
        }

        // it should only do 2^D steps
        SimplexSolver solver = new SimplexSolver((int) FastMath.pow(2, dimensions));
        PointFractionPair optSolution = solver.solve(
                new ObjectiveFunction(objectives),
                new ConstraintSet(constraints),
                GoalType.MAXIMIZE,
                PivotSelectionRule.RANDOM,
                new NonNegativeValues(true));

        System.out.println("Steps made: " + solver.getResolutionSteps().steps.size());

        // expected result is point is all 0 except last one, with value 5^D
        for (int i = 0; i < optSolution.getFirst().length; i++) {
            if (i != dimensions - 1) {
                assertEquals(optSolution.getFirst()[i], Fraction.ZERO);
            } else {
                assertEquals(optSolution.getFirst()[i], new Fraction(BigInteger.valueOf(5).pow(dimensions)));
            }
        }
    }

    // #region Unit tests files
    /**
     * Fetch, parse and run each tests in the
     * Config.UnitTest_Simplex_RelaxTestsFolder()
     * folder.
     * Parameters can be edited in the configurations file.
     */
    @Test
    public void runRelaxUnitTests() {
        Config.load();

        // if we don't run the test
        if (!Config.UnitTest_Simplex_Enabled()) {
            Logger.log("Relaxed Unit Tests: Simplex disabled", LoggerColor.MAGENTA, LoggerStyle.BOLD);
            return;
        }

        /** If the tests should stop if not all tests have an expected output. */
        final boolean continueWithoutOutput = Config.UnitTest_Simplex_ContinueWithoutOutput();
        /** Relative path to the unitTests folder */
        final String pathToTests = pathToTestFolder(Config.UnitTest_Simplex_RelaxTestsFolder());
        /** File where the list of tests file to run are. */
        final String listToRun = Config.UnitTest_Simplex_TestsToRun().trim();
        /** List of test files. */
        ArrayList<String> testFiles = new ArrayList<String>();
        /** List of test files expected relaxed output. */
        HashMap<String, String> outputFiles = new HashMap<String, String>();

        try {
            File test = new File(pathToTests + listToRun);
            Scanner scanner = new Scanner(test);
            // we expect a list of file name, without extension
            while (scanner.hasNextLine()) {
                // the test file, with our without extension
                final String fileName = scanner.nextLine();
                if (!fileName.isBlank() &&
                // commented files are ignored
                        !fileName.startsWith("#")) {
                    final String filePath = pathToTests + fileName.trim();
                    testFiles.add(filePath);
                    // the expected output of the test file, same name but with ".output"
                    outputFiles.put(filePath, filePath + ".output");
                }
            }
            scanner.close();
        } catch (Exception e) {
            Logger.error("An error occured. Could not find a list of test to run." +
                    "\nSearched at " + pathToTests + ", current path: " + new File(".").getAbsolutePath() +
                    "\nMessage: " + e.getMessage(), false);
            return;
        }

        // stop if no test found, or print future task
        if (testFiles.size() == 0) {
            Logger.trace("Relaxed: No test files found", false);
            return;
        } else {
            Logger.trace("Relaxed: Found " + testFiles.size() + " tests to run");
        }

        // warn or stop if each tests has not an output
        if (testFiles.size() > outputFiles.size()) {
            final String message = "amount of output files is not the same as the amount of test files. Expected " +
                    testFiles.size() + " amount of outputs. Found only " + outputFiles.size() + ".";
            if (!continueWithoutOutput) {
                Logger.error(message, false);
                fail(message);
            } else {
                Logger.warn(message, false);
            }
        }

        /** Count the number of failed test to assert at the end. */
        int numberTestFailed = 0;
        // pass through the service so we can detect if something broke here too
        // and we just have to deal with parsing the file, and not to prepare the solver
        final SimplexService service = new SimplexService();

        // parse a file and its output (if there is one)
        // run the solve and assert response

        for (String test : testFiles) {
            // get the name of the file
            String[] parts = test.split("/");
            String name = parts[parts.length - 1];

            boolean hasOutput = outputFiles.containsKey(test);
            if (!hasOutput) {
                Logger.warn("Test \"" + name + "\" has no expected output file.");
            }

            Pair<Feasibility, Double> expectedInteger = parseOutputFile(outputFiles.get(test));
            SimplexForm testForm = parseTestFile(test);
            // check if we parsed correctly
            if (expectedInteger.getFirst() == Feasibility.UNKNWON) {
                Logger.warn("Test output \"" + test + ".output\" could not be parsed. Running without expectation.");
                hasOutput = false;
            }
            if (testForm == null) {
                numberTestFailed++;
                Logger.failed("Test \"" + test + "\" could not be parsed.");
                continue;
            }

            // perform the test and check results
            SimplexResponse testResult = service.solve(testForm);
            // the expected output
            Pair<Feasibility, Double> expectedOutput = parseOutputFile(outputFiles.get(test));

            // check if we have the expected output
            if (hasOutput) {
                try {
                    // make sure this is not an error, and if it is, print the error
                    if (testResult.feasibility == Feasibility.UNKNWON) {
                        Logger.error(testResult.error);
                        assertNotEquals(Feasibility.UNKNWON, testResult.feasibility);
                    }

                    assertEquals(expectedOutput.getFirst(), testResult.feasibility,
                            () -> "Feasibility");
                    if (expectedOutput.getFirst() == Feasibility.FEASIBLE) {
                        // check if value is in the ballpark with the expected output
                        // since the expected output only have 1e-5 of precision
                        if (Math.abs(testResult.optimum.doubleValue()
                                - expectedOutput.getSecond()) > 1e-5) {
                            fail("Result is " + testResult.optimum.doubleValue() +
                                    ", but expected result is " + expectedOutput.getSecond() +
                                    ", which is a larger difference than 1e-5.");
                        }
                    }

                    Logger.passed(name + " with " + testResult.steps.size() + " steps");
                } catch (AssertionError e) {
                    numberTestFailed++;
                    Logger.failed(name + ": " + e.getMessage(), false);
                }
            } else {
                // if no expected output, just print the result,
                // since we are in a case of "continue without output"
                Logger.info("Test \"" + name + "\" is " +
                        testResult.feasibility.name() + ": " + testResult.optimum);
            }
        }

        assertEquals(0, numberTestFailed,
                numberTestFailed + "/" + testFiles.size() + " tests failed");
        Logger.passed("Relaxed: " + testFiles.size() + "/" + testFiles.size() + " tests passed", false);
    }

    /**
     * Fetch, parse and run each tests in the
     * Config.UnitTest_Simplex_TestsFolder()
     * folder.
     * Parameters can be edited in the configurations file.
     */
    @Test
    public void runIntegerUnitTests() {
        Config.load();

        // if we don't run the test
        if (!Config.UnitTest_Simplex_Enabled()) {
            Logger.log("Integer Unit Tests: Simplex disabled", LoggerColor.MAGENTA, LoggerStyle.BOLD);
            return;
        }

        /** If the tests should stop if not all tests have an expected output. */
        final boolean continueWithoutOutput = Config.UnitTest_Simplex_ContinueWithoutOutput();
        /** Relative path to the unitTests folder */
        final String pathToTests = pathToTestFolder(Config.UnitTest_Simplex_TestsFolder());
        /** File where the list of tests file to run are. */
        final String listToRun = Config.UnitTest_Simplex_TestsToRun().trim();
        /** List of test files. */
        ArrayList<String> testFiles = new ArrayList<String>();
        /** List of test files expected integer output. */
        HashMap<String, String> outputFiles = new HashMap<String, String>();

        try {
            File test = new File(pathToTests + listToRun);
            Scanner scanner = new Scanner(test);
            // we expect a list of file name, without extension
            while (scanner.hasNextLine()) {
                // the test file, with our without extension
                final String fileName = scanner.nextLine();
                if (!fileName.isBlank() &&
                // commented files are ignored
                        !fileName.startsWith("#")) {
                    final String filePath = pathToTests + fileName.trim();
                    testFiles.add(filePath);
                    // the expected output of the test file, same name but with ".output"
                    outputFiles.put(filePath, filePath + ".output");
                }
            }
            scanner.close();
        } catch (Exception e) {
            Logger.error("An error occured. Could not find a list of test to run." +
                    "\nSearched at " + pathToTests + ", current path: " + new File(".").getAbsolutePath() +
                    "\nMessage: " + e.getMessage(), false);
            return;
        }

        // stop if no test found, or print future task
        if (testFiles.size() == 0) {
            Logger.trace("Integer: No test files found", false);
            return;
        } else {
            Logger.trace("Integer: Found " + testFiles.size() + " tests to run");
        }

        // warn or stop if each tests has not an output
        if (testFiles.size() > outputFiles.size()) {
            final String message = "amount of output files is not the same as the amount of test files. Expected " +
                    testFiles.size() + " amount of outputs. Found only " + outputFiles.size() + ".";
            if (!continueWithoutOutput) {
                Logger.error(message, false);
                fail(message);
            } else {
                Logger.warn(message, false);
            }
        }

        /** Count the number of failed test to assert at the end. */
        int numberTestFailed = 0;
        // pass through the service so we can detect if something broke here too
        // and we just have to deal with parsing the file, and not to prepare the solver
        final SimplexService service = new SimplexService();

        // parse a file and its output (if there is one)
        // run the solve and assert response

        for (String test : testFiles) {
            // get the name of the file
            String[] parts = test.split("/");
            String name = parts[parts.length - 1];

            boolean hasOutput = outputFiles.containsKey(test);
            if (!hasOutput) {
                Logger.warn("Test \"" + name + "\" has no expected output file.");
            }

            Pair<Feasibility, Double> expectedInteger = parseOutputFile(outputFiles.get(test));
            SimplexForm testForm = parseTestFile(test);

            // check if we parsed correctly
            if (expectedInteger.getFirst() == Feasibility.UNKNWON) {
                Logger.warn("Test output \"" + test + ".output\" could not be parsed. Running without expectation.");
                hasOutput = false;
            }
            if (testForm == null) {
                numberTestFailed++;
                Logger.failed("Test \"" + test + "\" could not be parsed.");
                continue;
            }
            testForm.toInteger = true;
            testForm.advanced.integerMethod = IntegerMethod.GOMORY.name();
            testForm.advanced.maxIterations = 1000;

            // perform the test and check results
            SimplexResponse testResult = service.solve(testForm);
            // the expected output
            Pair<Feasibility, Double> expectedOutput = parseOutputFile(outputFiles.get(test));

            // check if we have the expected output
            if (hasOutput) {
                try {
                    // make sure this is not an error, and if it is, print the error
                    if (testResult.feasibility == Feasibility.UNKNWON) {
                        Logger.error(testResult.error);
                        assertNotEquals(Feasibility.UNKNWON, testResult.feasibility);
                    }

                    assertEquals(expectedOutput.getFirst(), testResult.feasibility,
                            () -> "Feasibility");
                    if (expectedOutput.getFirst() == Feasibility.FEASIBLE) {
                        assertEquals(expectedOutput.getSecond(), testResult.optimum.doubleValue());
                    }

                    Logger.passed(name + " with " + testResult.steps.size() + " steps", false);
                } catch (AssertionError e) {
                    numberTestFailed++;
                    Logger.failed(name + ": " + e.getMessage(), false);
                }
            } else {
                // if no expected output, just print the result,
                // since we are in a case of "continue without output"
                Logger.info("Test \"" + name + "\" is " +
                        testResult.feasibility.name() + ": " + testResult.optimum);
            }
        }

        assertEquals(0, numberTestFailed,
                numberTestFailed + "/" + testFiles.size() + " tests failed");
        Logger.passed("Integer: " + testFiles.size() + "/" + testFiles.size() + " tests passed", false);
    }

    /**
     * Format the path to the given test folder in
     * {@code Config.UnitTest_Simplex_TestsFolder()}.
     * <p>
     * <b>
     * TODO Tt's a makeshift patch. Maybe we should move the unit test folder to the
     * resources folder.
     * </b>
     *
     * @param testFolderPath Path to the test folder.
     * @return An absolute path to the test folder.
     */
    private static String pathToTestFolder(final String testFolderPath) {
        String absPath = new File(".").getAbsolutePath();

        final String splitter = "/backend";

        if (absPath.contains(splitter)) {
            absPath = absPath.split(splitter)[0];
        }

        return absPath + testFolderPath;
    }

    /**
     * Parse a standardized input file into the appropriate SimplexForm version.
     *
     * @param file A path to a file.
     * @return Corresponding SimplexForm, or null if an error occur.
     */
    private static SimplexForm parseTestFile(String file) {
        if (file == null) {
            return null;
        }

        SimplexForm response = new SimplexForm();
        int currentLine = 0;
        boolean finished = false;

        try {
            File test = new File(file);
            Scanner scanner = new Scanner(test);
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                // m n line
                if (currentLine == 0) {
                    final String[] split = line.split(" ");
                    if (split.length < 2) {
                        Logger.error(file + ": Expected two value on line " + currentLine);
                        break;
                    }
                    Integer m = parseInteger(split[0]);
                    Integer n = parseInteger(split[1]);
                    if (m == null) {
                        Logger.error(file + ": Cannot parse m: " + split[0]);
                        break;
                    }
                    if (n == null) {
                        Logger.error(file + ": Cannot parse n: " + split[1]);
                        break;
                    }
                    response.m = m;
                    response.n = n;

                    // setup the relationship array
                    response.relationships = new String[m];
                    Arrays.fill(response.relationships, Relationship.LEQ.name());

                    // setup the coefs array
                    response.coefs = new double[m][n];
                } else
                // tight line (basic variable indices)
                if (currentLine == 1) {
                    final String[] split = line.split(" ");
                    if (split.length < response.m) {
                        Logger.error(file + ": Expected " + response.m + " value on line " + currentLine);
                        break;
                    }
                    int[] basics = new int[response.m];
                    for (int i = 0; i < response.m; i++) {
                        Integer value = parseInteger(split[i]);
                        if (value == null) {
                            Logger.error(file + ": Cannot parse B" + i + " value on line " + currentLine);
                            break;
                        }
                        basics[i] = value;
                    }
                    response.tight = basics;
                } else
                // loose line (non basic variable indices)
                if (currentLine == 2) {
                    final String[] split = line.split(" ");
                    if (split.length < response.n) {
                        Logger.error(file + ": Expected " + response.n + " value on line " + currentLine);
                        break;
                    }
                    int[] nonbasics = new int[response.n];
                    for (int i = 0; i < response.n; i++) {
                        Integer value = parseInteger(split[i]);
                        if (value == null) {
                            Logger.error(file + ": Cannot parse N" + i + " value on line " + currentLine);
                            break;
                        }
                        nonbasics[i] = value;
                    }
                    response.loose = nonbasics;
                } else
                // constraints constants
                if (currentLine == 3) {
                    final String[] split = line.split(" ");
                    if (split.length < response.m) {
                        Logger.error(file + ": Expected " + response.m + " value on line " + currentLine);
                        break;
                    }
                    double[] constants = new double[response.m];
                    for (int i = 0; i < response.m; i++) {
                        Double value = parseDouble(split[i]);
                        if (value == null) {
                            Logger.error(file + ": Cannot parse b" + i + " value on line " + currentLine);
                            break;
                        }
                        constants[i] = value;
                    }
                    response.constants = constants;
                } else
                // coeff lines
                if (currentLine - 4 < response.m) {
                    final String[] split = line.split(" ");
                    if (split.length < response.n) {
                        Logger.error(file + ": Expected " + response.n + " value on line " + currentLine);
                        break;
                    }
                    for (int i = 0; i < response.n; i++) {
                        Double value = parseDouble(split[i]);
                        if (value == null) {
                            Logger.error(file + ": Cannot parse a" + (currentLine - 4) +
                                    "," + i + " value on line " + currentLine);
                            break;
                        }
                        // we prepared the arrays in the first line
                        response.coefs[currentLine - 4][i] = value;
                    }
                } else
                // objective line
                if (currentLine - 4 == response.m) {
                    final String[] split = line.split(" ");
                    if (split.length < response.n + 1) {
                        Logger.error(file + ": Expected " + (response.n + 1) + " value on line " + currentLine);
                        break;
                    }
                    double[] objectiveValues = new double[response.n + 1];
                    for (int i = 0; i < response.n + 1; i++) {
                        Double value = parseDouble(split[i]);
                        if (value == null) {
                            Logger.error(file + ": Cannot parse N" + i + " value on line " + currentLine);
                            break;
                        }
                        objectiveValues[i] = value;
                    }
                    response.objective = objectiveValues;
                    finished = true;
                    break;
                }
                currentLine++;
            }
            scanner.close();

            if (finished) {
                return response;
            } else {
                return null;
            }
        } catch (Exception e) {
            Logger.error(file + ": An error occured while parsing the file, at line " +
                    currentLine + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Parse an output file. The file must contains at least one line, where it
     * either contains {@code INFEASIBLE}, {@code UNBOUNDED}, a double value
     * parsable with {@code Double.valueOf(String)}, or a
     * {@code Fraction.toString()} fraction.
     * <p>
     * A second line can be present, telling how much steps are necessary to reach
     * said solution, if the problem is {@code FEASIBLE}.
     *
     * @param file A path to a file.
     * @return A pair containing the feasibility. If an error occur, the pair
     *         <UNKNOWN, null> will be returned.
     */
    private static Pair<Feasibility, Double> parseOutputFile(String file) {
        final Pair<Feasibility, Double> defaultResponse = new Pair<Feasibility, Double>(Feasibility.UNKNWON, null);
        if (file == null) {
            return defaultResponse;
        }

        try {
            File test = new File(file);
            Scanner scanner = new Scanner(test);
            // prepare the response component to default
            Feasibility feasibility = defaultResponse.getFirst();
            Double optimum = defaultResponse.getSecond();

            // we expect a single line, either a double, that is the optimum, or a
            // Feasibility error string
            if (scanner.hasNextLine()) {
                // parse the line
                final String expectedOutput = scanner.nextLine();
                // if there is a /, it's maybe a fraction
                if (expectedOutput.contains(" / ")) {
                    String[] parts = expectedOutput.split(" / ");
                    try {
                        int num = Integer.parseInt(parts[0].trim());
                        int den = Integer.parseInt(parts[1].trim());
                        optimum = new Fraction(num, den).doubleValue();
                        feasibility = Feasibility.FEASIBLE;
                    } catch (NumberFormatException e) {
                        // will be catched below
                        scanner.close();
                        throw new Error("Cannot parse \"" + expectedOutput + "\": not a fraction");
                    }
                } else {
                    // try to parse a double
                    try {
                        final double optimumDouble = Double.valueOf(expectedOutput);
                        optimum = optimumDouble;
                        feasibility = Feasibility.FEASIBLE;
                    } catch (NumberFormatException e) {
                        // if failed, then it must be a feasibility
                        if (expectedOutput.contains("INFEASIBLE")) {
                            feasibility = Feasibility.INFEASIBLE;
                        } else if (expectedOutput.contains("UNBOUNDED")) {
                            feasibility = Feasibility.UNBOUNDED;
                        }
                    }
                }
            }

            scanner.close();
            return new Pair<Feasibility, Double>(feasibility, optimum);
        } catch (Exception e) {
            Logger.error("An error occured. Could not find an output to parse. Message: " + e.getMessage());
        }

        return defaultResponse;
    }

    /**
     * Parse a string into a double. Return null if the parsing failed.
     *
     * @param value The value tu parse.
     * @return The double value, or null if an error occured.
     */
    private static Double parseDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse a string into a integer. Return null if the parsing failed.
     *
     * @param value The value tu parse.
     * @return The integer value, or null if an error occured.
     */
    private static Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    // #endregion

    // #region Utility functions
    @Test
    public void testDecimalPart() {
        assertEquals(new Fraction(0), new Fraction(4, 4).getDecimalPart());
        assertEquals(new Fraction(0), new Fraction(0).getDecimalPart());
        assertEquals(new Fraction(0), new Fraction(-1).getDecimalPart());
        assertEquals(new Fraction(1, 4), new Fraction(5, 4).getDecimalPart());
        assertEquals(new Fraction(1, 4), new Fraction(-5, 4).getDecimalPart());
        assertEquals(new Fraction(3, 4), new Fraction(7, 4).getDecimalPart());
    }
    // #endregion
}
