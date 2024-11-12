package com.example.centus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

public class AddDebtActivity extends Activity {

    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<Debt> debtList = new ArrayList<>(); // Lista długów
    private Spinner userSpinner; // Pole wyboru użytkownika

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

        // Ładujemy użytkowników z pliku
        loadUsersFromFile();

        // Inicjalizujemy spinner (pole wyboru użytkownika)
        userSpinner = findViewById(R.id.userSpinner);
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(userAdapter);

        // Inicjalizujemy pola i przyciski
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText amountEditText = findViewById(R.id.amountEditText);
        EditText infoEditText = findViewById(R.id.infoEditText);
        Button addDebtButton = findViewById(R.id.addDebtButton);

        // Obsługa kliknięcia przycisku "Dodaj dług"
        addDebtButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String amountText = amountEditText.getText().toString().trim();
            String additionalInfo = infoEditText.getText().toString().trim();
            String selectedUser = (String) userSpinner.getSelectedItem();

            if (name.isEmpty() || amountText.isEmpty() || selectedUser == null || selectedUser.isEmpty()) {
                Toast.makeText(AddDebtActivity.this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);

                // Nowe sprawdzenie, czy kwota jest większa od zera i mniejsza od limitu
                if (amount <= 0 || amount > 1_000_000) {
                    Toast.makeText(AddDebtActivity.this, "Kwota musi być dodatnia i nie większa niż 1,000,000", Toast.LENGTH_SHORT).show();
                    return;
                }

                Debt debt = new Debt(name, amount, additionalInfo, selectedUser);
                debtList.add(debt);
                saveDebtToFile(debt);

                // Kontynuujemy tak jak wcześniej
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
                            // Czyszczenie pól formularza
                            nameEditText.setText("");
                            amountEditText.setText("");
                            infoEditText.setText("");
                            userSpinner.setSelection(0); // Przywracamy domyślną wartość w Spinnerze
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

    // Metoda do ładowania użytkowników z pliku i parsowania danych
    private void loadUsersFromFile() {
        userList.clear();
        try (FileInputStream fis = openFileInput("users.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(";");
                if (userData.length == 4) {
                    String fullName = userData[0] + " " + userData[1];
                    userList.add(fullName);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu użytkowników", Toast.LENGTH_SHORT).show();
        }

        // Sprawdzenie, czy lista jest pusta
        if (userList.isEmpty()) {
            Toast.makeText(this, "Brak zapisanych użytkowników. Dodaj użytkowników, aby kontynuować.", Toast.LENGTH_LONG).show();
        }
    }


    // Klasa reprezentująca dług
    public static class Debt {
        String name;
        double amount;
        String additionalInfo;
        String user; // Dodane pole dla użytkownika

        public Debt(String name, double amount, String additionalInfo, String user) {
            this.name = name;
            this.amount = amount;
            this.additionalInfo = additionalInfo;
            this.user = user;
        }
    }
}
