import { Routes } from '@angular/router';
import { environment } from '../environments/environment';
import { MainComponent } from './main/main.component';
import { GraphComponent } from './graph/graph.component';

const FormatedTitle = " - " + environment.TITLE;

export const routes: Routes = [
    {
        title: environment.TITLE,
        path: "",
        component: MainComponent
    },
    {
        title: "Représentation géométrique" + FormatedTitle,
        path: "graph",
        component: GraphComponent
    },
    {
        // Error 404, redirect to main
        title: "Erreur 404" + FormatedTitle,
        path: "**",
        redirectTo: ""
    }
];
