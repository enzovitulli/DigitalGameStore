package dws.repositories;

import dws.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Transaction entity operations.
 * Provides CRUD operations and custom queries for Transaction management.
 * Extends JpaRepository to inherit basic database operations.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    /**
     * Finds all transactions for a specific user.
     * @param userId The ID of the user whose transactions to retrieve
     * @return List of transactions belonging to the user
     */
    List<Transaction> findByUserId(int userId);
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