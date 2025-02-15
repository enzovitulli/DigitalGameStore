package dws.controllers;

import dws.entities.Game;
import dws.repositories.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST Controller for managing Game entities.
 * Provides endpoints for CRUD operations on games and game searching functionality.
 * Base URL path: /api/games
 */
@RestController
@RequestMapping("/api/games")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final GameRepository gameRepository;

    public GameController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Retrieves all games from the database.
     *
     * @return List of all Game entities
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if database access fails
     */
    @GetMapping
    public List<Game> getAllGames() {
        logger.info("Fetching all games");
        try {
            return gameRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching games: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching games");
        }
    }

    /**
     * Retrieves a specific game by its ID.
     *
     * @param id The unique identifier of the game
     * @return ResponseEntity containing the Game if found
     * @throws ResponseStatusException with NOT_FOUND if game doesn't exist
     * @throws ResponseStatusException with INTERNAL_SERVER_ERROR if database access fails
     */
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable int id) {
        logger.info("Fetching game with id: {}", id);
        try {
            return gameRepository.findById(id)
                .map(game -> {
                    logger.debug("Found game: {}", game.getTitle());
                    return ResponseEntity.ok(game);
                })
                .orElseThrow(() -> {
                    logger.warn("Game not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
                });
        } catch (Exception e) {
            logger.error("Error fetching game {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching game");
        }
    }

    /**
     * Searches for games by genre.
     *
     * @param genre Optional genre to filter games by
     * @return List of games matching the genre, or all games if no genre specified
     */
    @GetMapping("/search")
    public List<Game> searchGames(@RequestParam(required = false) String genre) {
        if (genre != null && !genre.isEmpty()) {
            return gameRepository.findByGenreContaining(genre);
        }
        return gameRepository.findAll();
    }

    /**
     * Creates a new game in the database.
     *
     * @param game The Game entity to create
     * @return The created Game entity
     * @throws ResponseStatusException with BAD_REQUEST if game ID is provided
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Game createGame(@RequestBody Game game) {
        if (game.getGameId() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID must not be provided");
        }
        return gameRepository.save(game);
    }

    /**
     * Updates an existing game.
     *
     * @param id The ID of the game to update
     * @param game The updated Game entity
     * @return The updated Game entity
     * @throws ResponseStatusException with NOT_FOUND if game doesn't exist
     */
    @PutMapping("/{id}")
    public Game updateGame(@PathVariable int id, @RequestBody Game game) {
        if (!gameRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        game.setGameId(id);
        return gameRepository.save(game);
    }

    /**
     * Deletes a game from the database.
     *
     * @param id The ID of the game to delete
     * @throws ResponseStatusException with NOT_FOUND if game doesn't exist
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable int id) {
        if (!gameRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        gameRepository.deleteById(id);
    }
}
