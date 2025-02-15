package dws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DigitalGameStoreWebServicesApplication {
    public static void main(String[] args) {
        SpringApplication.run(DigitalGameStoreWebServicesApplication.class, args);
    }
    /* What does the @SpringBootApplication annotation do?:
     * The @SpringBootApplication annotation is a combination of three annotations:
     * 
     * 1- @EnableAutoConfiguration
     * Automatically configures your application based on the dependencies you’ve included in your pom.xml or build.gradle.
     * 
     * 2- @ComponentScan
     * Scans your package and its sub-packages for Spring components (@RestController, @Service, @Repository, etc.) to register them in the application context.
     * 
     * 3-@Configuration
     * Allows you to define beans and configurations in this class if necessary.
     */
    
}
