package com.example.centus;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<AddDebtActivity.Debt> debtList = new ArrayList<>();
    private LinearLayout debtsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        debtsLayout = findViewById(R.id.debtsLayout);

        loadDebtsFromFile(); // Załaduj istniejące długi z pliku
        displayDebts(); // Wyświetl listę długów

        // Przycisk do dodawania nowego długu
        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddDebtActivity.class);
            startActivityForResult(intent, 1); // Uruchamiamy AddDebtActivity z kodem żądania 1
        });
    }

    // Obsługa wyniku z AddDebtActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Pobierz nowy dług z Intenta
            String name = data.getStringExtra("debtName");
            double amount = data.getDoubleExtra("debtAmount", 0.0);
            String additionalInfo = data.getStringExtra("debtInfo");

            // Dodaj nowy dług do listy i odśwież wyświetlanie
            debtList.add(new AddDebtActivity.Debt(name, amount, additionalInfo));
            displayDebts();
        }
    }

    // Wyświetlanie długów na ekranie
    private void displayDebts() {
        debtsLayout.removeAllViews(); // Wyczyść poprzednie przyciski

        for (int i = 0; i < debtList.size(); i++) {
            Button debtButton = new Button(this);
            AddDebtActivity.Debt debt = debtList.get(i);
            debtButton.setText("Dług " + (i + 1) + ": " + debt.name);

            final int index = i; // Przechowuje indeks dla przycisku
            debtButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DebtDetailActivity.class);
                intent.putExtra("debtTitle", debtList.get(index).name);
                intent.putExtra("debtAmount", debtList.get(index).amount + " zł");
                intent.putExtra("debtDescription", debtList.get(index).additionalInfo);
                startActivity(intent);
            });

            debtsLayout.addView(debtButton); // Dodaj przycisk do layoutu
        }


    // Załaduj listę długów z pliku


        // Set up other buttons
        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddDebtActivity.class);
            startActivity(intent);
        });



        Button groupsButton = findViewById(R.id.groupsButton);
        groupsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyGroupsActivity.class);
            startActivity(intent);
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyProfileActivity.class);
            startActivity(intent);
        });

        Button optionsButton = findViewById(R.id.settingsButton);
        optionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
            startActivity(intent);
        });
    }

    // Load list of debts from file
    private void loadDebtsFromFile() {
        try {
            FileInputStream fis = openFileInput("debts.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains(" - ")) {
                    String[] parts = line.split(" - ");
                    String name = parts[0];
                    double amount = Double.parseDouble(parts[1].replace(" zł", ""));
                    String additionalInfo = reader.readLine();
                    debtList.add(new AddDebtActivity.Debt(name, amount, additionalInfo));
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu z pliku", Toast.LENGTH_SHORT).show();
        }
    }
}
