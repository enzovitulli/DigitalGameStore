package dws.repositories;

import dws.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides CRUD operations and custom queries for User management.
 * Extends JpaRepository to inherit basic database operations.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Finds a user by their username.
     * @param username The username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a username already exists.
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email already exists.
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}

/* What a Repository Does in Spring Boot:
 * A repository in Spring Data JPA acts as an abstraction over the database, allowing you to:
 * 
 * - Query, save, delete, or update rows in the database without writing raw SQL.
 * - Use built-in methods (save(), findById(), delete()) and extend them with custom queries (e.g., findByName()). 
 * 
 * Each repository manages CRUD operations for one entity. 
 * If you have multiple entities, each one typically has its own table in the database, 
 * and a repository for those specific operations is necessary.
 */