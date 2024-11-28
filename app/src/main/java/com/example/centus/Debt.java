package com.example.centus;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debts")
public class Debt {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double amount;
    public String additionalInfo;
    public String user;

    public Debt(String name, double amount, String additionalInfo, String user) {
        this.name = name;
        this.amount = amount;
        this.additionalInfo = additionalInfo;
        this.user = user;
    }
}

