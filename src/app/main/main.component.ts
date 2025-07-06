// disable below because eslint doesn't like either regex not latex
/* eslint-disable no-useless-escape */

import { AbstractControl, FormArray, FormBuilder, FormControl, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AsyncPipe } from '@angular/common';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, inject, isDevMode, ViewChild } from '@angular/core';
import { environment } from '../../environments/environment';
import { getSign, formatNumber } from '../../utils/LatexUtils';
import { LatexComponent } from "../../shared/latex/latex.component";
import { map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatRadioModule } from '@angular/material/radio';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { Observable } from 'rxjs';
import { SimplexFeasibility, SimplexResponse } from '../../models/APIModels';
import { SimplexResponseComponent } from '../main/simplex-response/simplex-response.component';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';

@Component({
    selector: 'app-main',
    imports: [
        AsyncPipe,
        LatexComponent,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatMenuModule,
        MatRadioModule,
        MatSlideToggleModule,
        MatStepperModule,
        ReactiveFormsModule,
        SimplexResponseComponent,
    ],
    templateUrl: './main.component.html',
    styleUrl: './main.component.scss',
    providers: [
        {
            provide: STEPPER_GLOBAL_OPTIONS,
            useValue: { displayDefaultIndicatorType: false },
        },
    ],
})
export class MainComponent {
    readonly api = inject(ApiService);
    readonly snackBar = inject(MatSnackBar);
    readonly breakpointObserver = inject(BreakpointObserver);

    constructor() {
        this.setupFormListener();
        // mobile responsive listener
        this.isMobile = this.breakpointObserver
            .observe('(min-width: 880px)')
            .pipe(map(({ matches }) => (!matches)));
    }

    // view to reset the input
    @ViewChild("input") inputFile!: HTMLInputElement;

    /**
     * name of the objective function
     */
    readonly objName = environment.SYMBOLS.OBJECTIVE;
    /**
     * name of the constraints variables
     */
    readonly varName = environment.SYMBOLS.VARIABLE;
    /**
     * name of the objective function coeficient
     */
    readonly coefName = environment.SYMBOLS.COEFFICIENT;
    /**
     * name of the constants
     */
    readonly constName = environment.SYMBOLS.CONSTANT;

    // list of the wanted objectives type
    readonly objectives = [
        { name: "Maximiser", value: true },
        { name: "Minimiser", value: false }
    ];
    // create a non nullable form buildre just for type simplicity
    readonly formBuilder = new FormBuilder().nonNullable;
    // stand alone controller, since we don't want it's value in the raw data to send to the api
    readonly formInputFile = this.formBuilder.control<string>("");
    // the main form with all the necessary value
    // and yes you read well, a FormArrays of FormArrays of FormControl of number :<
    simplex = this.formBuilder.group({
        toMaximise: this.formBuilder.control<boolean>(true, [Validators.required]),
        toInteger: this.formBuilder.control<boolean>(false, [Validators.required]),
        m: this.formBuilder.control<number>(0, [Validators.required, Validators.min(1), this.numberValidator()]),
        n: this.formBuilder.control<number>(0, [Validators.required, Validators.min(1), this.numberValidator()]),
        // TODO a slide toggle to say this line is tight or not
        // TODO also, there must be at most m tight (to clarify)
        // gives info on the choosen index of tight variables
        tight: this.formBuilder.array<number>([], [Validators.minLength(1), this.numberValidator()]),
        // gives info on the choosen index of loose variables
        loose: this.formBuilder.array<number>([], [Validators.minLength(1), this.numberValidator()]),
        constants: this.formBuilder.array<number>([], [Validators.required, Validators.minLength(1), this.numberValidator()]),
        coefs: this.formBuilder.array<FormArray<FormControl<number>>>([], [Validators.required, Validators.minLength(1), this.numberValidator()]),
        objective: this.formBuilder.array<number>([], [Validators.required, Validators.minLength(2), this.numberValidator()]),
    });
    // custom validator for preventing NaN to pass through as valid value to submit
    private numberValidator(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if (Number.isNaN(control.value)) {
                return { isNaN: true };
            }
            // TODO switch to bigint in the back?
            // max int value
            const MaxJavaIntValue = 2147483648;
            if (control.value > MaxJavaIntValue) {
                return { tooBig: MaxJavaIntValue };
            }
            // min int value
            const MinJavaIntValue = -2147483648;
            if (control.value < MinJavaIntValue) {
                return { tooSmall: MinJavaIntValue };
            }
            return null;
        }
    }

    // the preview string of the problem in latex notation
    public latex = "";
    // value displayed by the input button
    public filename = "Chosissez un fichier";

    // response of the server with the resolution
    public simplexResponse: SimplexResponse | null = null;

    // observable for detecting mobile sized screens
    public isMobile: Observable<boolean>;
    // TODO  input leq, geq or eq, and then convert it into leq

    private setupFormListener() {
        const ctrls = this.simplex.controls;
        // check m and n are whole number
        // and if they are, add/delete new controls for the array
        ctrls.m.valueChanges.subscribe(value => {
            if (Number(value) != Math.floor(Number(value))) {
                ctrls.m.setErrors({ notwhole: true });
            }
        });
        ctrls.n.valueChanges.subscribe(value => {
            if (Number(value) != Math.floor(Number(value))) {
                ctrls.n.setErrors({ notwhole: true });
            }
        });
        // update preview if we change anything
        this.simplex.valueChanges.subscribe(() => {
            this.resizeControlArrays();
        });
    }
    /**
     * Resize all FormArray to the correct size depending on M and N.
     * If those two are invalid, skip resize.
     */
    private resizeControlArrays(): void {
        const ctrls = this.simplex.controls;
        const M = ctrls.m.value;
        const N = ctrls.n.value;

        if (ctrls.m.invalid || ctrls.n.invalid) { return; }

        // we expect M constants
        if (ctrls.constants.length > M) {
            // remove enough controls from the end
            for (let i = ctrls.constants.length - 1; i >= M; i--) {
                ctrls.constants.removeAt(i);
            }
        } else if (ctrls.constants.length < M) {
            // add controls
            for (let i = 0; ctrls.constants.length < M; i++) {
                ctrls.constants.push(this.formBuilder.control(NaN, [Validators.required, this.numberValidator()]));
            }
        }

        // we expect N + 1 objectives (the constants is the 1)
        if (ctrls.objective.length > N + 1) {
            // remove enough controls from the end
            for (let i = ctrls.objective.length - 1; i >= N + 1; i--) {
                ctrls.objective.removeAt(i);
            }
        } else if (ctrls.objective.length < N + 1) {
            // add controls
            for (let i = 0; ctrls.objective.length < N + 1; i++) {
                ctrls.objective.push(this.formBuilder.control(NaN, [Validators.required, this.numberValidator()]));
            }
        }

        // lastly, we expect M constraints with N values
        // check main array first, who should all be of length M
        if (ctrls.coefs.length > M) {
            // remove enough controls from the end
            for (let i = ctrls.coefs.length - 1; i >= M; i--) {
                ctrls.coefs.removeAt(i);
            }
        } else if (ctrls.coefs.length < M) {
            // add controls
            for (let i = 0; ctrls.coefs.length < M; i++) {
                ctrls.coefs.push(this.formBuilder.array<FormControl<number>>([], [Validators.required, Validators.minLength(1), this.numberValidator()]));
            }
        }
        // loop throught each internal array now, and check they are all N length
        ctrls.coefs.controls.forEach(ctrl => {
            if (ctrl.length > N) {
                // remove enough controls from the end
                for (let i = ctrl.length - 1; i >= N; i--) {
                    ctrl.removeAt(i);
                }
            } else if (ctrl.length < N) {
                // add controls
                for (let i = 0; ctrl.length < N; i++) {
                    ctrl.push(this.formBuilder.control(NaN, [Validators.required, this.numberValidator()]));
                }
            }
        });
        this.latexPreview();
    }

    // message displayed for either M or N control when theyr are invalid
    public MNErrorMessage(ctrl: FormControl): string {
        if (ctrl == null || ctrl == undefined) { return ""; }

        if (ctrl.hasError("required")) {
            return "Valeur numérique requise";
        } else if (ctrl.hasError("min")) {
            return "Valeur min. est " + ctrl.getError("min").min;
        } else if (ctrl.hasError("notwhole")) {
            return "Doit être un nombre entier";
        } else if (ctrl.errors != null) {
            console.warn("Unhandled form error: ", ctrl.errors);
        }

        return "";
    }

    // Message displayed when the file control is invalid
    public fileErrorMessage(): string {
        const ctrl = this.formInputFile;

        if (ctrl.hasError("nottxt")) {
            return "Le fichier donné doit être un '.txt'"
        } else if (ctrl.hasError("unreadable")) {
            return "Impossible de lire le fichier";
        } else if (ctrl.hasError("invalidcontent")) {
            let res = "Le contenu du fichier n'est pas celui attendu";
            if (ctrl.getError("invalidcontent") != true) {
                res += ": " + ctrl.getError("invalidcontent");
            }
            return res;
        } else if (ctrl.hasError("error")) {
            return ctrl.getError("error");
        }

        return "";
    }

    // message displayed when the form is invalid
    public simplexErrorMessage(ctrl?: FormControl): string {
        if (ctrl != undefined) {
            if (ctrl.hasError("required")) {
                return "Une valeur n'est pas un nombre";
            }
            if (ctrl.hasError("tooBig")) {
                return "Une valeur est trop grande";
            }
            if (ctrl.hasError("tooSmall")) {
                return "Une valeur est trop petite";
            }
        }
        if (this.simplex.invalid && (this.simplex.dirty || this.simplex.touched)) {
            return "Valeurs manquantes ou invalides";
        }
        return "";
    }

    // listener to the file input since angular doesn't surpport them
    public onFileChange(event: Event) {
        const ctrl = this.formInputFile;

        if (event == null || event.target == null) {
            return;
        }
        const input: HTMLInputElement = event.target as HTMLInputElement;
        if (input.files == null || input.value.length == 0) {
            return;
        }
        const file = input.files[0]; // Here we use only the first file (single file)

        // look at the file and check values
        if (file == null) {
            return;
        }
        this.filename = "Fichier choisis: " + file.name;

        const reader = new FileReader();
        reader.readAsText(file, "UTF-8");
        reader.onload = (event) => {
            if (event.target == null) {
                ctrl.setErrors({ unreadable: true });
                return;
            }
            const content = event.target.result;
            if (typeof content != "string") {
                ctrl.setErrors({ invalidcontent: "ne contient pas de texte" });
                return;
            }

            if (this.checkFormat(content)) {
                // pass the value to the controller
                this.formInputFile.patchValue(content);
            }
        }
        reader.onerror = () => {
            ctrl.setErrors({ unreadable: true });
        }
    }

    // check that the content of the file match the expected input format
    private checkFormat(content: string): boolean {
        console.debug("Content:\n" + content);

        // quick replacement, in case the file use comma instead of dot
        // Number return NaN, and the regex fail if we don't use dots
        content = content.replaceAll(",", ".").trim();

        const ctrl = this.formInputFile;
        // this regex check the file contains only:
        //      numbers (negative and with floating point are valid)
        //      space
        // should return true
        const numberRegex = /^[0-9\-\. ]+$/
        // this regex check if it doesn't have:
        //      double points
        //      double dash
        //      multiples leading 0
        //      dash at the end of a number
        // should return false
        const doubleRegex = /^(?:\-\-|\-\s|\.\.|\s\.|\.\s|\d\-|\s00)|[-.]{1}$/;

        // check both regex on the stripped, flattened content
        const flattened = content.replaceAll("\n", " ").trim();
        if (!numberRegex.test(flattened) || doubleRegex.test(flattened)) {
            ctrl.setErrors({ invalidcontent: "ne contient pas de nombres valides" });
            return false;
        }

        // remember the lines and trim them (multiple spaces are removed)
        const lines: string[] = [];
        content.split("\n").forEach(l => {
            lines.push(l.replace(/ +(?= )/g, '').trim());
        });

        // 5 because: line M N, line of tight, line of loose, line of constants, line of the objective
        if (lines.length < 5) {
            ctrl.setErrors({ invalidcontent: "pas assez de lignes" });
            return false;
        }

        // check first line has M and N
        if (lines[0].split(" ").length != 2) {
            ctrl.setErrors({ invalidcontent: "première ligne ne contient pas deux nombres entier" });
            return false;
        }
        const M = Math.floor(Number(lines[0].split(" ")[0]));
        const N = Math.floor(Number(lines[0].split(" ")[1]));
        console.debug(`M: ${lines[0].split(" ")[0]}, N: ${lines[0].split(" ")[1]}`);
        if (M < 1 || isNaN(M)) {
            ctrl.setErrors({ invalidcontent: `M est inférieur à 1 (reçu ${M})` });
            return false
        }
        if (N < 1 || isNaN(N)) {
            ctrl.setErrors({ invalidcontent: `N est inférieur à 1 (reçu ${N}` });
            return false
        }
        this.simplex.patchValue({
            m: M,
            n: N,
        });

        // we now know how much lines to expect, so check them
        if (lines.length < 5 + M) {
            ctrl.setErrors({ invalidcontent: `pas assez de lignes (${M + 5} attendues, ${lines.length} trouvées)` });
            return false;
        }

        // variable to temporarily put parsed values
        let arr: number[] = [];

        // check tight variables, expected M amount
        console.debug(`Tight: ${lines[1]}`);
        if (lines[1].split(" ").length < M) {
            ctrl.setErrors({ invalidcontent: `pas assez de valeurs (${M} attendues, ${lines[1].split(" ").length} trouvées)` });
            return false;
        }
        for (const num of lines[1].split(" ")) {
            if (isNaN(Number(num)) || Number(num) <= 0) {
                ctrl.setErrors({ invalidcontent: `${num} n'est pas un nombre valide` });
                return false;
            }
            arr.push(Number(num));
        }
        this.simplex.patchValue({
            tight: arr
        });
        arr = [];

        // check loose variables, expected N amount
        console.debug(`Loose: ${lines[2]}`);
        if (lines[2].split(" ").length < N) {
            ctrl.setErrors({ invalidcontent: `pas assez de valeurs (${N} attendues, ${lines[2].split(" ").length} trouvées)` });
            return false;
        }
        for (const num of lines[2].split(" ")) {
            if (isNaN(Number(num)) || Number(num) <= 0) {
                ctrl.setErrors({ invalidcontent: `${num} n'est pas un nombre valide` });
                return false;
            }
            arr.push(Number(num));
        }
        this.simplex.patchValue({
            loose: arr
        });
        arr = [];

        // check constants with M values
        console.debug(`Constants: ${lines[3]}`);
        if (lines[3].split(" ").length < M) {
            ctrl.setErrors({ invalidcontent: `pas assez de valeurs (${M} attendues, ${lines[3].split(" ").length} trouvées)` });
            return false;
        }
        for (const num of lines[3].split(" ")) {
            if (isNaN(Number(num))) {
                ctrl.setErrors({ invalidcontent: `${num} n'est pas un nombre valide` });
                return false;
            }
            arr.push(Number(num));
        }
        this.simplex.patchValue({
            constants: arr
        });
        arr = [];

        // check M number of constraints with N coefficients
        const subarr: number[][] = [];
        for (let i = 4; i < 4 + M; i++) {
            console.debug(`Coefs: ${lines[i]}`);
            if (lines[i].split(" ").length < N) {
                ctrl.setErrors({ invalidcontent: `pas assez de valeurs (${N} attendues, ${lines[i].split(" ").length} trouvées)` });
                return false;
            }
            for (const num of lines[i].split(" ")) {
                if (isNaN(Number(num))) {
                    ctrl.setErrors({ invalidcontent: `${num} n'est pas un nombre valide` });
                    return false;
                }
                // negate because we change from the file standard (SimplexForm) to the simplex standard
                arr.push(-Number(num));
            }
            subarr.push(arr);
            arr = [];
        }
        this.simplex.patchValue({
            coefs: subarr
        });

        // check objective Z with N coefficients
        console.debug(`Objectives: ${lines[4 + M]}`);
        if (lines[4 + M].split(" ").length < N + 1) {
            ctrl.setErrors({ invalidcontent: `pas assez de valeurs (${N + 1} attendues, ${lines[4 + M].split(" ").length} trouvées)` });
            return false;
        }
        for (const num of lines[4 + M].split(" ")) {
            if (isNaN(Number(num))) {
                ctrl.setErrors({ invalidcontent: `${num} n'est pas un nombre valide` });
                return false;
            }
            arr.push(Number(num));
        }
        this.simplex.patchValue({
            objective: arr
        });
        arr = [];

        // TODO sort indexes of the tight and loose values (also need to sort in the exact same order constraints, constant and objectives)
        // if needed, or we can change the given indexes in the order they're given

        return true;
    }

    /**
     * Update the latex preview of the given problem.
     */
    latexPreview() {
        if (this.simplex.controls.m.invalid || this.simplex.controls.n.invalid) { return; }
        const rawVal = this.simplex.getRawValue();

        // objectives
        this.latex = `P = \\begin{cases} \\text{${rawVal.toMaximise ? "max" : "min"}} \\space ${this.objName}(x) = & `;
        // do not add a + sign until we are after a non zero value
        let allZero = true;
        for (let i = 1; i <= rawVal.n; i++) {
            if (rawVal.objective[i] != 0 ||
                // or show if it's the last number and all precedents are 0
                (allZero && i == rawVal.n)
            ) {
                this.latex += `${formatNumber(rawVal.objective[i], { startingSign: !allZero })}${this.coefName}_{${i}}`;
                allZero = false;
            }
            this.latex += i == rawVal.n ? ` & ${formatNumber(rawVal.objective[0], { showOnes: true, startingSign: true })}\\\\` : " & ";
        }

        // constraints
        const sign = getSign(rawVal.toMaximise);
        for (let i = 0; i < rawVal.m; i++) {
            this.latex += ` & `;
            // do not add a + sign until we are after a non zero value
            let allZero = true;
            for (let j = 0; j < rawVal.n; j++) {
                if (rawVal.coefs[i][j] != 0 ||
                    // or show if it's the last number and all precedents are 0
                    (allZero && j == rawVal.n - 1)
                ) {
                    this.latex += `${formatNumber(rawVal.coefs[i][j], { startingSign: !allZero })}${this.varName}_{${j + 1}} & `;
                    allZero = false;
                } else {
                    this.latex += " & ";
                }
                this.latex += j == rawVal.n - 1 ? `${sign} ${formatNumber(rawVal.constants[i], { showOnes: true, noSpace: true })}` : "";
            }
            this.latex += "\\\\";
        }

        // all sup to 0
        // case where varName != coefName
        if (this.varName != this.coefName) {
            this.latex += `${this.coefName}_{i}, i \\in & \\{`;
            for (let i = 1; i <= rawVal.m; i++) {
                this.latex += `${i}${i == rawVal.m ? "" : ", & "}`;
            }
            this.latex += "\\} & \\geqslant 0";
            this.latex += `\\\\${this.varName}_{i}, i \\in & \\{`;
            for (let i = 1; i <= rawVal.n; i++) {
                this.latex += `${i}${i == rawVal.n ? "" : ", & "}`;
            }
            this.latex += "\\} & \\geqslant 0";
        } else {
            this.latex += `\\\\${this.varName}_{i}, i \\in & \\{`;
            for (let i = 1; i <= rawVal.n; i++) {
                this.latex += `${i}${i == rawVal.n ? "" : ", & "}`;
            }
            this.latex += "\\} & \\geqslant 0";
        }
        this.latex += "\\\\\\end{cases}";
        // case where varName == coefName
    }

    /**
     * Acces for the HTML to the getSign function.
     */
    public getSign(): string {
        return getSign(this.simplex.getRawValue().toMaximise);
    }

    // send data to the api and wait for a response
    public send() {
        this.api.simplex.solve(this.simplex.getRawValue()).subscribe({
            next: (res) => {
                // timeout
                if (res == null) {
                    this.snackBar.open("La requête a expiré", "Fermer", {
                        horizontalPosition: "right",
                        verticalPosition: "top",
                        duration: 10 * 1000,
                        announcementMessage: "La requête a expiré",
                        politeness: "polite"
                    });
                    return;
                }
                if (res.error != null && res.feasibility == SimplexFeasibility.UNKNWON) {
                    if (res.code != 500) {
                        // error with the file input
                        this.formInputFile.setErrors({ "error": res.error });
                    } else {
                        // error while solving
                        console.error(res.error);
                        this.snackBar.open("Une erreur est survenue côté serveur", "Fermer", {
                            horizontalPosition: "right",
                            verticalPosition: "top",
                            duration: 10 * 1000,
                            announcementMessage: "Une erreur est survenue côté serveur",
                            politeness: "polite"
                        });
                        this.simplexResponse = res;
                    }
                    return;
                }
                // no error
                this.simplexResponse = res;
            },
            error: (err) => {
                console.error(err);
                this.snackBar.open("Une erreur est survenue", "Fermer", {
                    horizontalPosition: "right",
                    verticalPosition: "top",
                    duration: 10 * 1000,
                    announcementMessage: "Une erreur est survenue",
                    politeness: "polite"
                });
            }
        });
    }

    /**
     * Reset the form to a default state.
     */
    public reset() {
        // BUG somehow, inputs are not visibly reseted to their default (but the form value are)
        this.simplex.reset();
        this.filename = "Chosissez un fichier";
        this.inputFile.value = "";
        this.simplexResponse = null;

        // The correct response computed for /tests/man.txt, to speed up development
        if (isDevMode()) {
            this.simplexResponse = {
                "error": null,
                "feasibility": 0,
                "optimum": "765 / 41",
                "values": [
                    "89 / 41",
                    "50 / 41",
                    "62 / 41"
                ],
                "code": 200,
                "status": "OK",
                "steps": [{ "out": null, "in": null, "twophase": false, "dualcut": false, "table": [["0", "3", "5", "4", "0", "0", "0"], ["8", "2", "3", "0", "1", "0", "0"], ["10", "0", "2", "5", "0", "1", "0"], ["15", "3", "2", "4", "0", "0", "1"]], "basicId": [3, 4, 5] }, { "out": 1, "in": 2, "twophase": false, "dualcut": false, "table": [["-40 / 3", "-1 / 3", "0", "4", "-5 / 3", "0", "0"], ["8 / 3", "2 / 3", "1", "0", "1 / 3", "0", "0"], ["14 / 3", "-4 / 3", "0", "5", "-2 / 3", "1", "0"], ["29 / 3", "5 / 3", "0", "4", "-2 / 3", "0", "1"]], "basicId": [2, 4, 5] }, { "out": 2, "in": 3, "twophase": false, "dualcut": false, "table": [["-256 / 15", "11 / 15", "0", "0", "-17 / 15", "-4 / 5", "0"], ["8 / 3", "2 / 3", "1", "0", "1 / 3", "0", "0"], ["14 / 15", "-4 / 15", "0", "1", "-2 / 15", "1 / 5", "0"], ["89 / 15", "41 / 15", "0", "0", "-2 / 15", "-4 / 5", "1"]], "basicId": [2, 3, 5] }, { "out": 3, "in": 1, "twophase": false, "dualcut": false, "table": [["-765 / 41", "0", "0", "0", "-45 / 41", "-24 / 41", "-11 / 41"], ["50 / 41", "0", "1", "0", "15 / 41", "8 / 41", "-10 / 41"], ["62 / 41", "0", "0", "1", "-6 / 41", "5 / 41", "4 / 41"], ["89 / 41", "1", "0", "0", "-2 / 41", "-12 / 41", "15 / 41"]], "basicId": [2, 3, 1] }, { "out": null, "in": null, "twophase": false, "dualcut": false, "table": [["-765 / 41", "0", "0", "0", "-45 / 41", "-24 / 41", "-11 / 41"], ["50 / 41", "0", "1", "0", "15 / 41", "8 / 41", "-10 / 41"], ["62 / 41", "0", "0", "1", "-6 / 41", "5 / 41", "4 / 41"], ["89 / 41", "1", "0", "0", "-2 / 41", "-12 / 41", "15 / 41"]], "basicId": [2, 3, 1] }],
                "labels": ["RHS", "L", "x0", "x1", "x2", "y0", "y1", "y2"]
            }
        }
    }
}
