package optim.optim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import optim.optim.src.Config;

/**
 * Spring application.
 */
@SpringBootApplication
public class SimplexApplication {
    /** Default constructor. */
    public SimplexApplication() {
    }

    /**
     * Entry of the application.
     *
     * @param args Arguments.
     */
    public static void main(String[] args) {
        // load config
        Config.load();
        // launch the application
        SpringApplication.run(SimplexApplication.class, args);
    }

}
