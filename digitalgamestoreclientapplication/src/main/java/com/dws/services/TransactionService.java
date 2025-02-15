package com.dws.services;

import com.dws.entities.*;
import com.dws.services.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Arrays;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Calendar;

/**
 * Service class that handles communication with the Transaction REST API.
 * Manages game purchases, leases, and transaction history.
 * Coordinates with UserService and GameService for complete transaction processing.
 */
@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final RestTemplate restTemplate;
    private final String apiBaseUrl;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final UserService userService;
    private final GameService gameService;

    public TransactionService(
            RestTemplate restTemplate, 
            @Value("${api.base.url}") String apiBaseUrl,
            UserService userService,
            GameService gameService) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl + "/api/transactions";
        this.userService = userService;
        this.gameService = gameService;
    }

    /**
     * Retrieves all transactions from the system.
     *
     * @return List of all transactions
     * @throws RuntimeException if API communication fails
     */
    public List<Transaction> getAllTransactions() {
        try {
            Transaction[] transactions = restTemplate.getForObject(apiBaseUrl, Transaction[].class);
            return transactions != null ? Arrays.asList(transactions) : List.of();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error fetching transactions: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific transaction by its ID.
     *
     * @param id The unique identifier of the transaction
     * @return The requested Transaction object
     * @throws RuntimeException if transaction not found or API error occurs
     */
    public Transaction getTransaction(int id) {
        try {
            return restTemplate.getForObject(apiBaseUrl + "/{id}", Transaction.class, id);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error fetching transaction: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactionsByUserId(int userId) {
        try {
            Transaction[] transactions = restTemplate.getForObject(
                apiBaseUrl + "/user/{userId}", 
                Transaction[].class, 
                userId
            );
            return transactions != null ? Arrays.asList(transactions) : List.of();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error fetching user transactions: " + e.getMessage());
        }
    }

    /**
     * Creates a new transaction for game purchase or lease.
     * Validates user balance and game availability before processing.
     *
     * @param userId The ID of the user making the purchase/lease
     * @param gameId The ID of the game being purchased/leased
     * @param type The transaction type ("Purchase" or "Lease")
     * @return The created Transaction object
     * @throws RuntimeException if transaction creation fails or insufficient funds
     */
    public Transaction createTransaction(int userId, int gameId, String type) {
        try {
            User user = userService.getUser(userId);
            Game game = gameService.getGame(gameId);
            
            // Fix: Get correct price based on transaction type
            double cost = "Purchase".equals(type) ? game.getPrice() : game.getLeasePrice();
            logger.info("Creating {} transaction for game {}. Cost: ${}", type, gameId, cost);
            
            if (user.getAccountBalance() < cost) {
                throw new RuntimeException("Insufficient funds");
            }
            
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setGameId(gameId);
            transaction.setTransactionType(type);
            transaction.setAmount(cost);  // Set the correct amount based on transaction type
            
            String currentDate = dateFormat.format(new Date());
            transaction.setTransactionDate(currentDate);
            
            if ("Lease".equals(type)) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 30);
                transaction.setExpiryDate(dateFormat.format(calendar.getTime()));
            }
            
            // Create transaction first
            Transaction savedTransaction = restTemplate.postForObject(apiBaseUrl, transaction, Transaction.class);
            
            // Update balance only after successful transaction creation
            if (savedTransaction != null) {
                user.setAccountBalance(user.getAccountBalance() - cost);
                userService.updateUser(userId, user);
            }
            
            return savedTransaction;
        } catch (Exception e) {
            logger.error("Transaction creation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create transaction: " + e.getMessage());
        }
    }

    /**
     * Retrieves all transactions for a specific user.
     *
     * @param userId The ID of the user whose transactions to retrieve
     * @return List of transactions belonging to the user
     * @throws RuntimeException if retrieval fails or API error occurs
     */
    public List<Transaction> getUserTransactions(int userId) {
        try {
            Transaction[] transactions = restTemplate.getForObject(
                apiBaseUrl + "/user/{userId}", 
                Transaction[].class, 
                userId
            );
            
            return transactions != null ? Arrays.asList(transactions) : List.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user transactions: " + e.getMessage());
        }
    }

    // Helper method to get game title
    /**
     * Retrieves the title of a game by its ID.
     * Used for transaction history display.
     *
     * @param gameId The ID of the game to look up
     * @return The game's title or a fallback string if game not found
     */
    public String getGameTitle(int gameId) {
        try {
            Game game = gameService.getGame(gameId);
            return game.getTitle();
        } catch (Exception e) {
            logger.warn("Could not fetch game title for ID {}: {}", gameId, e.getMessage());
            return "Game " + gameId;
        }
    }
}
