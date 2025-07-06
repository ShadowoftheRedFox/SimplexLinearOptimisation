/**
* Value returned depending on the feasiblity of the given problem.
*/
export enum SimplexFeasibility {
    /** Unknown return. For errors. */
    UNKNWON = -1,
    /** Problem feasible, and have an optimum. */
    FEASIBLE = 0,
    /** Problem infeasible. Constraints too tight. */
    INFEASIBLE = 1,
    /** Problem unbounded. Constraints too loose. */
    UNBOUNDED = 2,
    /** Too many iterations made. */
    ITERATIONS = 3,
}

/**
 * Awaited response when the API has solved a simplex problem.
 */
export interface SimplexResponse {
    /** HTTP status message. */
    status: string;
    /** HTTP status code. */
    code: number;
    /** HTTP error. */
    error: string | null;

    /** Problem feasibility value. */
    feasibility: SimplexFeasibility
    /**
     * The optimum found if feasibility is feasible.
     * It's a string because it's a fraction.
     */
    optimum: string;
    /**
     * The values of the variables if feasibility is feasible.
     * They're string because they're fractions.
     */
    values: string[];
    /** Each simplex steps made. */
    steps: SimplexStep[];
    /** Labels of each columns, lambda included. */
    labels: string[];
}

/**
 * Describe one step made during a simplex method resolution.
 */
export interface SimplexStep {
    /**
     * Column index going out in this step.
     * Null if for the starting step of a phase, or final table.
     *
     * To get the correct label, look at the previous step basic array, at the
     * position `out-1`.
     */
    out: number | null;

    /**
     * Variable going in in this step.
     * Null if for the starting step of a phase, or final table.
     *
     * To get the correct label, you only need to look at the position
     * `in`. Keep in mind that in phase 1, 0 means L (lambda), but in phase 2,
     * 0 means x0.
     */
    in: number | null;

    /** If this step is a phase 1. */
    twophase: boolean;

    /**
     * If this step is a step where a dual cut is added (to get integer
     * response).
     */
    dualcut: boolean;

    /**
     * Table of the current step. The first column is always the objective
     * function values, if twophase is true, the second column is L (lambda).
     * This first row is always the objective functions coefficients.
     * They're string because they're represented as fractions.
     */
    table: string[][];

    /**
     * Column index of the basic variables. The index 0 is the first variable
     * (L or x0 depending on the phase 1 or 2, respectively).
     *
     * TO get the correct label, at a given position `i` in the array, the
     * correct label would be `label[basicId[i]-1]`.
     * */
    basicId: number[];
}

/**
 * Describe the modelisation form of a problem looks like.
 */
export interface SimplexForm {
    /** Number of constraints. */
    m: number;

    /** Number or variables. */
    n: number;

    /** Lines index +1 of non basic variables. */
    loose: number[];

    /** Lines index +1 of basic variables. */
    tight: number[];

    /**
     * Right hand side constants.
     * The length must be `m`.
     */
    constants: number[];

    /**
     * Right hand side coefficients.
     * The length must be `m` times `n`.
     */
    coefs: number[][];

    /**
     * The objective functions.
     * The index `0` is the constants.
     * The others are the objective coefficients.
     * The length must be `n+1`.
     */
    objective: number[];

    /** Whether this is a maximisation problem or not. */
    toMaximise: boolean;

    /** Whether this problem is an integer or a relaxed problem. */
    toInteger: boolean;
}
