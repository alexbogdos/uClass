package the.fellowship.uclass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import the.fellowship.eclass.EClass;
import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.RecordAuth;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.uclass.databinding.ActivityLoginBinding;

public class UClass extends AppCompatActivity {

    public static EClass eclass;
    public static PocketBase pocketbase;
    private SharedPreferences credentials;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences cache = getSharedPreferences("cache", Context.MODE_PRIVATE);
        credentials = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        eclass = new EClass("https://eclass.aueb.gr", cache);
        pocketbase = new PocketBase("https://guacamole230.duckdns.org:8090");

        UClass.eclass.getNotification().observe(this, this::showSnackbar);
        UClass.pocketbase.getNotification().observe(this, this::showSnackbar);

        // Check for stored credentials, redirect to Main
        if (credentials.contains("username") && credentials.contains("password")) {
            String username = credentials.getString("username", "");
            String password = credentials.getString("password", "");

            loginToEClass(username, password);
            eclass.fetchCached();  // Fetch network data after logging in

            loginToPocketBase(username, password);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        binding.loginButton.setOnClickListener(view -> {
            String username = binding.usernameEdit.getText().toString();
            String password = binding.passwordEdit.getText().toString();

            AuthenticateToEClass(username, password);
            subscribeToPocketBase(username, password);
        });
    }

    /**
     * Login to eclass with existing credentials.
     * This function is used for receiving the session token, but it assumes the credentials are valid.
     */
    private void loginToEClass(String username, String password) {
        eclass.login(username, password)
                .thenAccept(success -> {
                    if (!success) {
                        Intent intent = new Intent(this, UClass.class);
                        startActivity(intent);
                    }
                    eclass.fetchNetwork();
                })
                .exceptionally(err -> {
                    eclass.postNotification("Αποτυχία σύνδεσης με το eclass.aueb.gr");

                    Intent intent = new Intent(this, UClass.class);
                    startActivity(intent);
                    return null;
                });
    }

    /**
     * Login to eclass, validate and store credentials.
     */
    private void AuthenticateToEClass(String username, String password) {
        eclass.login(username, password)
                .thenAccept(success -> {
                    if (!success) {
                        eclass.postNotification("Λανθασμένο Όνομα Χρήστη ή Συνθηματικό");
                        return;
                    }

                    eclass.fetchNetwork();

                    // Store credentials
                    SharedPreferences.Editor editor = credentials.edit();
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.apply();

                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    });
                })
                .exceptionally(err -> {
                    Log.e("Login", String.valueOf(err));
                    eclass.postNotification("Αποτυχία σύνδεσης με το eclass.aueb.gr");
                    return null;
                });
    }


    /**
     * Login to PocketBase
     */
    private void loginToPocketBase(String username, String password) {
        CompletableFuture.runAsync(() -> {
            try {
                final RecordAuth auth = pocketbase.getCollection("users").authWithPassword(String.format("%s@aueb.gr", username), password);
                Log.d("Login", String.format("Connected to PocketBase as \"%s\"", auth.getRecord().<String>getValue("name")));
            } catch (ClientException err) {
                Log.e("Login", String.valueOf(err));
                pocketbase.postNotification("Αποτυχία σύνδεσης με το διακομιστή");
            }
        });
    }

    /**
     * Create new account to PocketBase
     */
    private void subscribeToPocketBase(String username, String password) {
        CompletableFuture.runAsync(() -> {
            try {
                final Map<String, ?> body = Map.of(
                        "email", String.format("%s@aueb.gr", username),
                        "name", username,
                        "password", password,
                        "passwordConfirm", password
                );
                RecordModel auth = pocketbase.getCollection("users").create(body, null);
                Log.d("Login", String.format("Connected to PocketBase as \"%s\"", auth.<String>getValue("name")));
            } catch (ClientException err) {
                pocketbase.postNotification("Αποτυχία σύνδεσης με το διακομιστή");
            }
        });
    }

    private void showSnackbar(String notification) {
        runOnUiThread(() -> Snackbar.make(binding.getRoot(), notification, Snackbar.LENGTH_LONG).setTextMaxLines(16).setAction("Action", null).show());
        Log.i("Notification", notification);
    }
}