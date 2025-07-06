import { Component, Input } from '@angular/core';
import { SimplexFeasibility, SimplexResponse, SimplexStep } from '../../../models/APIModels';
import { environment } from '../../../environments/environment';
import { formatFraction, formatVariableLabel } from '../../../utils/LatexUtils';
import { LatexComponent } from '../../../shared/latex/latex.component';

/**
 * Given a SimplexResponse, will show each steps, formated and in LaTeX.
 * Or display the SimplexResponse error.
 */
@Component({
    selector: 'app-simplex-response',
    imports: [
        LatexComponent
    ],
    templateUrl: './simplex-response.component.html',
    styleUrl: './simplex-response.component.scss'
})
export class SimplexResponseComponent {
    @Input({ required: true }) set response(value: SimplexResponse) {
        this._response = value;
        console.debug(value);
        this.prepareResponse();
    }
    get response(): SimplexResponse {
        return this._response;
    }

    // preparations for the display
    /** If the response has 2 phases */
    responseHasPhases = false;
    /** Prepared labels. Does not contains L (lambda) */
    preparedLabels: string[] = [];

    /** Inner response */
    private _response: SimplexResponse = {
        status: "unknown",
        code: -1,
        error: null,
        feasibility: -1,
        optimum: "",
        values: [],
        steps: [],
        labels: []
    };

    /** name of the objective function */
    readonly objName = environment.SYMBOLS.OBJECTIVE;
    /** name of the constraints variables */
    readonly varName = environment.SYMBOLS.VARIABLE;
    /** name of the objective function coeficient */
    readonly coefName = environment.SYMBOLS.COEFFICIENT;
    /** name of the constants */
    readonly constName = environment.SYMBOLS.CONSTANT;
    /** Name of artificial variables (slack) */
    readonly slackName = environment.SYMBOLS.ARTIFICIAL;

    /**
     * Prepare the response format.
     */
    prepareResponse() {
        if (this.response == undefined || this.response.steps.length == 0) {
            return;
        }

        for (const step of this.response.steps) {
            if (step.twophase) {
                this.responseHasPhases = true;
                break;
            }
        }
        this.responseHasPhases = false;

        this.preparedLabels = ["\\text{SM}"];
        this.response.labels.forEach(l => {
            if (l == "L" || l == "RHS") {
                return;
            }
            this.preparedLabels.push(formatVariableLabel(l));
        });
    }

    /**
     * Format the fesibility answer accordingly of it's value.
     * If it's feasible, also display the optimum and the values.
     * @returns Formated feasibility.
     */
    formatedFeasibility(): string {
        switch (this.response.feasibility) {
            case SimplexFeasibility.FEASIBLE: {
                let res = `\\text{L'optimum est } ${this.objName}* = ${formatFraction(this.response.optimum)} \\text{avec }`;
                this.response.values.forEach((v, i) => {
                    res += `${this.coefName}_{${i + 1}}* = ${formatFraction(v)} \\space `;
                });
                res += " .";
                return res;
            }
            case SimplexFeasibility.INFEASIBLE:
                return "\\text{Il n'y a pas de solution.}";
            case SimplexFeasibility.UNBOUNDED:
                return "\\text{La solution est non restrainte.}";
            case SimplexFeasibility.ITERATIONS:
                return "\\text{Le nombre maximal d'itérations a été atteint.}"
            default:
                return "\\color{red}{\\text{Erreur inconnue}}";
        }
    }

    /**
     * Create a string to describe the current step.
     * @param step The current step.
     * @param index Index of the current step.
     * @returns A string describing the current step, in LaTeX.
     */
    formatedStepTitle(step: SimplexStep, index: number): string {
        // if in and out are null
        if (step.in == null && step.out == null) {
            // either start of a phase, or end of the method
            if (index + 1 == this.response.steps.length) {
                return "\\text{Tableau final}";
            }
            if (step.dualcut) {
                return "\\text{Phase 1: Premier tableau}";
            } else {
                return "\\text{" + (this.responseHasPhases ? "Phase 2: " : "") + "Premier tableau}";
            }
        } else if (step.in != null && step.out != null) {
            // it is always defined since in and out null are always the index 0
            const previousStep = this.response.steps[index - 1];
            const labelIn = step.twophase && step.in == 1 ? "\\lambda" : this.preparedLabels[step.in]
            const labelOut = step.twophase && previousStep.basicId[step.out - 1] + 1 == 1 ?
                "\\lambda" :
                this.preparedLabels[previousStep.basicId[step.out - 1] + 1]
            return `\\text{Étape ${index}: variable entrante: }${labelIn}\\text{, variable sortante }${labelOut}`;
        }
        return "Étape inconnue";
    }

    // TODO angular table and not a mjx array?
    displayStep(step: SimplexStep, index: number): string {
        // we need it to color the step before the pivot
        const nextStep = this.response.steps[index + 1] || step;
        const heigth = step.table.length;
        const width = step.table[0].length;

        let latex = "\\begin{array} {|c|c|} \\hline & ";

        // first line is the col name
        for (let i = 0; i < width; i++) {
            // color the column
            if (i == nextStep.in) {
                latex += " \\color{RED} "
            }
            // to correspond to the label array
            if (i == 1 && step.twophase) {
                latex += "\\lambda";
            } else {
                latex += this.preparedLabels[i];
            }
            if (i < width - 1) {
                latex += " & ";
            }
        }
        latex += "\\\\ \\hline ";
        // first col of the row is the name of the loose var
        for (let i = 0; i < heigth; i++) {
            for (let j = 0; j < width; j++) {
                // header column
                if (j == 0) {
                    // color the row header
                    if (i == nextStep.out) {
                        latex += " \\color{BLUE} "
                    }
                    if (i == 0) {
                        // objective
                        latex += this.preparedLabels[0];
                    } else {
                        if (step.twophase && i == 1) {
                            latex += `\\lambda`;
                        } else {
                            // skim the table row, if a one is detected, check if the column contains only 0
                            // TODO somehow, we can't make this simpler???
                            for (let k = 1; k < width; k++) {
                                const item = Number(step.table[i][k]);
                                if (Number(item) === 1) {
                                    // look if the column only has 0
                                    let found = true;
                                    for (let l = 0; l < heigth; l++) {
                                        if (l != i && Number(step.table[l][k]) != 0) {
                                            found = false;
                                            break;
                                        }
                                    }
                                    if (found) {
                                        latex += this.preparedLabels[k];
                                        break;
                                    }
                                }

                            }
                        }
                    }
                    latex += " & ";
                }

                // color pivot
                if (j == nextStep.in && i == nextStep.out) {
                    latex += "\\color{PURPLE}"
                } else {
                    // color the column
                    if (j == nextStep.in) {
                        latex += " \\color{RED} "
                    }
                    // color the row
                    if (i == nextStep.out) {
                        latex += " \\color{BLUE} "
                    }
                }
                latex += ` ${formatFraction(step.table[i][j], { noSpace: true, showOnes: true })}`;
                if (j < width - 1) {
                    latex += " & ";
                }
            }
            latex += "\\\\ \\hline ";
        }
        latex += "\\end{array}";
        return latex;
    }
}
