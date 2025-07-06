/*
================================ INFORMATIONS ================================
It seems that Angular can load a script, like Desmos for this case, by
editing the angular.json scripts fields.
Though, it doesn't like the namespace declarations, even if the editor finds
it. This is why most of the comment are the type founds with the
@type/desmos package.
==============================================================================
*/

/* eslint-disable @typescript-eslint/no-explicit-any */
import { AfterViewInit, Component, ElementRef, inject, Input, isDevMode, ViewChild } from '@angular/core';
import { SimplexForm } from '../../models/APIModels';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Declare Desmos as a global variable so that it can be used in this TypeScript file
declare global {
    interface Window {
        Desmos: any;
    }
}

@Component({
    selector: 'app-graph',
    imports: [
        MatSnackBarModule
    ],
    templateUrl: './graph.component.html',
    styleUrl: './graph.component.scss'
})
export class GraphComponent implements AfterViewInit {
    readonly snackBar = inject(MatSnackBar);

    constructor() {
        // DEBUG for easy developpement
        if (isDevMode()) {
            this._form = this.man;
        }
    }

    // private calculator!: Desmos.Calculator;
    private calculator!: any;

    @ViewChild("calculator") calculatorElement!: ElementRef<HTMLDivElement>;
    @Input({ required: true }) set form(value: SimplexForm) {
        this._form = value;
        console.debug(value);
        this.render();
    }
    get form(): SimplexForm {
        return this._form;
    }

    /** Inner form */
    private _form: SimplexForm = {
        m: 0,
        n: 0,
        loose: [],
        tight: [],
        constants: [],
        coefs: [],
        objective: [],
        toMaximise: true,
        toInteger: false,
    }

    /** Extract from exemples */
    private man: SimplexForm =
        { m: 3, n: 2, loose: [1, 2, 3], tight: [4, 5], constants: [12, 9, 2], coefs: [[2, 3], [3, 1], [-1, -1]], objective: [0, 1, 1], toMaximise: true, toInteger: false, };
    // { m: 3, n: 2, loose: [1, 2, 3], tight: [4, 5], constants: [12, 9, -2], coefs: [[2, 3], [3, 1], [-1, -1]], objective: [0, 1, 1], toMaximise: true, toInteger: false, };

    private viewLoaded = false;

    ngAfterViewInit(): void {
        const Desmos = window.Desmos;
        this.calculator = Desmos.GraphingCalculator(this.calculatorElement.nativeElement);
        this.viewLoaded = true;
        this.render();
    }

    render() {
        // the calculator isn't ready
        if (!this.viewLoaded) { return; }

        // can't plot a line (well we *can*, just there is no point foing it)
        // TODO  plot the line
        if (this.form.n <= 1) {
            this.snackBar.open("Le nombre de variables est inférieur à 2, la dimension à afficher est donc une ligne.", "OK", {
                duration: 20 * 1000,
                horizontalPosition: "right",
                verticalPosition: "top",
                politeness: "polite",
            });
            return;
        }

        // do a little popup to say that the dimension is larger than 2, and can't be plotted fully
        if (this.form.n > 2) {
            this.snackBar.open("Le nombre de variables est supérieur à 2, la dimension à afficher est donc plus grande qu'un plan. L'affichage ne se fera qu'avec les deux premières variables.",
                "OK", {
                duration: 20 * 1000,
                horizontalPosition: "right",
                verticalPosition: "top",
                politeness: "polite",
            });
        }

        // since we restrict to non negative values
        this.calculator.setExpression({ id: 'XnonNegative', latex: 'y<0', color: '#ff0000' });
        this.calculator.setExpression({ id: 'YnonNegative', latex: 'x<0', color: '#ff0000' });

        // display the zone restrained by the constraints
        // we invert the signs, to fill the space where we don't want values
        this.form.coefs.forEach((coefs, i) => {
            const latex = `(${coefs[0]}x) + (${coefs[1]}y) >= ${this.form.constants[i]}`;
            this.calculator.setExpression({ id: `coef${i}`, latex: latex });
        });

        // plot two lines to show where the objectives tends to
        this.calculator.setExpression({ id: 'originDirectionLine1', latex: this.getObjectiveLine(0, 0), color: '#000000', points: false });
        // TODO make the line relative to the objective (if the graph is very big, close lines won't be seen, same if the graph is very small)
        const x = 1;
        const y = 1;
        this.calculator.setExpression({ id: 'originDirectionLine2', latex: this.getObjectiveLine(x, y), color: '#000000', points: false });

        // slider to move an objective line up and down
        // TODO same as above, find min and max value depending on the problem size
        const movable = "z";
        this.calculator.setExpression({ id: 'originSlider', latex: `${movable}=0`, sliderBounds: { min: -10, max: 10, step: this.form.toInteger ? "1" : "" } });
        this.calculator.setExpression({ id: 'originMovable', latex: this.getObjectiveLine(0, 0, movable), color: '#ff0000', points: false });
    }

    private getObjectiveLine(x: number, y: number, movable?: string): string {
        const coefX0 = this.form.objective[1];
        const coefX1 = this.form.objective[2];
        const constant = this.form.objective[0];
        let equals: string = ((coefX0 * x + coefX1 * y - constant) * (this.form.toMaximise ? 1 : -1)) + "";
        if (movable != undefined) {
            equals = movable + (this.form.toMaximise ? "" : "*-1")
        }
        return `(${coefX0}x) + (${coefX1}y) - ${constant} = ${equals}`;
    }
}
