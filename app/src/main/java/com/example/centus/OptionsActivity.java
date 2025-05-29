package com.example.centus;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class OptionsActivity extends Activity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUid;
    private TextView greetingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) currentUid = user.getUid();

        greetingText = findViewById(R.id.greetingText);
        if (user != null) {
            db.collection("users").document(currentUid).get().addOnSuccessListener(doc -> {
                String name = doc.getString("name");
                if (name != null) greetingText.setText("Cześć, " + name + "!");
            });
        }

        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));

        Button resetPasswordButton = findViewById(R.id.resetPasswordButton);
        resetPasswordButton.setOnClickListener(v -> {
            if (user != null && user.getEmail() != null) {
                auth.sendPasswordResetEmail(user.getEmail())
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Link do resetu hasła został wysłany", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Błąd wysyłania e-maila", Toast.LENGTH_SHORT).show());
            }
        });

        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(v -> {
            if (user == null) return;

            db.collection("debts")
                    .whereEqualTo("debtor_id", currentUid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.isEmpty()) {
                            user.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Konto zostało usunięte", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Błąd usuwania konta", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Nie możesz usunąć konta z aktywnymi długami", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        Button logOutButton = findViewById(R.id.logoutButton);
        logOutButton.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
