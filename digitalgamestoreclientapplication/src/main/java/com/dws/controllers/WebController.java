package com.dws.controllers;

import com.dws.entities.User;
import com.dws.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller handling basic web navigation and error pages.
 * Manages root path routing and session-based authentication checks.
 * This controller serves as the entry point for the web application.
 */
@Controller
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    private final UserService userService;

    // Add constructor
    public WebController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles requests to the login page.
     * Redirects authenticated users to the games page.
     *
     * @param session The HTTP session to check authentication status
     * @return Login page view or redirect to games page for authenticated users
     */
    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/games";
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username, 
                             @RequestParam("password") String password,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("user", user);
            return "redirect:/games";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/login";
        }
    }

    /**
     * Handles requests to the root path (/).
     * Redirects to appropriate page based on authentication status.
     *
     * @param session The HTTP session to check authentication status
     * @return Redirect to games page for authenticated users or login page for others
     */
    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("user") == null) {
            logger.debug("Unauthenticated access to home page, redirecting to login");
            return "redirect:/login";
        }
        logger.debug("Redirecting authenticated user to games page");
        return "redirect:/games";
    }

    /**
     * Handles requests to the error page.
     * Called when unhandled exceptions occur in the application.
     *
     * @return Error page view
     */
    @GetMapping("/error")
    public String error() {
        logger.error("Global error handler invoked");
        return "error";
    }
}
