package dws.controllers;

import dws.entities.User;
import dws.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Controller for managing User entities.
 * Provides endpoints for user authentication, registration, and profile management.
 * Base URL path: /api/users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user with username and password.
     *
     * @param credentials A map containing the user's username and password
     * @return ResponseEntity containing the authenticated User
     * @throws ResponseStatusException with UNAUTHORIZED if credentials are invalid
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if authentication fails
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        logger.info("Login attempt for user: {}", username);
        try {
            return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(user -> {
                    logger.info("Successful login for user: {}", username);
                    return ResponseEntity.ok(user);
                })
                .orElseThrow(() -> {
                    logger.warn("Failed login attempt for user: {}", username);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });
        } catch (Exception e) {
            logger.error("Login error for user {}: {}", username, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login error");
        }
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param userId The unique identifier of the user
     * @return ResponseEntity containing the User if found
     * @throws ResponseStatusException with NOT_FOUND if user doesn't exist
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable int userId) {
        logger.info("Fetching user with ID: {}", userId);
        try {
            return userRepository.findById(userId)
                .map(user -> {
                    logger.info("User found with ID: {}", userId);
                    return ResponseEntity.ok(user);
                })
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        String.format("User with ID %d not found", userId));
                });
        } catch (Exception e) {
            logger.error("Error fetching user with ID {}: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                String.format("Failed to retrieve user with ID %d", userId), e);
        }
    }

    /**
     * Retrieves all users from the database.
     *
     * @return List of all User entities
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if database access fails
     */
    @GetMapping
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve users", e);
        }
    }

    /**
     * Creates a new user account.
     *
     * @param user The User entity to create
     * @return The created User entity
     * @throws ResponseStatusException with BAD_REQUEST if username/email already exists
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if user creation fails
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        logger.info("Creating new user with username: {}", user.getUsername());
        try {
            if (user == null) {
                logger.warn("User data is null");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User data cannot be null");
            }
            if (userRepository.existsByUsername(user.getUsername())) {
                logger.warn("Username already exists: {}", user.getUsername());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                logger.warn("Email already exists: {}", user.getEmail());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }
            // The password comes already encoded from the client
            User savedUser = userRepository.save(user);
            logger.info("User created with ID: {}", savedUser.getUserId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to create user", e);
        }
    }

    /**
     * Updates an existing user's information.
     *
     * @param userId The ID of the user to update
     * @param user The updated User entity
     * @return The updated User entity
     * @throws ResponseStatusException with NOT_FOUND if user doesn't exist
     */
    @PutMapping("/{userId}")
    public User updateUser(@PathVariable int userId, @RequestBody User user) {
        logger.info("Updating user with ID: {}", userId);
        try {
            if (!userRepository.existsById(userId)) {
                logger.warn("User not found with ID: {}", userId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format("User with ID %d not found", userId));
            }
            user.setUserId(userId);
            // Remove password encoding check and just save the user as is
            User updatedUser = userRepository.save(user);
            logger.info("User updated with ID: {}", userId);
            return updatedUser;
        } catch (Exception e) {
            logger.error("Error updating user with ID {}: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                String.format("Failed to update user with ID %d", userId), e);
        }
    }

    /**
     * Deletes a user account.
     *
     * @param userId The ID of the user to delete
     * @throws ResponseStatusException with NOT_FOUND if user doesn't exist
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int userId) {
        logger.info("Deleting user with ID: {}", userId);
        try {
            if (!userRepository.existsById(userId)) {
                logger.warn("User not found with ID: {}", userId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format("User with ID %d not found", userId));
            }
            // Transactions will be deleted automatically due to CASCADE configuration in entity
            userRepository.deleteById(userId);
            logger.info("User and related transactions deleted for ID: {}", userId);
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                String.format("Failed to delete user with ID %d", userId), e);
        }
    }
}