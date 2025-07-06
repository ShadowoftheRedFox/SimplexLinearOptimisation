export const environment = {
    /**
     * Tell if the current instance is running in
     * production.
     * Default: false
     */
    PROD: false,
    /**
     * The API url.
     * Default: "http://localhost"
     */
    API_URL: "http://localhost",
    /**
     * The API port.
     * Default: "8080"
     */
    API_PORT: "8080",
    /**
     * Used in name accross pages.
     * Default: "CY Tech Simplex"
     */
    TITLE: "CY Tech Simplex",
    /**
     * Specific timeouts for HTTP request, since some
     * can take a lot of time. Values are in miliseconds.
     */
    TIMEOUTS: {
        /**
         * Value used for any HTTP request.
         * Default: 3000
         */
        DEFAULT: 3 * 1000,
        /**
         * Value used for simplex request.
         * Default: 120000
         */
        SIMPLEX: 2 * 60 * 1000,
    },
    /**
     * Symbols used in equations.
     */
    SYMBOLS: {
        /**
         * Label for the objective function.
         * Default: "z"
         */
        OBJECTIVE: "z",
        /**
         * Label for variables.
         * Default: "x"
         */
        VARIABLE: "x",
        /**
         * Label for the objective function coefficients.
         * Default: "c"
         */
        COEFFICIENT: "c",
        /**
         * Label for the artificials variables.
         * Default: "y"
         */
        ARTIFICIAL: "y",
        /**
         * Label for for constant value.
         * Default: "b"
         */
        CONSTANT: "b",
        /**
         * Label for variables in the dual.
         * Default: "u"
         */
        DUAL_VARIABLE: "u",
        /**
         * Label for the objective function coefficient in the dual.
         * Default: "d"
         */
        DUAL_COEFFICIENT: "d",
        /**
         * Label for the artificial variables in the dual.
         * Default: "v"
         */
        DUAL_ARTIFICIAL: "v",
    }
};
