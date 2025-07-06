import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../environments/environment';
import { SimplexForm, SimplexResponse } from '../models/APIModels';
import { catchError, timeout } from "rxjs/operators"
import { of, TimeoutError } from 'rxjs';

const ApiUrl = environment.API_URL + ":" + environment.API_PORT + "/";

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    readonly http = inject(HttpClient);

    readonly simplex = {
        solve: (content: SimplexForm) => {
            return this.sendApiRequest<SimplexResponse>(
                "POST",
                "",
                content,
                "Sending simplex inputs",
                environment.TIMEOUTS.SIMPLEX
            )
        },
    }

    private sendApiRequest<T>(
        method: "GET" | "POST" | "PUT" | "DELETE" | "PATCH",
        endpoint: string,
        parameters: object = {},
        message?: string,
        maxTimeout?: number
    ) {
        const urlParameters = parameters != undefined && Object.keys(parameters).length > 0
            ? "?data=" + JSON.stringify(parameters)
            : "";

        if (message !== undefined && message != null && message.length > 0) {
            console.info("[API] " + message);
        }

        // default timeout of 3s
        if (!maxTimeout) {
            maxTimeout = environment.TIMEOUTS.DEFAULT;
        }

        switch (method) {
            case "GET":
                return this.http.get<T>(ApiUrl + endpoint + urlParameters).pipe<T, T | null>(
                    timeout(maxTimeout),
                    catchError((error) => {
                        if (error instanceof TimeoutError) {
                            console.error(`Request to ${endpoint} timed out after ${maxTimeout}ms`);
                            return of(null);
                        }
                        // return non timeout error
                        return of(error);
                    })
                );
            case "POST":
                return this.http.post<T>(ApiUrl + endpoint, parameters).pipe<T, T | null>(
                    timeout(maxTimeout),
                    catchError((error) => {
                        if (error instanceof TimeoutError) {
                            console.error(`Request to ${endpoint} timed out after ${maxTimeout}ms`);
                            return of(null);
                        }
                        // return non timeout error
                        return of(error);
                    })
                );
            case "PUT":
                return this.http.put<T>(ApiUrl + endpoint, parameters).pipe<T, T | null>(
                    timeout(maxTimeout),
                    catchError((error) => {
                        if (error instanceof TimeoutError) {
                            console.error(`Request to ${endpoint} timed out after ${maxTimeout}ms`);
                            return of(null);
                        }
                        // return non timeout error
                        return of(error);
                    })
                );
            case "PATCH":
                return this.http.patch<T>(ApiUrl + endpoint, parameters).pipe<T, T | null>(
                    timeout(maxTimeout),
                    catchError((error) => {
                        if (error instanceof TimeoutError) {
                            console.error(`Request to ${endpoint} timed out after ${maxTimeout}ms`);
                            return of(null);
                        }
                        // return non timeout error
                        return of(error);
                    })
                );
            case "DELETE":
                return this.http.delete<T>(ApiUrl + endpoint, parameters).pipe<T, T | null>(
                    timeout(maxTimeout),
                    catchError((error) => {
                        if (error instanceof TimeoutError) {
                            console.error(`Request to ${endpoint} timed out after ${maxTimeout}ms`);
                            return of(null);
                        }
                        // return non timeout error
                        return of(error);
                    })
                );
        }
    }
}
