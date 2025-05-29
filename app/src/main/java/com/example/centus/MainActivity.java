package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

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
    protected void onResume() {
        super.onResume();
        listenToDebtChanges();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusIndicator = findViewById(R.id.statusIndicator);
        firebaseHelper = new FirebaseHelper();

        debtsLayout = findViewById(R.id.debtsLayout);
        totalDebtTextView = findViewById(R.id.totalDebtTextView);

        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));
    }

    private void listenToDebtChanges() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String currentUid = currentUser.getUid();

        firebaseHelper.getDebts().addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.w("MainActivity", "Błąd snapshot listenera", e);
                Toast.makeText(this, "Błąd pobierania danych", Toast.LENGTH_SHORT).show();
                return;
            }

            if (querySnapshot == null) return;

            debtsLayout.removeAllViews();

            Map<String, List<AddDebtActivity.Debt>> groupedDebts = new HashMap<>();
            List<AddDebtActivity.Debt> debtsAsDebtor = new ArrayList<>();

            double totalCredit = 0;
            double totalDebt = 0;

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
                    groupedDebts.putIfAbsent(debtorId, new ArrayList<>());
                    groupedDebts.get(debtorId).add(debt);
                    totalCredit += amount;
                } else if (debtorId != null && debtorId.equals(currentUid)) {
                    debtsAsDebtor.add(debt);
                    totalDebt += amount;
                }
            }

            if (!debtsAsDebtor.isEmpty()) {
                TextView label = new TextView(this);
                label.setText("🟥 Twoje długi (jesteś dłużnikiem):");
                debtsLayout.addView(label);
                for (AddDebtActivity.Debt debt : debtsAsDebtor) addDebtButtonToLayout(debt);
            }

            if (!groupedDebts.isEmpty()) {
                TextView label = new TextView(this);
                label.setText("🟩 Twoi dłużnicy (Ty pożyczyłeś):");
                debtsLayout.addView(label);

                for (String debtorId : groupedDebts.keySet()) {
                    List<AddDebtActivity.Debt> debts = groupedDebts.get(debtorId);
                    double total = 0.0;
                    for (AddDebtActivity.Debt d : debts) total += d.amount;

                    double finalTotal = total;
                    firebaseHelper.fetchUserById(debtorId, new FirebaseHelper.OnUserFetchListener() {
                        @Override
                        public void onSuccess(Map<String, Object> userData) {
                            String name = (String) userData.get("name");
                            String surname = (String) userData.get("surname");
                            String fullName = (name != null ? name : "") + " " + (surname != null ? surname : "");

                            Button debtorButton = new Button(MainActivity.this);
                            debtorButton.setText(fullName + " – " + debts.size() + " dług(i) – razem: " + String.format("%.2f", finalTotal) + " zł");
                            debtorButton.setOnClickListener(v -> {
                                Intent intent = new Intent(MainActivity.this, DebtorDetailsActivity.class);
                                intent.putExtra("debtorId", debtorId);
                                intent.putExtra("debtorName", fullName);
                                startActivity(intent);
                            });
                            debtsLayout.addView(debtorButton);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("MainActivity", "Błąd przy pobieraniu użytkownika", e);
                        }
                    });
                }
            }

            updateTotalDebt(totalDebt, totalCredit);
        });
    }

    private void addDebtButtonToLayout(AddDebtActivity.Debt debt) {
        Button button = new Button(this);
        button.setText("Dług: " + debt.name + " - " + String.format("%.2f", debt.amount) + " zł");
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, DebtDetailActivity.class);
            intent.putExtra("debtId", debt.getId());
            startActivity(intent);
        });
        debtsLayout.addView(button);
    }

    private void updateTotalDebt(double totalDebt, double totalCredit) {
        double total = totalCredit - totalDebt;
        totalDebtTextView.setText("Bilans: " + String.format("%.2f", total) + " zł");
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
}
