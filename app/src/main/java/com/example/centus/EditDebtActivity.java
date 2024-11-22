package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditDebtActivity extends AppCompatActivity {

    // Klasa Debt jako obiekt w tej aktywności (niezależny od AddDebtActivity)
    public static class Debt {
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

    private Debt currentDebt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_debt);

        // Pobierz dane długu z Intenta
        String debtName = getIntent().getStringExtra("debtName");
        double debtAmount = getIntent().getDoubleExtra("debtAmount", 0);
        String debtInfo = getIntent().getStringExtra("debtInfo");
        String debtUser = getIntent().getStringExtra("debtUser");

        if (debtName == null || debtUser == null) {
            Toast.makeText(this, "Nieprawidłowe dane długu!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentDebt = new Debt(debtName, debtAmount, debtInfo, debtUser);

        // Inicjalizacja pól edycji
        EditText nameEditText = findViewById(R.id.editDebtName);
        EditText amountEditText = findViewById(R.id.editDebtAmount);
        EditText infoEditText = findViewById(R.id.editDebtInfo);

        // Ustaw dane w polach tekstowych
        nameEditText.setText(debtName);
        amountEditText.setText(String.valueOf(debtAmount));
        infoEditText.setText(debtInfo);

        // Obsługa przycisku zapisu
        Button saveButton = findViewById(R.id.saveDebtButton);
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

                // Zaktualizuj dane długu
                currentDebt.name = newName;
                currentDebt.amount = newAmount;
                currentDebt.additionalInfo = newInfo;

                // Przygotuj Intent zaktualizowanych danych
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedDebtName", newName);
                resultIntent.putExtra("updatedDebtAmount", newAmount);
                resultIntent.putExtra("updatedDebtInfo", newInfo);
                resultIntent.putExtra("updatedDebtUser", currentDebt.user);

                // Zwróć dane do aktywności, która wywołała edycję
                setResult(RESULT_OK, resultIntent);
                finish(); // Zakończ aktywność edycji
            } catch (NumberFormatException e) {
                Toast.makeText(EditDebtActivity.this, "Nieprawidłowy format kwoty", Toast.LENGTH_SHORT).show();
            }
        });

        // Obsługa przycisku anulowania
        Button cancelButton = findViewById(R.id.cancelEditButton);
        cancelButton.setOnClickListener(v -> finish());
    }
}
