package optim.optim.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import optim.optim.src.Config;
import optim.optim.src.simplex.Fraction;
import optim.optim.src.simplex.SimplexTable;

/**
 * A single step of the simplex method resolution. It contains the current
 * table, which variable is going in and out at this step. It also contains
 * informations whether this is in the first phase, or when adding an integer
 * constraint.
 */
public class SimplexStep {
    /**
     * Variable going out in this step.
     */
    public Integer out;
    /**
     * Variable going in in this step.
     */
    public Integer in;
    /**
     * If this step is the first phase of the two phase method.
     */
    public boolean twophase;
    /**
     * If this step is a step where a dual cut is added (to get integer
     * response).
     */
    private boolean dualcut;

    /**
     * Table of the current step. First value of each column and row
     * is the index of the variable of said column or row.
     */
    public Fraction[][] table;

    /** Index of the basics variables. */
    public ArrayList<Integer> basicRows = new ArrayList<Integer>();

    /** Name of the columns. */
    protected ArrayList<String> columLabels = new ArrayList<String>();

    /**
     * Fill a new step with the given data.
     *
     * @param inCol  The column index going in. Null for start of step.
     * @param outRow The row index going out. Null for start of step.
     * @param table  The table at the current state.
     */
    public SimplexStep(final Integer inCol, final Integer outRow, final SimplexTable table) {
        // TODO max step size? or depending the size of the table
        this.in = inCol;
        this.out = outRow;
        this.twophase = table.isPhase1();
        // Logger.debugStep(inCol, outRow, table);
        // if debug, we don't want to remember the steps fully
        if (Config.Debugger_Enabled()) {
            setTable(null);
        } else {
            setTable(table);
        }
    }

    /**
     * Setter for {@link #table}.
     *
     * @param table The SimplexTable to convert.
     * @throws NullPointerException If the given tableau is null.
     */
    private void setTable(final SimplexTable table) throws NullPointerException {
        if (!Config.Debugger_Enabled()) {
            Objects.requireNonNull(table);
            this.table = table.getSimplexTable();
            this.basicRows.addAll(table.getBasicVariables());
        }
    }

    /** Return a JSON formatted string of the class. */
    public String toString() {
        String[] substring = new String[table.length];
        for (int i = 0; i < table.length; i++) {
            substring[i] = SimplexResponse.arrayToString(table[i]);
        }
        String columLabelsString = "[";
        for (int i = 0; i < columLabels.size(); i++) {
            columLabelsString += "\"" + columLabels.get(i) + "\"";
            if (i < columLabels.size() - 1) {
                columLabelsString += ",";
            }
        }
        columLabelsString += "]";
        return "{" +
                "\"out\":" + out + "," +
                "\"in\":" + in + "," +
                "\"twophase\":" + twophase + "," +
                "\"dualcut\":" + dualcut + "," +
                "\"table\":" + Arrays.toString(substring) + "," +
                // don't add labels if none were given
                (columLabels.size() == 0 ? "" : "\"labels\":" + columLabelsString + ",") +
                "\"basicId\":" + basicRows +
                "}";
    }
}
