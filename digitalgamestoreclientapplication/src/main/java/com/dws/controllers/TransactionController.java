package com.dws.controllers;

import com.dws.entities.Transaction;
import com.dws.entities.User;
import com.dws.services.TransactionService;
import com.dws.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class that handles web requests related to transactions.
 * This controller manages user purchases, leases, and transaction history views.
 * Uses Thymeleaf templates to render views and interacts with the TransactionService.
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    /**
     * Displays all transactions for the currently authenticated user.
     *
     * @param model Spring MVC Model object for passing data to the view
     * @param session HttpSession object containing user details
     * @return The name of the view template to render
     */
    @GetMapping
    public String getUserTransactions(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            model.addAttribute("transactions", 
                transactionService.getTransactionsByUserId(user.getUserId()));
            return "transactions/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading transactions: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Processes a game purchase transaction.
     *
     * @param gameId ID of the game to purchase
     * @param session HttpSession object containing user details
     * @param redirectAttributes Spring MVC redirect attributes for flash messages
     * @return Redirect URL after processing the purchase
     */
    @PostMapping("/purchase/{gameId}")
    public String purchaseGame(
        @PathVariable("gameId") int gameId,
        HttpSession session,
        RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            transactionService.createTransaction(user.getUserId(), gameId, "Purchase");
            session.setAttribute("user", userService.getUser(user.getUserId()));
            logger.info("Game {} purchased successfully by user {}", gameId, user.getUsername());
            redirectAttributes.addFlashAttribute("success", "Game purchased successfully!");
            return "redirect:/users/profile";
        } catch (Exception e) {
            logger.error("Purchase failed for game {} by user {}: {}", 
                gameId, user.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Purchase failed: " + e.getMessage());
            return "redirect:/games/" + gameId;
        }
    }

    /**
     * Processes a game lease transaction.
     *
     * @param gameId ID of the game to lease
     * @param session HttpSession object containing user details
     * @param redirectAttributes Spring MVC redirect attributes for flash messages
     * @return Redirect URL after processing the lease
     */
    @PostMapping("/lease/{gameId}")
    public String leaseGame(
        @PathVariable("gameId") int gameId,
        HttpSession session,
        RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            transactionService.createTransaction(user.getUserId(), gameId, "Lease");
            session.setAttribute("user", userService.getUser(user.getUserId()));
            logger.info("Game {} leased successfully by user {}", gameId, user.getUsername());
            redirectAttributes.addFlashAttribute("success", "Game leased successfully!");
            return "redirect:/users/profile";
        } catch (Exception e) {
            logger.error("Lease failed for game {} by user {}: {}", 
                gameId, user.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lease failed: " + e.getMessage());
            return "redirect:/games/" + gameId;
        }
    }

    /**
     * Displays details for a specific transaction.
     *
     * @param id ID of the transaction to display
     * @param model Spring MVC Model object for passing data to the view
     * @return The name of the view template to render
     */
    @GetMapping("/{id}")
    public String getTransactionDetails(@PathVariable int id, Model model) {
        try {
            Transaction transaction = transactionService.getTransaction(id);
            model.addAttribute("transaction", transaction);
            return "transactions/details";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading transaction details: " + e.getMessage());
            return "error";
        }
    }

}
