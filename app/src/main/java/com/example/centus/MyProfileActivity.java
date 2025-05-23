package com.example.centus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MyProfileActivity extends Activity {

    private LinearLayout debtsLayout;
    private FirebaseHelper firebaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);

        firebaseHelper = new FirebaseHelper();
        debtsLayout = findViewById(R.id.debtsLayout);

        // Nawigacja
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        listenToDebtChanges(); // 🔁 nasłuchujemy tylko swoje długi
    }

    private void listenToDebtChanges() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String currentUid = currentUser.getUid();

        firebaseHelper.getDebts().addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.w("MyProfileActivity", "Błąd snapshot listenera", e);
                Toast.makeText(this, "Błąd pobierania danych", Toast.LENGTH_SHORT).show();
                return;
            }

            if (querySnapshot == null) return;

            ArrayList<AddDebtActivity.Debt> debtsAsDebtor = new ArrayList<>();
            debtsLayout.removeAllViews();

            for (QueryDocumentSnapshot doc : querySnapshot) {
                String id = doc.getId();
                String name = doc.getString("name");
                Double amountValue = doc.getDouble("amount");
                double amount = (amountValue != null) ? amountValue : 0.0;
                String info = doc.getString("additional_info");
                String debtorId = doc.getString("debtor_id");

                if (debtorId != null && debtorId.equals(currentUid)) {
                    AddDebtActivity.Debt debt = new AddDebtActivity.Debt(name, amount, info, debtorId);
                    debt.setId(id);
                    debtsAsDebtor.add(debt);
                }
            }

            if (!debtsAsDebtor.isEmpty()) {
                for (AddDebtActivity.Debt debt : debtsAsDebtor) addDebtButtonToLayout(debt);
            } else {
                TextView empty = new TextView(this);
                empty.setText("Nie masz żadnych aktywnych długów.");
                debtsLayout.addView(empty);
            }
        });
    }

    private void addDebtButtonToLayout(AddDebtActivity.Debt debt) {
        Button button = new Button(this);
        button.setText("Dług: " + debt.name + " - " + debt.amount + " zł");
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, DebtDetailActivity.class);
            intent.putExtra("debtId", debt.getId());
            startActivity(intent);
        });
        debtsLayout.addView(button);
    }
}
