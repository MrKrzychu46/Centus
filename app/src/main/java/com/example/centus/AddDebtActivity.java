package com.example.centus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

public class AddDebtActivity extends Activity {

    private ArrayList<String> userList = new ArrayList<>();
    private HashMap<String, String> userEmailMap = new HashMap<>(); // Mapowanie użytkowników na e-maile
    private ArrayList<Debt> debtList = new ArrayList<>();
    private Spinner userSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_debt);

        // Przyciski nawigacji
        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddDebtActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddDebtActivity.this, MainActivity.class);
            startActivity(intent);
        });

        Button addUsersButton = findViewById(R.id.addingUsersButton);
        addUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddDebtActivity.this, AddUserActivity.class);
            startActivity(intent);
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddDebtActivity.this, MyProfileActivity.class);
            startActivity(intent);
        });

        Button groupsButton = findViewById(R.id.groupsButton);
        groupsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddDebtActivity.this, MyGroupsActivity.class);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddDebtActivity.this, OptionsActivity.class);
            startActivity(intent);
        });

        // Ładowanie użytkowników z pliku
        loadUsersFromFile();

        // Inicjalizacja spinnera
        userSpinner = findViewById(R.id.userSpinner);
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(userAdapter);

        // Inicjalizacja pól i przycisków
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText amountEditText = findViewById(R.id.amountEditText);
        EditText infoEditText = findViewById(R.id.infoEditText);
        Button addDebtButton = findViewById(R.id.addDebtButton);

        // Obsługa przycisku "Dodaj dług"
        addDebtButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String amountText = amountEditText.getText().toString().trim();
            String additionalInfo = infoEditText.getText().toString().trim();
            String selectedUser = (String) userSpinner.getSelectedItem();

            if (name.isEmpty() || amountText.isEmpty() || selectedUser == null || selectedUser.isEmpty()) {
                Toast.makeText(AddDebtActivity.this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedEmail = userEmailMap.get(selectedUser);
            if (selectedEmail == null || selectedEmail.isEmpty()) {
                Toast.makeText(AddDebtActivity.this, "Nie znaleziono adresu e-mail dla wybranego użytkownika", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);

                if (amount <= 0 || amount > 1_000_000) {
                    Toast.makeText(AddDebtActivity.this, "Kwota musi być dodatnia i nie większa niż 1,000,000", Toast.LENGTH_SHORT).show();
                    return;
                }

                Debt debt = new Debt(name, amount, additionalInfo, selectedUser);
                debtList.add(debt);
                saveDebtToFile(debt);

                // Automatyczne wysyłanie e-maila
                new Thread(() -> {
                    try {
                        MailSender mailSender = new MailSender("centuscentaury@gmail.com", "oduu ebfs tiie rdol");
                        String subject = "Powiadomienie o nowym długu";
                        String messageBody = "Witaj,\n\n" +
                                "Zostałeś dodany jako dłużnik w aplikacji Centus. Szczegóły dotyczące długu:\n\n" +
                                "Nazwa długu: " + name + "\n" +
                                "Kwota: " + amount + " zł\n" +
                                (additionalInfo.isEmpty() ? "" : "Dodatkowe informacje: " + additionalInfo + "\n\n") +
                                "Prosimy o kontakt w celu rozliczenia.\n\n" +
                                "Pozdrawiamy,\n" +
                                "Zespół Centus";

                        mailSender.sendEmail(selectedEmail, subject, messageBody);

                        runOnUiThread(() -> Toast.makeText(AddDebtActivity.this, "E-mail został pomyślnie wysłany do dłużnika.", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(AddDebtActivity.this, "Błąd podczas wysyłania e-maila.", Toast.LENGTH_SHORT).show());
                    }
                }).start();

                // Kontynuacja działania aplikacji
                Intent resultIntent = new Intent();
                resultIntent.putExtra("debtName", name);
                resultIntent.putExtra("debtAmount", amount);
                resultIntent.putExtra("debtInfo", additionalInfo);
                resultIntent.putExtra("debtUser", selectedUser);
                setResult(Activity.RESULT_OK, resultIntent);

                new AlertDialog.Builder(AddDebtActivity.this)
                        .setTitle("Potwierdzenie")
                        .setMessage("Dług został dodany. Czy chcesz wrócić do ekranu głównego?")
                        .setPositiveButton("Tak", (dialog, which) -> {
                            Intent intent = new Intent(AddDebtActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Nie", (dialog, which) -> {
                            nameEditText.setText("");
                            amountEditText.setText("");
                            infoEditText.setText("");
                            userSpinner.setSelection(0);
                        })
                        .show();

            } catch (NumberFormatException e) {
                Toast.makeText(AddDebtActivity.this, "Nieprawidłowa kwota", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDebtToFile(Debt debt) {
        try (FileOutputStream fos = openFileOutput("debts.txt", MODE_APPEND);
             OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8")) {
            String debtData = debt.name + ";" + debt.amount + ";" + debt.additionalInfo + ";" + debt.user + "\n";
            writer.write(debtData);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu długu", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUsersFromFile() {
        userList.clear();
        userEmailMap.clear();
        try (FileInputStream fis = openFileInput("users.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(";");
                if (userData.length == 4) {
                    String fullName = userData[0] + " " + userData[1];
                    String email = userData[3];
                    userList.add(fullName);
                    userEmailMap.put(fullName, email);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu użytkowników", Toast.LENGTH_SHORT).show();
        }

        if (userList.isEmpty()) {
            Toast.makeText(this, "Brak zapisanych użytkowników. Dodaj użytkowników, aby kontynuować.", Toast.LENGTH_LONG).show();
        }
    }

    public static class Debt {
        String name;
        double amount;
        String additionalInfo;
        String user;

        public Debt(String name, double amount, String additionalInfo, String user) {
            this.name = name;
            this.amount = amount;
            this.additionalInfo = additionalInfo;
            this.user = user;
        }
    }
}
