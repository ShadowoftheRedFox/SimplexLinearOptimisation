package optim.optim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import optim.optim.controller.body.SimplexForm;
import optim.optim.response.SimplexResponse;
import optim.optim.service.SimplexService;

/**
 * Main controller. Manage the main routes.
 */
@RestController
public class MainController {
    /** Default constructor. */
    public MainController() {
    }

    /**
     * Service used to manage the simplex resolution method.
     */
    @Autowired
    private SimplexService simplexService;

    /**
     * Endpoint for simplex resolution.
     *
     * @param simplexForm The requested form.
     * @return A {@link SimplexResponse} to string.
     */
    @CrossOrigin
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String postController(@RequestBody SimplexForm simplexForm) {
        SimplexResponse res = new SimplexResponse();
        if (simplexForm == null) {
            return res.setStatus(HttpStatus.NOT_ACCEPTABLE).toString();
        }

        return simplexService.solve(simplexForm).toString();
    }
}
