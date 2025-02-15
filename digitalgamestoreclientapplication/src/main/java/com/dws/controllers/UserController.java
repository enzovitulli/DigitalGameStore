package com.dws.controllers;

import com.dws.entities.User;
import com.dws.services.UserService;
import com.dws.services.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling user-related web requests.
 * Manages user registration, profile management, account settings, and session-based authentication.
 * Uses Thymeleaf templates for view rendering and RESTful communication with backend API.
 * Base path: /users
 *
 * Key features:
 * - User registration and profile management
 * - Account balance operations
 * - Transaction history viewing
 * - Account deletion
 */
@Controller
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final TransactionService transactionService;

    public UserController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    /**
     * Displays the user registration form.
     *
     * @param model Spring MVC Model object for passing data to the view
     * @return The registration form view name or error page if loading fails
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    /**
     * Processes the user registration form submission.
     *
     * @param user The User object populated from form data
     * @param redirectAttributes For adding flash messages
     * @return Redirect to login page on success or back to form on failure
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";  // Changed from /users/login to /login
        } catch (Exception e) {
            logger.error("Registration failed for user {}: {}", user.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/register";
        }
    }

    /**
     * Displays the user's profile page with their information and transaction history.
     *
     * @param model Spring MVC Model object for passing data to the view
     * @param session Current user's session
     * @return The profile view name or error page if loading fails
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";  // Changed from /users/login to /login
        }
        model.addAttribute("user", user);
        model.addAttribute("transactions", transactionService.getUserTransactions(user.getUserId()));
        return "users/profile";
    }

    /**
     * Displays the user's profile edit page.
     *
     * @param model Spring MVC Model object for passing data to the view
     * @param session Current user's session
     * @return The profile edit view name or error page if loading fails
     */
    @GetMapping("/edit-profile")  // Changed from profile/edit
    public String showEditProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "users/edit-profile";
    }

    /**
     * Processes profile updates including email and password changes.
     *
     * @param user The User object populated from form data
     * @param session Current user's session
     * @param redirectAttributes For adding flash messages
     * @return Redirect to profile page or error page
     */
    @PostMapping("/edit-profile")  // Changed from profile/edit
    public String updateProfile(@ModelAttribute User updatedUser, 
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return "redirect:/login";
            }

            // Preserve existing data while updating
            updatedUser.setUserId(currentUser.getUserId());
            updatedUser.setUsername(currentUser.getUsername());  // Username shouldn't change
            updatedUser.setAccountBalance(currentUser.getAccountBalance());
            
            // Now update the user
            userService.updateUser(currentUser.getUserId(), updatedUser);
            
            // Update session with new user data
            session.setAttribute("user", userService.getUser(currentUser.getUserId()));
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/users/profile";
        } catch (Exception e) {
            logger.error("Profile update failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/users/edit-profile";
        }
    }

    /**
     * Handles all profile-related actions including fund addition.
     * Uses action parameter to determine specific operation.
     *
     * @param action Type of profile action to perform
     * @param amount Amount to add for fund operations (optional)
     * @param session Current user session
     * @param redirectAttributes For flash messages
     * @return Redirect to appropriate page based on action result
     */
    @PostMapping("/profile")
    public String handleProfileActions(@RequestParam("action") String action,
                                     @RequestParam(name = "amount", required = false) Double amount,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            if ("addFunds".equals(action) && amount != null) {
                userService.addBalance(user.getUsername(), amount);
                // Refresh user in session with new balance
                session.setAttribute("user", userService.getUser(user.getUserId()));
                redirectAttributes.addFlashAttribute("success", 
                    String.format("Successfully added $%.2f to your account", amount));
            }
        } catch (Exception e) {
            logger.error("Error processing profile action {}: {}", action, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to process request: " + e.getMessage());
        }
        
        return "redirect:/users/profile";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /**
     * Processes user account deletion.
     * Removes user data and invalidates current session.
     *
     * @param session Current user session
     * @param redirectAttributes For flash messages
     * @return Redirect to login page with status message
     */
    @PostMapping("/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            userService.deleteUser(user.getUserId());
            session.invalidate();  // Clear the session after successful deletion
            redirectAttributes.addFlashAttribute("success", "Your account has been successfully deleted.");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Account deletion failed for user {}: {}", user.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete account: " + e.getMessage());
            return "redirect:/users/profile";
        }
    }
}
