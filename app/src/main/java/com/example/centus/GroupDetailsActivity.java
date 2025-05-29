package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class GroupDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout expensesLayout;
    private String groupId;
    private String groupName;
    private String currentUserUid;
    private TextView title;
    private Button editGroupNameButton, manageMembersButton, addGroupExpenseButton, deleteGroupButton;
    private boolean isOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        expensesLayout = findViewById(R.id.expensesLayout);
        title = findViewById(R.id.groupTitleText);
        editGroupNameButton = findViewById(R.id.editGroupNameButton);
        manageMembersButton = findViewById(R.id.manageMembersButton);
        addGroupExpenseButton = findViewById(R.id.addGroupExpenseButton);
        deleteGroupButton = findViewById(R.id.deleteGroupButton);

        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        title.setText("Grupa: " + groupName);

        editGroupNameButton.setOnClickListener(v -> showEditGroupNameDialog());
        manageMembersButton.setOnClickListener(v -> openManageMembers());
        addGroupExpenseButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddGroupExpenseActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });

        deleteGroupButton.setOnClickListener(v -> confirmDeleteGroup());

        verifyOwnership();
    }

    private void verifyOwnership() {
        db.collection("groups").document(groupId).get().addOnSuccessListener(doc -> {
            String ownerId = doc.getString("owner_id");
            isOwner = currentUserUid.equals(ownerId);

            deleteGroupButton.setEnabled(isOwner);
            deleteGroupButton.setAlpha(isOwner ? 1.0f : 0.5f);

            manageMembersButton.setEnabled(isOwner);
            manageMembersButton.setAlpha(isOwner ? 1.0f : 0.5f);

            editGroupNameButton.setEnabled(isOwner);
            editGroupNameButton.setAlpha(isOwner ? 1.0f : 0.5f);

            loadGroupExpenses();
        });
    }

    private void confirmDeleteGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Usuń grupę")
                .setMessage("Czy na pewno chcesz usunąć tę grupę i wszystkie powiązane dane?")
                .setPositiveButton("Tak", (dialog, which) -> deleteGroupAndDependencies())
                .setNegativeButton("Nie", null)
                .show();
    }

    private void deleteGroupAndDependencies() {
        db.collection("group_expenses")
                .whereEqualTo("group_id", groupId)
                .get()
                .addOnSuccessListener(expenses -> {
                    List<String> expenseIds = new ArrayList<>();
                    for (QueryDocumentSnapshot expense : expenses) {
                        String expenseId = expense.getId();
                        expenseIds.add(expenseId);
                    }

                    for (String expenseId : expenseIds) {
                        deleteGroupExpenseWithDebts(expenseId);
                    }

                    db.collection("groups").document(groupId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Usunięto grupę", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("GroupDetailsActivity", "Błąd usuwania grupy", e);
                                Toast.makeText(this, "Błąd usuwania grupy", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void showEditGroupNameDialog() {
        if (!isOwner) return;
        EditText input = new EditText(this);
        input.setText(groupName);

        new AlertDialog.Builder(this)
                .setTitle("Edytuj nazwę grupy")
                .setView(input)
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        db.collection("groups").document(groupId)
                                .update("name", newName)
                                .addOnSuccessListener(aVoid -> {
                                    groupName = newName;
                                    title.setText("Wydatki grupy: " + groupName);
                                    Toast.makeText(this, "Zmieniono nazwę grupy", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Błąd edycji nazwy", Toast.LENGTH_SHORT).show();
                                    Log.e("GroupDetailsActivity", "Błąd edycji", e);
                                });
                    }
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void openManageMembers() {
        if (!isOwner) return;
        Intent intent = new Intent(this, ManageGroupMembersActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void loadGroupExpenses() {
        db.collection("group_expenses")
                .whereEqualTo("group_id", groupId)
                .orderBy("created_at")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("GroupDetailsActivity", "Błąd nasłuchu", error);
                        Toast.makeText(this, "Błąd nasłuchiwania zmian", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    expensesLayout.removeAllViews();

                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Brak wydatków w tej grupie", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String expenseId = doc.getId();
                        String title = doc.getString("title");
                        Double amount = doc.getDouble("amount");

                        Button expenseButton = new Button(this);
                        expenseButton.setText(title + " (" + amount + " zł)");
                        expenseButton.setBackgroundTintList(getColorStateList(R.color.button_background));
                        expenseButton.setTextColor(getColor(R.color.text_light));

                        if (isOwner) {
                            expenseButton.setOnClickListener(v -> showEditExpenseDialog(expenseId, title, amount));
                        }
                        expensesLayout.addView(expenseButton);
                    }
                });
    }

    private void showEditExpenseDialog(String expenseId, String title, Double amount) {
        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Nowy tytuł");
        inputTitle.setText(title);

        EditText inputAmount = new EditText(this);
        inputAmount.setHint("Nowa kwota");
        inputAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputAmount.setText(String.valueOf(amount));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(inputTitle);
        layout.addView(inputAmount);

        new AlertDialog.Builder(this)
                .setTitle("Edytuj lub usuń wydatek")
                .setView(layout)
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    String newTitle = inputTitle.getText().toString().trim();
                    String newAmountStr = inputAmount.getText().toString().trim();

                    if (!newTitle.isEmpty() && !newAmountStr.isEmpty()) {
                        double newAmount = Double.parseDouble(newAmountStr);

                        db.collection("group_expenses").document(expenseId)
                                .update("title", newTitle, "amount", newAmount)
                                .addOnSuccessListener(aVoid -> {
                                    // zaktualizuj również wszystkie długi powiązane z tym wydatkiem
                                    db.collection("debts")
                                            .whereEqualTo("group_expense_id", expenseId)
                                            .get()
                                            .addOnSuccessListener(debts -> {
                                                for (QueryDocumentSnapshot debtDoc : debts) {
                                                    Double currentAmount = debtDoc.getDouble("amount");
                                                    if (currentAmount != null) {
                                                        double updatedAmount = newAmount / debts.size();
                                                        db.collection("debts").document(debtDoc.getId())
                                                                .update("amount", updatedAmount);
                                                    }
                                                }
                                            });

                                    Toast.makeText(this, "Zaktualizowano wydatek i powiązane długi", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Błąd zapisu", Toast.LENGTH_SHORT).show();
                                    Log.e("GroupDetailsActivity", "Błąd edycji", e);
                                });
                    }
                })
                .setNegativeButton("Usuń", (dialog, which) -> deleteGroupExpenseWithDebts(expenseId))
                .setNeutralButton("Anuluj", null)
                .show();
    }

    private void deleteGroupExpenseWithDebts(String expenseId) {
        db.collection("debts")
                .whereEqualTo("group_expense_id", expenseId)
                .get()
                .addOnSuccessListener(debtsSnapshot -> {
                    List<String> debtIds = new ArrayList<>();
                    for (QueryDocumentSnapshot debtDoc : debtsSnapshot) {
                        debtIds.add(debtDoc.getId());
                    }

                    if (debtIds.isEmpty()) {
                        db.collection("group_expenses").document(expenseId)
                                .delete()
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Usunięto wydatek", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Błąd usuwania wydatku", Toast.LENGTH_SHORT).show();
                                    Log.e("GroupDetailsActivity", "Błąd usuwania wydatku", e);
                                });
                        return;
                    }

                    final int[] deletedCount = {0};
                    for (String debtId : debtIds) {
                        db.collection("debts").document(debtId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    deletedCount[0]++;
                                    if (deletedCount[0] == debtIds.size()) {
                                        db.collection("group_expenses").document(expenseId)
                                                .delete()
                                                .addOnSuccessListener(aVoid2 -> Toast.makeText(this, "Usunięto wydatek i powiązane długi", Toast.LENGTH_SHORT).show())
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Błąd usuwania wydatku", Toast.LENGTH_SHORT).show();
                                                    Log.e("GroupDetailsActivity", "Błąd usuwania wydatku", e);
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Błąd usuwania długu", Toast.LENGTH_SHORT).show();
                                    Log.e("GroupDetailsActivity", "Błąd usuwania długu", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd odczytu długów", Toast.LENGTH_SHORT).show();
                    Log.e("GroupDetailsActivity", "Błąd odczytu długów", e);
                });
    }
}
