package dws.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "Game")
@Data
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gameId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String genre;
    
    @Column(nullable = false)
    private String developer;
    
    @Column(nullable = false)
    private LocalDate releaseDate;
    
    @Column(nullable = false)
    private double price;
    
    @Column(nullable = false)
    private double leasePrice;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
}
