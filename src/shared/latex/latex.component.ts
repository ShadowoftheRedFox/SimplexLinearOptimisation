import { Component, inject, Input, OnChanges } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { MathJaxService } from '../../services/math-jax.service';

@Component({
    selector: 'app-latex',
    templateUrl: './latex.component.html',
    styleUrls: ['./latex.component.scss']
})
export class LatexComponent implements OnChanges {
    readonly mathLatex = inject(MathJaxService);

    /**
     * Switch between inlined of centered math lines.
     * Default false.
     */
    @Input() inlined = false;
    /**
     * Text to display in latex. It is automatically put between the correct brackets.
     */
    @Input({ required: true }) set content(value: string) {
        if (value.trim().length == 0) {
            this._content = "";
            return;
        }
        if (this.inlined) {
            this._content = this.mathLatex.InlineOpenBracket + value + this.mathLatex.InlineCloseBracket;
        } else {
            this._content = this.mathLatex.OpenBracket + value + this.mathLatex.CloseBracket;
        }
    }
    get content(): string {
        return this._content;
    }

    // underlined content to prevent recursive change loop
    _content = "";

    ngOnChanges(changes: SimpleChanges) {
        // to render math equations again on content change
        if (changes['content']) {
            if (this.mathLatex.getMathJaxLoaded() === true) {
                this.mathLatex.render();
            }
        }
    }
}
