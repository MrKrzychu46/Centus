package com.example.centus;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private CollectionReference debtsCollection;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("users");
        debtsCollection = db.collection("debts");
    }

    // Dodanie użytkownika do Firestore
    public void addUser(String firstName, String lastName, String phone, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("first_name", firstName);
        user.put("last_name", lastName);
        user.put("phone", phone);
        user.put("email", email);

        usersCollection.add(user)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "User added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding user", e));
    }

    // Pobieranie użytkowników z Firestore
    public CollectionReference getUsers() {
        return usersCollection;
    }

    // Dodanie długu do Firestore
    public void addDebt(String name, double amount, String additionalInfo, String userId) {
        Map<String, Object> debt = new HashMap<>();
        debt.put("name", name);
        debt.put("amount", amount);
        debt.put("additional_info", additionalInfo);
        debt.put("user_id", userId);

        debtsCollection.add(debt)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Debt added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding debt", e));
    }

    // Pobieranie długów z Firestore
    public CollectionReference getDebts() {
        return debtsCollection;
    }

    // Aktualizacja długu w Firestore
    public void updateDebt(String debtId, String name, double amount, String additionalInfo, String userId) {
        Map<String, Object> updatedDebt = new HashMap<>();
        updatedDebt.put("name", name);
        updatedDebt.put("amount", amount);
        updatedDebt.put("additional_info", additionalInfo);
        updatedDebt.put("user_id", userId);

        debtsCollection.document(debtId)
                .update(updatedDebt)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Debt successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating debt", e));
    }

    // Usunięcie długu z Firestore
    public void deleteDebt(String debtId) {
        debtsCollection.document(debtId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Debt successfully deleted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting debt", e));
    }
}
