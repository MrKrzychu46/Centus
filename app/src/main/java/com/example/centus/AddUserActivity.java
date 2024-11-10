package com.example.centus;

import android.annotation.SuppressLint;
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

public class AddUserActivity extends Activity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private Button addUserButton;
    private ArrayList<String> userList;
    private ArrayAdapter<String> userAdapter;
    private ListView userListView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user);

        //Przyciski nawigacji
        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddUserActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddUserActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton DebtsButton = findViewById(R.id.addingDebtsButton);
        DebtsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddUserActivity.this, AddDebtActivity.class);
                startActivity(intent);
            }
        });


        Button profileButton = findViewById(R.id.profileButton);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddUserActivity.this, MyProfileActivity.class);
                startActivity(intent);
            }
        });

        Button groupsButton = findViewById(R.id.groupsButton);

        groupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddUserActivity.this, MyGroupsActivity.class);
                startActivity(intent);
            }
        });

        Button settingsButton = findViewById(R.id.settingsButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddUserActivity.this, OptionsActivity.class);
                startActivity(intent);
            }
        });

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addUserButton = findViewById(R.id.addUserButton);
        userListView = findViewById(R.id.userListView);

        userList = new ArrayList<>();
        loadUsersFromFile();

        userListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String user = userList.get(position);
            userList.remove(position);
            userAdapter.notifyDataSetChanged();
            saveUsersToFile();
            Toast.makeText(AddUserActivity.this, "Usunięto użytkownika: " + user, Toast.LENGTH_SHORT).show();
            return true;
        });

        // Adapter do wyświetlania danych w ListView
        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        userListView.setAdapter(userAdapter);

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });
    }

    private void addUser() {
        // Pobieranie danych z pól tekstowych
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        // Walidacja danych
        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie wymagane pola!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            Toast.makeText(this, "Nieprawidłowy numer telefonu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Nieprawidłowy adres email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tworzenie ciągu z danymi użytkownika
        String userInfo = firstName + " " + lastName + " - " + phone + " - " + email;

        // Przekształcenie ciągu na małe litery dla porównania
        String userInfoLowerCase = userInfo.toLowerCase();

        // Sprawdzenie, czy użytkownik już istnieje (ignorując wielkość liter)
        boolean userExists = false;
        for (String user : userList) {
            if (user.toLowerCase().equals(userInfoLowerCase)) {
                userExists = true;
                break;
            }
        }

        if (userExists) {
            Toast.makeText(this, "Użytkownik już istnieje!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dodanie użytkownika do listy
        userList.add(userInfo);
        userAdapter.notifyDataSetChanged();

        // Zapisanie użytkownika do pliku
        saveUsersToFile();

        // Czyszczenie pól formularza
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        phoneEditText.setText("");
        emailEditText.setText("");
        Toast.makeText(this, "Użytkownik dodany!", Toast.LENGTH_SHORT).show();
    }





    private void saveUsersToFile() {
        try (FileOutputStream fos = openFileOutput("users.txt", MODE_PRIVATE);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {

            for (String user : userList) {
                writer.write(user);
                writer.newLine();
            }

            Toast.makeText(this, "Użytkownik zapisany!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu użytkownika", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadUsersFromFile() {
        try (FileInputStream fis = openFileInput("users.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            String line;
            while ((line = reader.readLine()) != null) {
                userList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu użytkowników", Toast.LENGTH_SHORT).show();
        }
    }

}
