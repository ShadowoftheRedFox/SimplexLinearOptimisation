package optim.optim.response;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import optim.optim.src.simplex.Fraction;
import optim.optim.src.simplex.SimplexTable;

/**
 * Response expected after a simplex request. It contains the resolution steps,
 * results and feasibility.
 */
public class SimplexResponse {
    /** Default constructor. */
    public SimplexResponse() {
    }

    /**
     * Value returned depending on the feasiblity of the given problem.
     */
    public enum Feasibility {
        /** Unknown return. For errors. */
        UNKNWON(-1),
        /** Problem feasible, and have an optimum. */
        FEASIBLE(0),
        /** Problem infeasible. Constraints too tight. */
        INFEASIBLE(1),
        /** Problem unbounded. Constraints too loose. */
        UNBOUNDED(2),
        /** Too many iterations made. */
        ITERATIONS(3);

        private final int value;

        /**
         * Contructor when getting the enum.
         *
         * @param value The value of the enum.
         */
        private Feasibility(int value) {
            this.value = value;
        }

        /**
         * Getter for {@link #value}.
         *
         * @return The value, between 0 to 3 included.
         */
        public int value() {
            return value;
        }
    }

    /** HTTP status message. */
    public HttpStatus status = HttpStatus.OK;
    /** HTTP status code. */
    public int code = status.value();
    /** HTTP error. */
    public String error = null;
    /** Problem feasibility value. */
    public Feasibility feasibility = Feasibility.UNKNWON;
    /** The optimum found if feasibility is feasible. */
    public Fraction optimum = Fraction.ZERO;
    /** The values of the variables if feasibility is feasible. */
    public Fraction[] values = {};
    /** Each simplex steps made. */
    public ArrayList<SimplexStep> steps = new ArrayList<SimplexStep>();
    /** Name of the columns. */
    protected List<String> columLabels = new ArrayList<String>();

    /**
     * Add a resolution step. It should be done at each iteration, and each revelant
     * steps (like the first table of change of phases).
     *
     * @param inCol  The column index of the variable entering the base.
     * @param outRow The row index of the variable leaving the base.
     * @param table  The current table.
     */
    public void addStep(final Integer inCol, final Integer outRow, final SimplexTable table) {
        if (columLabels.size() == 0) {
            columLabels = table.getLabels();
        }
        steps.add(new SimplexStep(inCol, outRow, table));
    }

    /**
     * Set the given status and set the error as "Unknown error" if the given code
     * is an error. Null otherwise.
     *
     * @param status The HTTP response code.
     * @return This SimplexResponse instance.
     */
    public SimplexResponse setStatus(HttpStatus status) {
        return setStatus(status, "Unknown error");
    }

    /**
     * Set the given string as the error value. If error is null, the status is set
     * to {@code OK}, otherwise it is set to {@code INTERNAL_SERVER_ERROR}.
     *
     * @param error The error to set.
     * @return This SimplexResponse instance.
     */
    public SimplexResponse setStatus(String error) {
        return setStatus(error == null ? HttpStatus.OK : HttpStatus.NOT_ACCEPTABLE, error);
    }

    /**
     * Set a status and an error.
     *
     * @param status The HTTP response code.
     * @param error  The error to set.
     * @return This SimplexResponse instance.
     */
    public SimplexResponse setStatus(HttpStatus status, String error) {
        if (status == null) {
            status = error == null ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        this.status = status;
        this.code = status.value();
        this.error = error;
        return this;
    }

    /** Return the JSON stringified value of this class. */
    @Override
    public String toString() {
        String columLabelsString = "[";
        for (int i = 0; i < columLabels.size(); i++) {
            columLabelsString += "\"" + columLabels.get(i) + "\"";
            if (i < columLabels.size() - 1) {
                columLabelsString += ",";
            }
        }
        columLabelsString += "]";

        return "{" +
                "\"error\":" + (error == null ? "null" : "\"" + error + "\"") + "," +
                "\"feasibility\":" + feasibility.value + "," +
                "\"optimum\":\"" + optimum + "\"," +
                "\"values\":" + arrayToString(values) + "," +
                "\"code\":" + code + "," +
                "\"status\":\"" + status.name() + "\"," +
                "\"steps\":" + steps.toString() + "," +
                "\"labels\":" + columLabelsString +
                "}";
    }

    /**
     * Transform an array of Object into a valid JSON string.
     *
     * @param array The array to stringify.
     * @return The stringified array.
     */
    public static String arrayToString(Object[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }

        String res = "[";
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] != null) {
                res += "\"" + array[i] + "\",";
            } else {
                res += "\"null\",";
            }
        }
        res += "\"" + array[array.length - 1] + "\"]";
        return res;
    }
}
