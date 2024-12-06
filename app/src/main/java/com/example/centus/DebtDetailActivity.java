package com.example.centus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DebtDetailActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private String debtId; // Unikalne ID długu w Firestore

    // Deklaracja widoków jako pola klasy
    private TextView debtTitleView, debtAmountView, debtDescriptionView, debtUserView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debt_detail_activity);

        // Inicjalizacja FirebaseHelper
        firebaseHelper = new FirebaseHelper();

        // Inicjalizacja widoków
        debtTitleView = findViewById(R.id.debtTitle);
        debtAmountView = findViewById(R.id.debtAmount);
        debtDescriptionView = findViewById(R.id.debtDescription);
        debtUserView = findViewById(R.id.debtUser);

        // Pobranie szczegółów długu z Intenta
        debtId = getIntent().getStringExtra("debtId");
        loadDebtDetailsFromFirestore(debtId);

        // Obsługa przycisku edycji długu
        Button editDebtButton = findViewById(R.id.editDebtButton);
        editDebtButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, EditDebtActivity.class);
            intent.putExtra("debtId", debtId);
            startActivityForResult(intent, 1); // Oczekujemy wyniku z edycji
        });

        // Obsługa przycisku usuwania długu
        Button deleteDebtButton = findViewById(R.id.deleteDebtButton);
        deleteDebtButton.setOnClickListener(v -> {
            deleteDebt();
        });

        // Obsługa przycisków nawigacyjnych
        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK); // Ustawienie wyniku przed powrotem do MainActivity
            finish(); // Kończymy aktywność i wracamy do MainActivity
        });

        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, AddDebtActivity.class);
            startActivity(intent);
        });
    }

    private void loadDebtDetailsFromFirestore(String debtId) {
        firebaseHelper.getDebts().document(debtId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String debtName = documentSnapshot.getString("name");
                        double debtAmount = documentSnapshot.getDouble("amount");
                        String debtInfo = documentSnapshot.getString("additional_info");
                        String debtUser = documentSnapshot.getString("user_id");

                        // Ustawienie wartości tekstowych
                        debtAmountView.setText(String.valueOf(debtAmount));
                        debtTitleView.setText(debtName);
                        debtDescriptionView.setText(debtInfo);
                        debtUserView.setText(debtUser);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("DebtDetailActivity", "Error loading debt details", e);
                    Toast.makeText(this, "Błąd ładowania szczegółów długu", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteDebt() {
        firebaseHelper.getDebts().document(debtId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DebtDetailActivity.this, "Dług został usunięty", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK); // Informujemy, że dług został usunięty
                    finish(); // Kończymy aktywność i wracamy do MainActivity
                })
                .addOnFailureListener(e -> {
                    Log.w("DebtDetailActivity", "Error deleting debt", e);
                    Toast.makeText(DebtDetailActivity.this, "Błąd podczas usuwania długu", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            loadDebtDetailsFromFirestore(debtId); // Odświeżenie szczegółów długu po edycji
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDebtDetailsFromFirestore(debtId); // Odświeżenie szczegółów długu po powrocie do widoku
    }
}
