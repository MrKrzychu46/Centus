package com.example.centus;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText, surnameEditText, phoneEditText;
    private static final String TAG = "RegisterActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicjalizacja FirebaseAuth, Firestore i App Check
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicjalizacja widoków
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        nameEditText = findViewById(R.id.nameEditText);
        surnameEditText = findViewById(R.id.surnameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        Button registerButton = findViewById(R.id.registerButton);

        // Rejestracja użytkownika
        registerButton.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(RegisterActivity.this, "Brak połączenia z Internetem. Nie można się zarejestrować.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Brak połączenia z Internetem");
                return;
            }

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Proszę wypełnić wszystkie wymagane pola", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Hasła nie są zgodne", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(email, password, name, surname, phone);
        });
    }

    private void registerUser(String email, String password, String name, String surname, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            addUserToFirestore(user, name, surname, phone);
                        }
                        Toast.makeText(RegisterActivity.this, "Rejestracja powiodła się", Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        // Logowanie błędów do Logcat
                        Toast.makeText(RegisterActivity.this, "Rejestracja nie powiodła się", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Rejestracja nie powiodła się", task.getException());
                    }
                });
    }

    private void addUserToFirestore(FirebaseUser user, String name, String surname, String phone) {
        String userId = user.getUid(); // Używamy UID Firebase
        String email = user.getEmail();

        if (email != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("email", email);
            userMap.put("name", name);
            userMap.put("surname", surname);
            if (!phone.isEmpty()) {
                userMap.put("phone", phone);
            }

            db.collection("users").document(userId).set(userMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Użytkownik dodany do Firestore: " + userId))
                    .addOnFailureListener(e -> Log.w(TAG, "Błąd podczas dodawania użytkownika do Firestore", e));
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        Log.d(TAG, "isNetworkAvailable: " + isConnected);
        return isConnected;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
