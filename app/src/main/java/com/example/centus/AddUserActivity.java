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
    private ArrayList<User> userList;
    private ArrayAdapter<String> userAdapter;
    private ListView userListView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user);

        // Przyciski nawigacji
        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddUserActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddUserActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton debtsButton = findViewById(R.id.addingDebtsButton);
        debtsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddUserActivity.this, AddDebtActivity.class);
            startActivity(intent);
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddUserActivity.this, MyProfileActivity.class);
            startActivity(intent);
        });

        Button groupsButton = findViewById(R.id.groupsButton);
        groupsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddUserActivity.this, MyGroupsActivity.class);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddUserActivity.this, OptionsActivity.class);
            startActivity(intent);
        });

        // Inicjalizacja elementów UI
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addUserButton = findViewById(R.id.addUserButton);
        userListView = findViewById(R.id.userListView);

        userList = new ArrayList<>();
        loadUsersFromFile();

        userListView.setOnItemLongClickListener((parent, view, position, id) -> {
            User user = userList.get(position);
            userList.remove(position);
            updateUserAdapter();
            saveUsersToFile();
            Toast.makeText(AddUserActivity.this, "Usunięto użytkownika: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            return true;
        });

        addUserButton.setOnClickListener(v -> addUser());

        updateUserAdapter();
    }

    private void addUser() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

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

        for (User user : userList) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                Toast.makeText(this, "Użytkownik z tym emailem już istnieje!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        User newUser = new User(firstName, lastName, phone, email);
        userList.add(newUser);
        updateUserAdapter();
        saveUsersToFile();

        firstNameEditText.setText("");
        lastNameEditText.setText("");
        phoneEditText.setText("");
        emailEditText.setText("");
        Toast.makeText(this, "Użytkownik dodany!", Toast.LENGTH_SHORT).show();
    }

    private void updateUserAdapter() {
        ArrayList<String> userStrings = new ArrayList<>();
        for (User user : userList) {
            userStrings.add(user.toString());
        }
        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userStrings);
        userListView.setAdapter(userAdapter);
    }

    private void saveUsersToFile() {
        try (FileOutputStream fos = openFileOutput("users.txt", MODE_PRIVATE);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {

            for (User user : userList) {
                writer.write(user.getFirstName() + ";" + user.getLastName() + ";" + user.getPhone() + ";" + user.getEmail());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu użytkownika", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUsersFromFile() {
        userList.clear();
        try (FileInputStream fis = openFileInput("users.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(";");
                if (userData.length == 4) {
                    User user = new User(userData[0], userData[1], userData[2], userData[3]);
                    userList.add(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu użytkowników", Toast.LENGTH_SHORT).show();
        }
        updateUserAdapter();
    }
}
