package com.example.centus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DebtorDetailsActivity extends AppCompatActivity {

    private LinearLayout debtsLayout;
    private TextView debtorNameText;
    private TextView balanceText;
    private FirebaseFirestore db;
    private String debtorId;
    private String currentUserUid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debtor_details);

        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        debtorId = getIntent().getStringExtra("debtorId");
        String debtorName = getIntent().getStringExtra("debtorName");

        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        debtorNameText = findViewById(R.id.debtorTitle);
        balanceText = findViewById(R.id.balanceText);
        debtsLayout = findViewById(R.id.debtorDebtsLayout);

        debtorNameText.setText("Długi użytkownika: " + debtorName);

        listenToDebtChanges();
        listenToReverseDebtChanges();
    }

    private double totalCreditorDebts = 0;
    private double totalDebtorDebts = 0;

    private void listenToDebtChanges() {
        db.collection("debts")
                .whereEqualTo("creditor_id", currentUserUid)
                .whereEqualTo("debtor_id", debtorId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) return;
                    List<AddDebtActivity.Debt> debts = new ArrayList<>();
                    totalCreditorDebts = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        Double amountValue = doc.getDouble("amount");
                        double amount = (amountValue != null) ? amountValue : 0.0;
                        String info = doc.getString("additional_info");
                        AddDebtActivity.Debt debt = new AddDebtActivity.Debt(name, amount, info, debtorId);
                        debt.setId(id);
                        debts.add(debt);
                        totalCreditorDebts += amount;
                    }
                    displayDebts(debts);
                    updateBalance();
                });
    }

    private void listenToReverseDebtChanges() {
        db.collection("debts")
                .whereEqualTo("creditor_id", debtorId)
                .whereEqualTo("debtor_id", currentUserUid)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) return;
                    totalDebtorDebts = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Double amountValue = doc.getDouble("amount");
                        double amount = (amountValue != null) ? amountValue : 0.0;
                        totalDebtorDebts += amount;
                    }
                    updateBalance();
                });
    }

    private void updateBalance() {
        double balance = totalCreditorDebts - totalDebtorDebts;
        if (balance > 0) {
            balanceText.setText("Użytkownik jest Ci winien " + String.format("%.2f", balance) + " zł");
        } else if (balance < 0) {
            balanceText.setText("Jesteś winien użytkownikowi " + String.format("%.2f", -balance) + " zł");
        } else {
            balanceText.setText("Brak wzajemnych zobowiązań");
        }
    }


    private void displayDebts(List<AddDebtActivity.Debt> debts) {
        debtsLayout.removeAllViews();
        for (AddDebtActivity.Debt debt : debts) {
            Button button = new Button(this);
            button.setText(debt.name + " – " + debt.amount + " zł");
            button.setOnClickListener(v -> {
                Intent intent = new Intent(this, DebtDetailActivity.class);
                intent.putExtra("debtId", debt.getId());
                startActivity(intent);
            });
            debtsLayout.addView(button);
        }
    }
}
