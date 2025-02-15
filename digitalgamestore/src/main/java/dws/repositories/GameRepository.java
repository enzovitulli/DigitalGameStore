package dws.repositories;

import dws.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Game entity operations.
 * Provides CRUD operations and custom queries for Game management.
 * Extends JpaRepository to inherit basic database operations.
 */
public interface GameRepository extends JpaRepository<Game, Integer> {
    /**
     * Finds games by partial genre match.
     * @param genre The genre string to search for
     * @return List of games matching the genre
     */
    List<Game> findByGenreContaining(String genre);

    /**
     * Finds games by partial title match, case insensitive.
     * @param title The title string to search for
     * @return List of games matching the title
     */
    List<Game> findByTitleContainingIgnoreCase(String title);
}