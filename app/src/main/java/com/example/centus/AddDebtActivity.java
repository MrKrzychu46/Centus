package com.example.centus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class AddDebtActivity extends Activity {

    private ArrayList<String> userList = new ArrayList<>();
    private HashMap<String, String> userEmailMap = new HashMap<>();
    private ArrayList<Debt> debtList = new ArrayList<>();
    private String selectedUser = ""; // Zmienna do przechowywania wybranego użytkownika

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

        // Inicjalizacja pól i przycisków
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText amountEditText = findViewById(R.id.amountEditText);
        EditText infoEditText = findViewById(R.id.infoEditText);
        Button addDebtButton = findViewById(R.id.addDebtButton);

        // Przygotowanie do wyboru użytkownika
        Button selectUserButton = findViewById(R.id.selectUserButton);
        TextView selectedUserTextView = findViewById(R.id.selectedUserTextView);

        // Wyświetlenie dialogu do wyboru użytkownika
        selectUserButton.setOnClickListener(v -> showUserDialog(selectedUserTextView));

        // Obsługa przycisku "Dodaj dług"
        addDebtButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String amountText = amountEditText.getText().toString().trim();
            String additionalInfo = infoEditText.getText().toString().trim();

            if (name.isEmpty() || amountText.isEmpty() || selectedUser.isEmpty()) {
                Toast.makeText(AddDebtActivity.this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedEmail = userEmailMap.get(selectedUser);
            if (selectedEmail == null || selectedEmail.isEmpty()) {
                Toast.makeText(AddDebtActivity.this, "Nie znaleziono adresu e-mail dla wybranego użytkownika", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String sanitizedAmountText = amountText.replaceAll("[^0-9.,]", "").replace(",", ".");
                double amount = Double.parseDouble(sanitizedAmountText);

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
                            selectedUser = "";
                            selectedUserTextView.setText("Brak wybranego użytkownika");
                        })
                        .show();

            } catch (NumberFormatException e) {
                Toast.makeText(AddDebtActivity.this, "Nieprawidłowa kwota", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDialog(TextView selectedUserTextView) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_user_list);

        ListView userListView = dialog.findViewById(R.id.userListView);
        Button closeDialogButton = dialog.findViewById(R.id.closeDialogButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        userListView.setAdapter(adapter);

        userListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedUser = userList.get(position);
            selectedUserTextView.setText("Wybrano: " + selectedUser);
            dialog.dismiss();
        });

        closeDialogButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
