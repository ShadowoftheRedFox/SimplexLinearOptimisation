import { environment } from "../environments/environment";

/**
 * A list of options:
 * - `startingSign: boolean` : Add the leading "+" for positive numbers. Default false.
 * - `showOnes: boolean` : Display the 1 if the value is 1 or -1. Default false.
 * - `noSpace: boolean` : Remove the leading spaces. Default false.
 */
export interface LatexOptions {
    /**
     * Add the leading "+" for positive numbers. Default false.
     */
    startingSign: boolean;
    /**
     * Display the 1 if the value is 1 or -1. Default false.
     */
    showOnes: boolean;
    /**
     * Remove the leading spaces. Default false.
     */
    noSpace: boolean;
}

/**
 * Given the LaxtexOptions, it will create or add the default values.
 * @param options The partial latex option object.
 * @returns A full LatexOptions.
 */
function parseOptions(options?: Partial<LatexOptions>): LatexOptions {
    const res: LatexOptions = { startingSign: false, showOnes: false, noSpace: false };
    if (options) {
        res.startingSign = options.startingSign || false;
        res.showOnes = options.showOnes || false;
        res.noSpace = options.noSpace || false;
    }
    return res;
}

/**
 * Transform a number into the correct latex representation. If
 * the given value is not a number, it is replaced with "?".
 * @param int The value to display.
 * @param options A list of format options.
 * @returns Latex formated value.
 */
export function formatNumber(int: number, options?: Partial<LatexOptions>): string {
    options = parseOptions(options);

    const empty = options.noSpace ? "" : "\\space\\space\\space\\space";

    if (int == null || int == undefined || Number.isNaN(int)) {
        return (options.startingSign ? "+\\space" : empty) + "?";
    }
    if (int < 0) { return "-\\space" + (-int != 1 || options.showOnes ? -int : '\\space\\space '); }
    return (options.startingSign ? "+\\space" : empty) + (int != 1 || options.showOnes ? int : '\\space\\space ');
}

/**
 * Get the latex sign corresponding to the current goal.
 * @returns The latex sign.
 */
export function getSign(toMaximise: boolean): string {
    return toMaximise ? "\\leqslant" : "\\geqslant";
}

/**
 * Format a fraction string, either X, or X / Y, into correct latex fraction.
 * @param fraction A string to format.
 * @param options A list of format options.
 * @returns The latex formated string.
 */
export function formatFraction(fraction: string, options?: Partial<LatexOptions>): string {
    options = parseOptions(options);
    if (fraction == null || fraction == undefined) { return ""; }
    if (!fraction.includes("/")) { return formatNumber(Number(fraction), options); }

    const split = fraction.split("/");
    // format numbers without space and signs
    const num = formatNumber(Number(split[0]), { noSpace: true, showOnes: true });
    const denum = formatNumber(Number(split[1]), { noSpace: true, showOnes: true });
    return `\\frac{${num}}{${denum}}`;
}

/**
 * Format a variable label, like "x0" or "y5", into the correct label format.
 * @param label The raw label variable.
 * @returns LaTeX formated variable label.
 */
export function formatVariableLabel(label: string): string {
    if (label == undefined || label.length == 0) {
        return "";
    }
    let labelType = label[0];
    if (labelType == "x") {
        labelType = environment.SYMBOLS.VARIABLE;
    } else if (labelType == "y") {
        labelType = environment.SYMBOLS.ARTIFICIAL;
    }
    return `${labelType}_{${Number(label.slice(1, label.length)) + 1}}`;
}
