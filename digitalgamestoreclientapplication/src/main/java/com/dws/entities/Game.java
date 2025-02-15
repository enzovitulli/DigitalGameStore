package com.dws.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    private int gameId;
    private String title;
    private String genre;
    private String developer;
    private String releaseDate;
    private double price;
    private double leasePrice;
    private String description;
}
