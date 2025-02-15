package com.dws.controllers;

import com.dws.entities.Game;
import com.dws.services.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller handling game-related web requests.
 * Manages game catalogue display, game details viewing, and search functionality.
 * Requires authenticated users through session management.
 * Uses Thymeleaf templates for view rendering.
 * Base path: /games
 */
@Controller
@RequestMapping("/games")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Displays the list of all games and available genres.
     * Requires authenticated user session.
     *
     * @param model Model for passing data to view
     * @param session HTTP session for authentication check
     * @return Games list view or redirect to login
     */
    @GetMapping
    public String listGames(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        try {
            model.addAttribute("games", gameService.getAllGames());
            model.addAttribute("genres", gameService.getAvailableGenres());
            return "games/list";
        } catch (Exception e) {
            logger.error("Error loading games list: {}", e.getMessage());
            model.addAttribute("error", "Unable to load games. Please try again later.");
            return "error";
        }
    }

    /**
     * Displays detailed information for a specific game.
     * Shows game details, price, and purchase/lease options.
     *
     * @param gameId ID of the game to display
     * @param model Spring Model for passing data to view
     * @return Game details view or error page if loading fails
     */
    @GetMapping("/{gameId}")
    public String getGameDetails(@PathVariable("gameId") int gameId, Model model) {
        try {
            Game game = gameService.getGame(gameId);
            model.addAttribute("game", game);
            return "games/details";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading game details: " + e.getMessage());
            return "error";
        }
    }
}
