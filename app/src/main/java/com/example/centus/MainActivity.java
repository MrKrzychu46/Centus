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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUid = currentUser.getUid();

        firebaseHelper.getDebts().get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<AddDebtActivity.Debt> debtsAsCreditor = new ArrayList<>();
                    ArrayList<AddDebtActivity.Debt> debtsAsDebtor = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        Double amountValue = doc.getDouble("amount");
                        double amount = (amountValue != null) ? amountValue : 0.0;
                        String info = doc.getString("additional_info");
                        String creditorId = doc.getString("creditor_id");
                        String debtorId = doc.getString("debtor_id");

                        AddDebtActivity.Debt debt = new AddDebtActivity.Debt(name, amount, info, debtorId);
                        debt.setId(id);

                        if (creditorId != null && creditorId.equals(currentUid)) {
                            debtsAsCreditor.add(debt);
                        } else if (debtorId != null && debtorId.equals(currentUid)) {
                            debtsAsDebtor.add(debt);
                        }
                    }

                    // Wyświetl długi jako dłużnik
                    if (!debtsAsDebtor.isEmpty()) {
                        TextView label = new TextView(this);
                        label.setText("🟥 Twoje długi (jesteś dłużnikiem):");
                        debtsLayout.addView(label);
                        for (AddDebtActivity.Debt debt : debtsAsDebtor) {
                            addDebtButtonToLayout(debt);
                        }
                    }

                    // Wyświetl długi jako wierzyciel
                    if (!debtsAsCreditor.isEmpty()) {
                        TextView label = new TextView(this);
                        label.setText("🟩 Twoi dłużnicy (Ty pożyczyłeś):");
                        debtsLayout.addView(label);
                        for (AddDebtActivity.Debt debt : debtsAsCreditor) {
                            addDebtButtonToLayout(debt);
                        }
                    }

                    updateTotalDebt(debtsAsDebtor, debtsAsCreditor);
                })
                .addOnFailureListener(e -> {
                    Log.w("MainActivity", "Błąd ładowania długów", e);
                    Toast.makeText(this, "Błąd ładowania długów", Toast.LENGTH_SHORT).show();
                });
    }

    private void addDebtButtonToLayout(AddDebtActivity.Debt debt) {
        Button button = new Button(this);
        button.setText("Dług: " + debt.name + " - " + debt.amount + " zł");
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DebtDetailActivity.class);
            intent.putExtra("debtId", debt.getId());
            startActivityForResult(intent, 1);
        });
        debtsLayout.addView(button);
    }

    private void updateTotalDebt(ArrayList<AddDebtActivity.Debt> debtorList, ArrayList<AddDebtActivity.Debt> creditorList) {
        double total = 0.0;
        for (AddDebtActivity.Debt d : creditorList) total += d.amount;
        for (AddDebtActivity.Debt d : debtorList) total -= d.amount;

        totalDebtTextView.setText("Bilans: " + total + " zł");
        if (total > 0) {
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            statusIndicator.setVisibility(View.VISIBLE);
        } else if (total < 0) {
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            statusIndicator.setVisibility(View.VISIBLE);
        } else {
            statusIndicator.setVisibility(View.INVISIBLE);
        }
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