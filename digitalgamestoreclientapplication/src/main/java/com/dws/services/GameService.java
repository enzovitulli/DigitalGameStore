package com.dws.services;

import com.dws.entities.Game;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class that handles communication with the Game REST API.
 * Manages game catalogue retrieval and game information.
 * Provides game data for the store frontend and transaction processing.
 */
@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public GameService(RestTemplate restTemplate, @Value("${api.base.url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl + "/api/games";
    }

    /**
     * Retrieves the complete catalogue of games from the API.
     * Logs the retrieval process and any errors that occur.
     *
     * @return List of all available games
     * @throws RuntimeException if API communication fails
     */
    public List<Game> getAllGames() {
        logger.info("Fetching all games from API");
        try {
            Game[] games = restTemplate.getForObject(apiBaseUrl, Game[].class);
            logger.debug("Successfully retrieved {} games", games != null ? games.length : 0);
            return games != null ? Arrays.asList(games) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching games: {}", e.getMessage());
            throw new RuntimeException("Error fetching games: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific game by its ID.
     *
     * @param id The unique identifier of the game
     * @return The requested Game object
     * @throws RuntimeException if game not found or API error occurs
     */
    public Game getGame(int id) {
        try {
            return restTemplate.getForObject(apiBaseUrl + "/{id}", Game.class, id);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error fetching game: " + e.getMessage());
        }
    }

    /**
     * Retrieves all unique game genres from the catalogue.
     * Used for game filtering and catalogue organization.
     *
     * @return Set of unique genre strings
     */
    public Set<String> getAvailableGenres() {
        return getAllGames().stream()
            .map(Game::getGenre)
            .collect(Collectors.toSet());
    }
}
