package dws.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import dws.entities.*;
import dws.repositories.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * REST Controller for managing Transaction entities.
 * Provides endpoints for transaction processing and history retrieval.
 * All endpoints require proper validation of users, games, and balances.
 * Base URL path: /api/transactions
 */
@RestController
@RequestMapping("/api/transactions") 
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public TransactionController(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            GameRepository gameRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Retrieves a specific transaction by its ID.
     *
     * @param transactionId The unique identifier of the transaction
     * @return ResponseEntity containing the Transaction if found
     * @throws ResponseStatusException with NOT_FOUND if transaction doesn't exist
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if database access fails
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable int transactionId) {
        try {
            return transactionRepository.findById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                String.format("Failed to retrieve transaction with ID %d", transactionId), e);
        }
    }

    /**
     * Retrieves all transactions from the database.
     *
     * @return List of all Transaction entities
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if database access fails
     */
    @GetMapping
    public List<Transaction> getAllTransactions() {
        try {
            return transactionRepository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to retrieve transactions", e);
        }
    }

    /**
     * Creates a new transaction for a game purchase or lease.
     * Validates user balance and updates it accordingly.
     *
     * @param transaction Transaction object containing purchase/lease details
     * @return ResponseEntity containing the created Transaction
     * @throws ResponseStatusException with NOT_FOUND if user or game don't exist
     * @throws ResponseStatusException with BAD_REQUEST if user has insufficient funds
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if transaction creation fails
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        logger.info("Creating transaction: User {} for game {}", 
            transaction.getUserId(), transaction.getGameId());
        try {
            // Validate user and game existence
            User user = userRepository.findById(transaction.getUserId())
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", transaction.getUserId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
            
            Game game = gameRepository.findById(transaction.getGameId())
                .orElseThrow(() -> {
                    logger.warn("Game not found: {}", transaction.getGameId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
                });

            // Calculate cost and check balance
            double cost = "Purchase".equals(transaction.getTransactionType()) 
                ? game.getPrice() : game.getLeasePrice();

            if (user.getAccountBalance() < cost) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
            }

            // Update user balance
            user.setAccountBalance(user.getAccountBalance() - cost);
            userRepository.save(user);

            // Format dates as strings
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            transaction.setTransactionDate(dateFormat.format(new Date()));
            
            if ("Lease".equals(transaction.getTransactionType())) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 30);
                transaction.setExpiryDate(dateFormat.format(calendar.getTime()));
            }
            
            transaction.setAmount(cost);

            logger.info("Transaction created successfully");
            return ResponseEntity.ok(transactionRepository.save(transaction));
        } catch (Exception e) {
            logger.error("Transaction creation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to create transaction: " + e.getMessage());
        }
    }

    /**
     * Deletes an existing transaction.
     *
     * @param transactionId The unique identifier of the transaction to delete
     * @throws ResponseStatusException with NOT_FOUND if transaction doesn't exist
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if deletion fails
     */
    @DeleteMapping("/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable int transactionId) {
        try {
            if (!transactionRepository.existsById(transactionId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format("Transaction with ID %d not found", transactionId));
            }
            transactionRepository.deleteById(transactionId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                String.format("Failed to delete transaction with ID %d", transactionId), e);
        }
    }

    /**
     * Retrieves all transactions for a specific user.
     *
     * @param userId The unique identifier of the user
     * @return List of Transaction entities belonging to the user
     * @throws ResponseStatusException with NOT_FOUND if user doesn't exist
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if retrieval fails
     */
    @GetMapping("/user/{userId}")
    public List<Transaction> getTransactionsByUserId(@PathVariable int userId) {
        try {
            if (!userRepository.existsById(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format("User with ID %d not found", userId));
            }
            // Updated to use the correct method name
            return transactionRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                String.format("Failed to retrieve transactions for user with ID %d", userId), e);
        }
    }

}

/**
 * Data Transfer Object for transaction creation requests.
 * Used to validate and process new transaction requests.
 * Separates API contract from internal Transaction entity.
 *
 * Example JSON request:
 * {
 *   "userId": 1,
 *   "gameId": 2,
 *   "transactionType": "Purchase" or "Lease"
 * }
 */
class TransactionRequest {
    private int userId;          // ID of the user making the transaction
    private int gameId;          // ID of the game being purchased or leased
    private String transactionType;  // Type of transaction: "Purchase" or "Lease"

    // Standard getters and setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
}