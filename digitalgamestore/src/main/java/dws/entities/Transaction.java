package dws.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Transaction")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;
    
    @Column(nullable = false)
    private int userId;
    
    @Column(nullable = false)
    private int gameId;
    
    @Column(nullable = false)
    private String transactionType;
    
    @Column(nullable = false)
    private String transactionDate;
    
    private String expiryDate;
    
    @Column(nullable = false)
    private double amount;
}