package com.dws.services;

import com.dws.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class that handles communication with the User REST API.
 * Provides methods for user management including registration, profile updates,
 * and balance management.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public UserService(RestTemplate restTemplate, @Value("${api.base.url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl + "/api/users";
    }

    /**
     * Authenticates a user with the given username and password.
     *
     * @param username The username of the user
     * @param password The password of the user
     * @return The authenticated User object
     * @throws RuntimeException if the credentials are invalid or there's an API error
     */
    public User login(String username, String password) {
        logger.info("Attempting login for user: {}", username);
        try {
            // Change to POST request
            return restTemplate.postForObject(
                apiBaseUrl + "/login",
                Map.of("username", username, "password", password),
                User.class
            );
        } catch (HttpClientErrorException e) {
            logger.error("Login failed for user {}: {} {}", username, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Invalid credentials");
        }
    }

    /**
     * Retrieves all users from the system.
     *
     * @return List of all User objects
     * @throws RuntimeException if there's an error communicating with the API
     */
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        try {
            User[] users = restTemplate.getForObject(apiBaseUrl, User[].class);
            return users != null ? Arrays.asList(users) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching users: {}", e.getMessage());
            throw new RuntimeException("Error fetching users: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param id The unique identifier of the user
     * @return The requested User object
     * @throws RuntimeException if the user is not found or there's an API error
     */
    public User getUser(int id) {
        logger.info("Fetching user with ID: {}", id);
        try {
            return restTemplate.getForObject(apiBaseUrl + "/{id}", User.class, id);
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching user with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error fetching user: " + e.getMessage());
        }
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for
     * @return The matching User object
     * @throws RuntimeException if the user is not found or there's an API error
     */
    public User getUserByUsername(String username) {
        logger.info("Fetching user with username: {}", username);
        try {
            return getAllUsers().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        } catch (Exception e) {
            logger.error("Error fetching user with username {}: {}", username, e.getMessage());
            throw new RuntimeException("Error fetching user by username: " + e.getMessage());
        }
    }

    /**
     * Creates a new user account.
     *
     * @param user The User object containing registration details
     * @return The created User object
     * @throws RuntimeException if user creation fails or there's an API error
     */
    public User createUser(User user) {
        logger.info("Creating user: {}", user.getUsername());
        try {
            return restTemplate.postForObject(apiBaseUrl, user, User.class);
        } catch (HttpClientErrorException e) {
            logger.error("Error creating user {}: {}", user.getUsername(), e.getMessage());
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Updates a user's information.
     *
     * @param id The unique identifier of the user
     * @param user The User object containing updated details
     * @throws RuntimeException if user update fails or there's an API error
     */
    public void updateUser(int id, User user) {
        logger.info("Updating user with ID: {}", id);
        try {
            restTemplate.put(apiBaseUrl + "/{id}", user, id);
        } catch (HttpClientErrorException e) {
            logger.error("Error updating user with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error updating user: " + e.getMessage());
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The unique identifier of the user
     * @throws RuntimeException if user deletion fails or there's an API error
     */
    public void deleteUser(int id) {
        logger.info("Deleting user with ID: {}", id);
        try {
            restTemplate.delete(apiBaseUrl + "/{id}", id);
        } catch (HttpClientErrorException e) {
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    /**
     * Adds funds to a user's account balance.
     *
     * @param username Username of the user
     * @param amount Amount to add
     * @throws RuntimeException if update fails or there's an API error
     */
    public void addBalance(String username, double amount) {
        logger.info("Adding balance for user: {}", username);
        try {
            User user = getUserByUsername(username);
            user.setAccountBalance(user.getAccountBalance() + amount);
            updateUser(user.getUserId(), user);
        } catch (HttpClientErrorException e) {
            logger.error("Error updating balance for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Error updating balance: " + e.getMessage());
        }
    }
}
