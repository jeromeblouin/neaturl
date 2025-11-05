package org.neaturl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This URL shortener service requires a PostgresQL database to run.
 * The DB schema is automatically created if it doesn't exist.
 */
@SpringBootApplication
public class App {

    static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
