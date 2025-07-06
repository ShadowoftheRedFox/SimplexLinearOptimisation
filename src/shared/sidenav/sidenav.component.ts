import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

interface Item {
    name: string;
    route: string;
    icon?: string;
    tip?: string;
}

/**
 * Component used to travel between the different modules available.
 */
@Component({
    selector: 'app-sidenav',
    imports: [
        MatButtonModule,
        MatIconModule,
        MatSidenavModule,
        MatTooltipModule,
        RouterLink,
    ],
    templateUrl: './sidenav.component.html',
    styleUrl: './sidenav.component.scss'
})
export class SidenavComponent {
    public items: Item[] = [
        {
            name: "Simplex",
            route: "/",
            tip: "Résolution avec la méthode du simplex",
            icon: "function"
        },
        {
            name: "Graphe",
            route: "/graph",
            tip: "Affichage d'un graphe selon un problème linéaire",
            icon: "shape_line"
        },
    ];
}
