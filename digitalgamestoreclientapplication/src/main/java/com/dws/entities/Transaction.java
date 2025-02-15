package com.dws.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private int transactionId;
    private int userId;
    private int gameId;
    private String transactionType;
    private String transactionDate;
    private String expiryDate;
    private double amount;
}