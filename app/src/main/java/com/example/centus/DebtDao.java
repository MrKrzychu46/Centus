package com.example.centus;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DebtDao {
    @Insert
    void insert(Debt debt);

    @Update
    void update(Debt debt);

    @Delete
    void delete(Debt debt);

    @Query("DELETE FROM debts WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM debts")
    List<Debt> getAllDebts();

    @Query("SELECT * FROM debts WHERE user = :userName")
    List<Debt> getDebtsByUser(String userName);

    @Query("SELECT * FROM debts WHERE id = :id")
    Debt getDebtById(int id); // Dodana metoda
}
