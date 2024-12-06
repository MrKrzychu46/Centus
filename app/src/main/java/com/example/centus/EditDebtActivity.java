package com.example.centus;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditDebtActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private String debtId; // Unikalne ID długu w Firestore

    private EditText nameEditText;
    private EditText amountEditText;
    private EditText infoEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_debt);

        // Inicjalizacja FirebaseHelper
        firebaseHelper = new FirebaseHelper();

        // Pobierz ID długu z Intentu (będzie potrzebne do edycji w Firestore)
        debtId = getIntent().getStringExtra("debtId");

        // Inicjalizacja elementów UI
        nameEditText = findViewById(R.id.editDebtName);
        amountEditText = findViewById(R.id.editDebtAmount);
        infoEditText = findViewById(R.id.editDebtInfo);
        saveButton = findViewById(R.id.saveDebtButton);

        // Załaduj szczegóły długu z Firestore
        loadDebtDetailsFromFirestore(debtId);

        // Obsługa przycisku zapisywania zmian
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newAmountText = amountEditText.getText().toString().trim();
            String newInfo = infoEditText.getText().toString().trim();

            // Weryfikacja pól
            if (newName.isEmpty() || newAmountText.isEmpty()) {
                Toast.makeText(EditDebtActivity.this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newAmount = Double.parseDouble(newAmountText);

                if (newAmount <= 0 || newAmount > 1_000_000) {
                    Toast.makeText(EditDebtActivity.this, "Kwota musi być dodatnia i nie większa niż 1,000,000", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Aktualizacja długu w Firestore
                firebaseHelper.updateDebt(debtId, newName, newAmount, newInfo, "currentUserPlaceholder");
                Toast.makeText(EditDebtActivity.this, "Dług został zaktualizowany", Toast.LENGTH_SHORT).show();
                finish(); // Zakończ aktywność po zaktualizowaniu długu

            } catch (NumberFormatException e) {
                Toast.makeText(EditDebtActivity.this, "Nieprawidłowy format kwoty", Toast.LENGTH_SHORT).show();
            }
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

                        // Inicjalizacja pól edycji z wartościami pobranymi z Firestore
                        nameEditText.setText(debtName);
                        amountEditText.setText(String.valueOf(debtAmount));
                        infoEditText.setText(debtInfo);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("EditDebtActivity", "Error loading debt details", e);
                    Toast.makeText(this, "Błąd ładowania szczegółów długu", Toast.LENGTH_SHORT).show();
                });
    }
}
