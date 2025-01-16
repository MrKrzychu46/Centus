package com.example.centus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ArrayList<AddDebtActivity.Debt> debtList = new ArrayList<>();
    private LinearLayout debtsLayout;
    private TextView totalDebtTextView;
    private View statusIndicator;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusIndicator = findViewById(R.id.statusIndicator);
        firebaseHelper = new FirebaseHelper();

        debtsLayout = findViewById(R.id.debtsLayout);
        totalDebtTextView = findViewById(R.id.totalDebtTextView);

        // Wyszukiwanie użytkowników za pomocą e-maila
        Button searchUserButton = findViewById(R.id.searchUserButton);
        EditText emailSearchEditText = findViewById(R.id.emailSearchEditText);

        searchUserButton.setOnClickListener(v -> {
            String email = emailSearchEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Proszę wpisać e-mail", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseHelper.searchUserByEmail(email).addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Nie znaleziono użytkownika z podanym e-mailem", Toast.LENGTH_SHORT).show();
                } else {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = document.getData();
                        Log.d("MainActivity", "Data map: " + data); // Sprawdź, co zawiera mapa danych

                        String firstName = (String) data.get("name");
                        String lastName = (String) data.get("surname");
                        String phone = (String) data.get("phone");

                        Toast.makeText(MainActivity.this,
                                "Znaleziono użytkownika:\n" +
                                        "Imię: " + firstName + "\n" +
                                        "Nazwisko: " + lastName + "\n" +
                                        "Telefon: " + phone,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Wystąpił błąd podczas wyszukiwania użytkownika", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Error searching user by email", e);
            });
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            loadDebtsFromFirestore();
        }
    }

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
                    startActivityForResult(intent, 1);
                });

                debtsLayout.addView(debtButton);
            }
        }
    }

    private void updateTotalDebt() {
        double totalDebt = calculateTotalDebt();
        totalDebtTextView.setText("Bilans długu: " + totalDebt + " zł");

        if (totalDebt > 0) {
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            statusIndicator.setVisibility(View.VISIBLE);
        } else if (totalDebt < 0) {
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            statusIndicator.setVisibility(View.VISIBLE);
        } else {
            statusIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private double calculateTotalDebt() {
        double total = 0.0;
        for (AddDebtActivity.Debt debt : debtList) {
            total += debt.amount;
        }
        return total;
    }

    private void loadDebtsFromFirestore() {
        debtList.clear();
        debtsLayout.removeAllViews();
        firebaseHelper.getDebts().get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String name = document.getString("name");
                        Double amountValue = document.getDouble("amount");
                        double amount = (amountValue != null) ? amountValue : 0.0;

                        String additionalInfo = document.getString("additional_info");
                        String user = document.getString("user_id");

                        AddDebtActivity.Debt debt = new AddDebtActivity.Debt(name, amount, additionalInfo, user);
                        debt.setId(id);
                        if (!containsDebt(debt)) {
                            debtList.add(debt);
                        }
                    }
                    displayDebts();
                    updateTotalDebt();
                })
                .addOnFailureListener(e -> {
                    Log.w("MainActivity", "Error loading debts from Firestore", e);
                    Toast.makeText(MainActivity.this, "Błąd ładowania długów", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDebtsFromFirestore();
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