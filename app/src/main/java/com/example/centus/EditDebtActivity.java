package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class EditDebtActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private String debtId;

    private EditText nameEditText;
    private EditText amountEditText;
    private EditText infoEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_debt);

        firebaseHelper = new FirebaseHelper();
        debtId = getIntent().getStringExtra("debtId");

        nameEditText = findViewById(R.id.editDebtName);
        amountEditText = findViewById(R.id.editDebtAmount);
        infoEditText = findViewById(R.id.editDebtInfo);
        saveButton = findViewById(R.id.saveDebtButton);

        // Nawigacja
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        loadDebtDetailsFromFirestore(debtId);

        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newAmountText = amountEditText.getText().toString().trim();
            String newInfo = infoEditText.getText().toString().trim();

            if (newName.isEmpty() || newAmountText.isEmpty()) {
                Toast.makeText(this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newAmount = Double.parseDouble(newAmountText);
                if (newAmount <= 0 || newAmount > 1_000_000) {
                    Toast.makeText(this, "Kwota musi być dodatnia i nie większa niż 1,000,000", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // ✅ UID aktualnego użytkownika
                firebaseHelper.updateDebt(debtId, newName, newAmount, newInfo, currentUid);
                Toast.makeText(this, "Dług został zaktualizowany", Toast.LENGTH_SHORT).show();
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nieprawidłowy format kwoty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDebtDetailsFromFirestore(String debtId) {
        firebaseHelper.getDebts().document(debtId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String debtName = documentSnapshot.getString("name");
                        Double debtAmount = documentSnapshot.getDouble("amount");
                        String debtInfo = documentSnapshot.getString("additional_info");

                        nameEditText.setText(debtName != null ? debtName : "");
                        amountEditText.setText(debtAmount != null ? String.valueOf(debtAmount) : "");
                        infoEditText.setText(debtInfo != null ? debtInfo : "");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("EditDebtActivity", "Error loading debt details", e);
                    Toast.makeText(this, "Błąd ładowania szczegółów długu", Toast.LENGTH_SHORT).show();
                });
    }
}
