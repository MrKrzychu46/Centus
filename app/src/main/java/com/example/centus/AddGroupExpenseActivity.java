package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGroupExpenseActivity extends AppCompatActivity {

    private EditText titleInput, amountInput;
    private TextView groupNameText;
    private Button saveButton;
    private FirebaseFirestore db;
    private String currentUid, groupId, groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_expense);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));


        titleInput = findViewById(R.id.expenseTitleEditText);
        amountInput = findViewById(R.id.expenseAmountEditText);
        groupNameText = findViewById(R.id.expenseGroupNameText);
        saveButton = findViewById(R.id.saveExpenseButton);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        groupNameText.setText("Grupa: " + groupName);

        saveButton.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String title = titleInput.getText().toString().trim();
        String amountText = amountInput.getText().toString().trim();

        if (title.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nieprawidłowa kwota", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> expense = new HashMap<>();
        expense.put("title", title);
        expense.put("amount", amount);
        expense.put("group_id", groupId);
        expense.put("payer_id", currentUid);
        expense.put("created_at", Timestamp.now());

        db.collection("group_expenses").add(expense).addOnSuccessListener(docRef -> {
            db.collection("groups").document(groupId).get().addOnSuccessListener(groupDoc -> {
                List<String> members = (List<String>) groupDoc.get("members");
                if (members == null || members.isEmpty()) {
                    Toast.makeText(this, "Grupa nie ma członków", Toast.LENGTH_SHORT).show();
                    return;
                }

                BigDecimal share = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

                for (String memberUid : members) {
                    if (memberUid.equals(currentUid)) continue;

                    Map<String, Object> debt = new HashMap<>();
                    debt.put("name", title + " (" + groupName + ")");
                    debt.put("amount", share.doubleValue());
                    debt.put("additional_info", "Wyd. grupowy");
                    debt.put("creditor_id", currentUid);
                    debt.put("debtor_id", memberUid);
                    debt.put("created_at", Timestamp.now());
                    debt.put("group_expense_id", docRef.getId()); // 👈 DODAJ TO!

                    db.collection("debts").add(debt);
                }

                Toast.makeText(this, "Wydatek dodany", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Błąd dodawania wydatku", Toast.LENGTH_SHORT).show();
            Log.e("AddGroupExpense", "Błąd zapisu", e);
        });
    }

}
