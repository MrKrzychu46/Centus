package com.example.centus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class AddDebtActivity extends Activity {

    public ArrayList<Debt> debtList = new ArrayList<>(); // Lista długów

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_debt);

        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddDebtActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddDebtActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button addUsersButton = findViewById(R.id.addingUsersButton);
        addUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddDebtActivity.this, AddUserActivity.class);
                startActivity(intent);
            }
        });


        Button profileButton = findViewById(R.id.profileButton);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddDebtActivity.this, MyProfileActivity.class);
                startActivity(intent);
            }
        });

        Button groupsButton = findViewById(R.id.groupsButton);

        groupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddDebtActivity.this, MyGroupsActivity.class);
                startActivity(intent);
            }
        });

        Button settingsButton = findViewById(R.id.settingsButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddDebtActivity.this, OptionsActivity.class);
                startActivity(intent);
            }
        });

        // Odczytujemy długi z pliku
        loadDebtsFromFile();

        // Inicjalizujemy EditText oraz ListView
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText amountEditText = findViewById(R.id.amountEditText);
        EditText infoEditText = findViewById(R.id.infoEditText);
        Button addDebtButton = findViewById(R.id.addDebtButton);


        // Ustawiamy kliknięcie przycisku "Dodaj dług"
        addDebtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String amountText = amountEditText.getText().toString();
                String additionalInfo = infoEditText.getText().toString();

                if (name.isEmpty() || amountText.isEmpty()) {
                    Toast.makeText(AddDebtActivity.this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    Debt debt = new Debt(name, amount, additionalInfo);
                    debtList.add(debt);

                    // Przekaż nowy dług jako wynik do MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("debtName", name);
                    resultIntent.putExtra("debtAmount", amount);
                    resultIntent.putExtra("debtInfo", additionalInfo);
                    setResult(Activity.RESULT_OK, resultIntent);

                    finish(); // Zamknij AddDebtActivity po dodaniu długu
                } catch (NumberFormatException e) {
                    Toast.makeText(AddDebtActivity.this, "Nieprawidłowa kwota", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

        // Metoda zapisująca długi do pliku
    private void saveDebtsToFile() {
        try {
            // Tworzymy plik w lokalnej pamięci urządzenia
            FileOutputStream fos = openFileOutput("debts.txt", MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            // Zapisujemy długi w pliku
            for (Debt debt : debtList) {
                writer.write(debt.name + " - " + debt.amount + " zł\n" + debt.additionalInfo + "\n");
            }

            writer.close(); // Zamykamy plik
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu do pliku", Toast.LENGTH_SHORT).show();
        }
    }

    // Metoda ładująca długi z pliku
    private void loadDebtsFromFile() {
        try {
            FileInputStream fis = openFileInput("debts.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            Debt currentDebt = null;

            while ((line = reader.readLine()) != null) {
                if (line.contains(" - ")) { // Pierwsza linia zawiera nazwę i kwotę
                    String[] parts = line.split(" - ");
                    String name = parts[0];
                    double amount = Double.parseDouble(parts[1].replace(" zł", ""));
                    String additionalInfo = reader.readLine(); // Druga linia to dodatkowe informacje
                    currentDebt = new Debt(name, amount, additionalInfo);
                    debtList.add(currentDebt);
                }
            }

            reader.close(); // Zamykamy plik
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu z pliku", Toast.LENGTH_SHORT).show();
        }
    }



    // Klasa reprezentująca dług
    public static class Debt {
        String name;
        double amount;
        String additionalInfo;

        public Debt(String name, double amount, String additionalInfo) {
            this.name = name;
            this.amount = amount;
            this.additionalInfo = additionalInfo;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Zapisujemy długi do pliku, gdy aplikacja jest w tle
        saveDebtsToFile();
    }
}
