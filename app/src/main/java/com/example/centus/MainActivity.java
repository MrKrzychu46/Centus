package com.example.centus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<AddDebtActivity.Debt> debtList = new ArrayList<>();
    private LinearLayout debtsLayout;
    private TextView totalDebtTextView;
    private View statusIndicator;
    private FirebaseHelper firebaseHelper; // Added: FirebaseHelper instance for Firestore operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusIndicator = findViewById(R.id.statusIndicator);

        firebaseHelper = new FirebaseHelper();  // Added: Initialize FirebaseHelper

        debtsLayout = findViewById(R.id.debtsLayout);
        totalDebtTextView = findViewById(R.id.totalDebtTextView);

        // Przycisk do nawigacji
        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddDebtActivity.class);
            startActivityForResult(intent, 1);
        });

        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyProfileActivity.class);
            startActivity(intent);
        });

        Button groupsButton = findViewById(R.id.groupsButton);
        groupsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyGroupsActivity.class);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
            startActivity(intent);
        });
    }

    // Obsługa powrotu z AddDebtActivity i DebtDetailActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Załaduj ponownie długi, aby uwzględnić wszelkie zmiany (dodanie, edycja lub usunięcie)
            loadDebtsFromFirestore(); // Existing call
        }
    }

    // Wyświetlanie długów w widoku
    private void displayDebts() {
        debtsLayout.removeAllViews();
        Log.d("MainActivity", "Rozpoczynam wyświetlanie długów. Liczba długów: " + debtList.size());

        if (debtList.isEmpty()) {
            Toast.makeText(MainActivity.this, "Brak długów", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Lista długów jest pusta");
        } else {
            for (int i = 0; i < debtList.size(); i++) {
                AddDebtActivity.Debt debt = debtList.get(i);
                Log.d("MainActivity", "Wyświetlam dług: " + debt.name + " - " + debt.amount);

                Button debtButton = new Button(this);
                debtButton.setText("Dług " + (i + 1) + ": " + debt.name);

                final int index = i;
                debtButton.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, DebtDetailActivity.class);
                    intent.putExtra("debtId", debtList.get(index).getId());
                    startActivityForResult(intent, 1); // Oczekujemy wyniku z DebtDetailActivity
                });

                debtsLayout.addView(debtButton);
            }
        }
    }

    // Aktualizacja sumy długów
    private void updateTotalDebt() {
        double totalDebt = calculateTotalDebt();
        totalDebtTextView.setText("Bilans długu: " + totalDebt + " zł");

        if (totalDebt > 0) {
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            statusIndicator.setVisibility(View.VISIBLE); // Ustaw widoczność na widoczną
        } else if (totalDebt < 0) {
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            statusIndicator.setVisibility(View.VISIBLE); // Ustaw widoczność na widoczną
        } else {
            statusIndicator.setVisibility(View.INVISIBLE); // Ukryj kwadracik
        }
    }

    // Obliczanie sumy długów
    private double calculateTotalDebt() {
        double total = 0.0;
        for (AddDebtActivity.Debt debt : debtList) {
            total += debt.amount;
        }
        return total;
    }

    // Wczytywanie długów z Firestore
    private void loadDebtsFromFirestore() {
        debtList.clear();
        debtsLayout.removeAllViews(); // Czyszczenie widoku, aby uniknąć duplikatów
        firebaseHelper.getDebts().get() // Added: Using FirebaseHelper for Firestore access
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String name = document.getString("name");
                        Double amount = document.getDouble("amount"); // Modified: Check for null-safe retrieval
                        if (amount == null) amount = 0.0; // Added: Assign default value for null amounts
                        String additionalInfo = document.getString("additional_info");
                        String user = document.getString("user_id");

                        AddDebtActivity.Debt debt = new AddDebtActivity.Debt(name, amount, additionalInfo, user);
                        debt.setId(id);  // Przypisanie ID do obiektu `Debt`
                        if (!containsDebt(debt)) {
                            debtList.add(debt);
                        }
                    }
                    displayDebts(); // Existing: Display debts after loading
                    updateTotalDebt(); // Existing: Update total debt balance
                })
                .addOnFailureListener(e -> {
                    Log.w("MainActivity", "Error loading debts from Firestore", e);
                    Toast.makeText(MainActivity.this, "Błąd ładowania długów", Toast.LENGTH_SHORT).show();
                });
    }

    // Metoda wywoływana po powrocie do MainActivity
    @Override
    protected void onResume() {
        super.onResume();
        // Załaduj ponownie długi po powrocie do MainActivity, aby odświeżyć dane
        loadDebtsFromFirestore(); // Existing call
    }

    private boolean containsDebt(AddDebtActivity.Debt newDebt) {
        for (AddDebtActivity.Debt debt : debtList) {
            if (debt.getId().equals(newDebt.getId())) {
                return true;
            }
        }
        return false;
    }
}
