package com.example.centus;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public Task<QuerySnapshot> searchUserByEmail(String email) {
        return usersCollection.whereEqualTo("email", email).get();
    }
    // Dodanie użytkownika do Firestore
    public void addUser(String firstName, String lastName, String phone, String email, String uniqueId) {
        Map<String, Object> user = new HashMap<>();
        user.put("first_name", firstName);
        user.put("last_name", lastName);
        user.put("phone", phone);
        user.put("email", email);
        user.put("uniqueId", uniqueId);

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

    // Pobieranie i grupowanie długów według user_id
    public void fetchGroupedDebts(OnDebtsGroupedListener listener) {
        debtsCollection.get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, List<Map<String, Object>>> groupedDebts = new HashMap<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String userId = document.getString("user_id");
                        if (userId != null) {
                            groupedDebts.putIfAbsent(userId, new ArrayList<>());
                            Map<String, Object> debtData = document.getData();
                            debtData.put("debtId", document.getId()); // Dodajemy debtId
                            groupedDebts.get(userId).add(debtData);
                        }
                    }

                    Log.d(TAG, "Zgrupowane długi: " + groupedDebts);
                    listener.onSuccess(groupedDebts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd podczas pobierania długów", e);
                    listener.onFailure(e);
                });
    }

    // Pobieranie danych użytkownika na podstawie uniqueId
    public void fetchUserById(String uniqueId, OnUserFetchListener listener) {
        usersCollection.whereEqualTo("uniqueId", uniqueId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "Pobrano użytkownika o uniqueId: " + uniqueId + " -> " + querySnapshot.getDocuments().get(0).getData());
                        listener.onSuccess(querySnapshot.getDocuments().get(0).getData());
                    } else {
                        Log.e(TAG, "Brak użytkownika o uniqueId: " + uniqueId);
                        listener.onFailure(new Exception("User not found with uniqueId: " + uniqueId));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Błąd podczas pobierania użytkownika o uniqueId: " + uniqueId, e);
                    listener.onFailure(e);
                });
    }

    // Interfejsy do obsługi wyników
    public interface OnDebtsGroupedListener {
        void onSuccess(Map<String, List<Map<String, Object>>> groupedDebts);
        void onFailure(Exception e);
    }

    public interface OnUserFetchListener {
        void onSuccess(Map<String, Object> userData);
        void onFailure(Exception e);
    }
}
