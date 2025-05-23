package com.example.centus;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

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

    // Pobieranie użytkownika po UID (z dokumentu)
    public Task<DocumentSnapshot> getUserByUid(String uid) {
        return usersCollection.document(uid).get();
    }

    // Dodanie użytkownika do Firestore z UID jako ID dokumentu
    public void addUser(String uid, String firstName, String lastName, String phone, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", firstName);
        user.put("surname", lastName);
        user.put("phone", phone);
        user.put("email", email);

        usersCollection.document(uid).set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User saved with UID: " + uid))
                .addOnFailureListener(e -> Log.w(TAG, "Error saving user", e));
    }

    // Pobieranie użytkowników
    public CollectionReference getUsers() {
        return usersCollection;
    }

    // Dodanie długu

    public void addDebt(String name, double amount, String additionalInfo, String creditorId, String debtorId) {
        Map<String, Object> debt = new HashMap<>();
        debt.put("name", name);
        debt.put("amount", amount);
        debt.put("additional_info", additionalInfo);
        debt.put("creditor_id", creditorId);
        debt.put("debtor_id", debtorId);
        debt.put("created_at", Timestamp.now()); // ⏰ dodanie daty

        debtsCollection.add(debt)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Debt added with ID: " + docRef.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding debt", e));
    }



    // Pobieranie długów
    public CollectionReference getDebts() {
        return debtsCollection;
    }

    // Aktualizacja długu
    public void updateDebt(String debtId, String name, double amount, String additionalInfo, String userId) {
        Map<String, Object> updatedDebt = new HashMap<>();
        updatedDebt.put("name", name);
        updatedDebt.put("amount", amount);
        updatedDebt.put("additional_info", additionalInfo);
        updatedDebt.put("user_id", userId);

        debtsCollection.document(debtId).update(updatedDebt)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Debt updated"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating debt", e));
    }

    // Usunięcie długu
    public void deleteDebt(String debtId) {
        debtsCollection.document(debtId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Debt deleted"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting debt", e));
    }

    // Grupowanie długów wg UID
    public void fetchGroupedDebts(OnDebtsGroupedListener listener) {
        debtsCollection.get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, List<Map<String, Object>>> groupedDebts = new HashMap<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getString("user_id");
                        if (userId != null) {
                            groupedDebts.putIfAbsent(userId, new ArrayList<>());
                            Map<String, Object> debtData = doc.getData();
                            debtData.put("debtId", doc.getId());
                            groupedDebts.get(userId).add(debtData);
                        }
                    }
                    listener.onSuccess(groupedDebts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching grouped debts", e);
                    listener.onFailure(e);
                });
    }

    public void fetchUserById(String uid, OnUserFetchListener listener) {
        usersCollection.document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onSuccess(documentSnapshot.getData());
                    } else {
                        listener.onFailure(new Exception("Nie znaleziono użytkownika o UID: " + uid));
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }


    // Interfejsy
    public interface OnDebtsGroupedListener {
        void onSuccess(Map<String, List<Map<String, Object>>> groupedDebts);
        void onFailure(Exception e);
    }
    public interface OnUserFetchListener {
        void onSuccess(Map<String, Object> userData);
        void onFailure(Exception e);
    }

}
