package com.example.centus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

public class AddDebtActivity extends Activity {

    private FirebaseHelper firebaseHelper;
    private AutoCompleteTextView userPhoneInput;
    private HashMap<String, String> phoneToUidMap = new HashMap<>();
    private HashMap<String, String> phoneToEmailMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_debt);
        firebaseHelper = new FirebaseHelper();

        userPhoneInput = findViewById(R.id.userPhoneInput);

        // Wyłącz autouzupełnianie i sugestie
        userPhoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        userPhoneInput.setThreshold(Integer.MAX_VALUE); // brak sugestii

        loadUsersFromFirestore();

        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText amountEditText = findViewById(R.id.amountEditText);
        EditText infoEditText = findViewById(R.id.infoEditText);
        Button addDebtButton = findViewById(R.id.addDebtButton);

        addDebtButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String amountText = amountEditText.getText().toString().trim();
            String additionalInfo = infoEditText.getText().toString().trim();
            String phone = userPhoneInput.getText().toString().trim();
            String creditorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (name.isEmpty() || amountText.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            String debtorId = phoneToUidMap.get(phone);
            String recipientEmail = phoneToEmailMap.get(phone);

            if (debtorId == null || recipientEmail == null) {
                Toast.makeText(this, "Nie znaleziono użytkownika z tym numerem", Toast.LENGTH_SHORT).show();
                return;
            }

            if (debtorId.equals(creditorId)) {
                Toast.makeText(this, "Nie możesz dodać siebie jako dłużnika", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String sanitized = amountText.replaceAll("[^0-9.,]", "").replace(",", ".");
                double amount = Double.parseDouble(sanitized);
                if (amount <= 0 || amount > 1_000_000) {
                    Toast.makeText(this, "Kwota musi być dodatnia i nie większa niż 1,000,000", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseHelper.addDebt(name, amount, additionalInfo, creditorId, debtorId);

                new Thread(() -> {
                    try {
                        MailSender mailSender = new MailSender("centuscentaury@gmail.com", "jopl oohx sydl fpmk");
                        String subject = "Nowy dług w aplikacji Centus";
                        String message = "Zostałeś dodany jako dłużnik.\n\n" +
                                "Nazwa: " + name + "\nKwota: " + amount + " zł\n" +
                                (additionalInfo.isEmpty() ? "" : "Info: " + additionalInfo + "\n") +
                                "Skontaktuj się z wierzycielem.";

                        mailSender.sendEmail(recipientEmail, subject, message);
                        runOnUiThread(() -> Toast.makeText(this, "E-mail został wysłany.", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Błąd przy wysyłaniu e-maila", Toast.LENGTH_SHORT).show());
                    }
                }).start();

                new AlertDialog.Builder(this)
                        .setTitle("Potwierdzenie")
                        .setMessage("Dług został dodany. Wrócić do ekranu głównego?")
                        .setPositiveButton("Tak", (dialog, which) -> {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        })
                        .setNegativeButton("Nie", (dialog, which) -> {
                            nameEditText.setText("");
                            amountEditText.setText("");
                            infoEditText.setText("");
                            userPhoneInput.setText("");
                        })
                        .show();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nieprawidłowa kwota", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsersFromFirestore() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String uid = doc.getId();
                        String phone = doc.getString("phone");
                        String email = doc.getString("email");

                        if (uid.equals(currentUid)) continue;
                        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(email)) continue;

                        phoneToUidMap.put(phone, uid);
                        phoneToEmailMap.put(phone, email);
                    }
                });
    }

    public static class Debt {
        String id, name, additionalInfo, user;
        double amount;

        public Debt(String name, double amount, String additionalInfo, String user) {
            this.name = name;
            this.amount = amount;
            this.additionalInfo = additionalInfo;
            this.user = user;
        }

        public void setId(String id) { this.id = id; }
        public String getId() { return id; }
    }
}
