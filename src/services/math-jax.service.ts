import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

// Declare MathJax as a global variable so that it can be used in this TypeScript file
declare global {
    interface Window {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        MathJax: any;
    }
}
// TODO KaTeX?

@Injectable({
    providedIn: 'root'
})
export class MathJaxService {
    public readonly InlineOpenBracket = "\\(";
    public readonly InlineCloseBracket = "\\)";
    public readonly OpenBracket = "$$";
    public readonly CloseBracket = "$$";

    // A variable to check if MathJax was successfully loaded
    private mathJaxLoaded = false;
    public mathJaxLoading: Subject<boolean> = new Subject<boolean>();

    // Configure which MathJax version we want
    private mathJax = {
        source: 'https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js',
    }

    constructor() {
        this.mathJaxLoading.subscribe((res) => {
            if (!res) { return; }
            this.render();
        });

        // from the documentation, set a config before loading the script
        this.loadConfig();
        this.loadMathJax().then((res) => {
            this.mathJaxLoaded = res;
        }).catch((err) => {
            this.mathJaxLoaded = false;
            console.error(err);
        }).finally(() => {
            console.info("Finished MathJax setup");
            this.mathJaxLoading.next(this.mathJaxLoaded);
        });
    }

    // This method is used by the MathJaxDirective to check if MathJax is loaded
    public getMathJaxLoaded(): boolean {
        return this.mathJaxLoaded;
    }

    private async loadMathJax(): Promise<boolean> {
        return new Promise((resolve, reject) => {
            console.log('Loading MathJax');

            const script: HTMLScriptElement = document.createElement('script');
            script.type = 'text/javascript';
            script.src = this.mathJax.source;
            script.async = true;

            // Once the script is loaded, resolve the promise
            script.onload = () => {
                console.info("MathJax loaded");
                resolve(true)
            };

            // If there's an error, reject the promise
            script.onerror = () => {
                console.info("Error loading MathJax");
                reject(false);
            }

            document.head.appendChild(script); // Append the script to start loading it
        });
    }

    private loadConfig() {
        // TODO https://docs.mathjax.org/en/latest/web/configuration.html#using-a-local-file-for-configuration
        window.MathJax = {
            showMathMenu: false,
            tex2jax: { inlineMath: [["\\(", "\\)"]], displayMath: [["$$", "$$"]] },
            menuSettings: { zoom: "Double-Click", zscale: "150%" },
            CommonHTML: { linebreaks: { automatic: true } },
            "HTML-CSS": { linebreaks: { automatic: true } },
            SVG: { linebreaks: { automatic: true } },
            loader: { load: ['[tex]/color'] },
            tex: { packages: { '[+]': ['color'] } }
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
        } as any;
    }

    render() {
        window.MathJax.startup.promise.then(() => {
            console.debug('Typesetting LaTex');
            window.MathJax.typesetPromise();
        });
    }
}
