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
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences cache = getSharedPreferences("cache", Context.MODE_PRIVATE);
        eclass = new EClass("https://eclass.aueb.gr", cache);
        pocketbase = new PocketBase("https://guacamole230.duckdns.org:8090");

        // Check for stored credentials, redirect to Main
        SharedPreferences credentials = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        if (credentials.contains("username") && credentials.contains("password")) {
            String username = credentials.getString("username", "");
            String password = credentials.getString("password", "");

            eclass.login(username, password)
                    .thenAccept(success -> {
                        if (!success) {
                            Intent intent = new Intent(this, UClass.class);
                            startActivity(intent);
                        }
                        eclass.fetchNetwork();
                    })
                    .exceptionally(err -> {
                        Log.e("Login", String.format("Failed connecting to EClass: \n%s", err));
                        runOnUiThread(() -> Snackbar.make(binding.getRoot(), String.format("Failed connecting to EClass: \n%s", err), Snackbar.LENGTH_LONG).setAction("Action", null).show());

                        Intent intent = new Intent(this, UClass.class);
                        startActivity(intent);
                        return null;
                    });

            eclass.fetchCached();

            CompletableFuture.runAsync(() -> {
                try {
                    final RecordAuth auth = pocketbase.getCollection("users").authWithPassword(String.format("%s@aueb.gr", username), password);
                    Log.d("Login", String.format("Connected to PocketBase as \"%s\"", auth.getRecord().<String>getValue("name")));
                } catch (ClientException err) {
                    Log.e("Login", String.format("Failed connecting to PocketBase: \n%s", err));
                    runOnUiThread(() -> Snackbar.make(binding.getRoot(), String.format("Failed connecting to PocketBase: \n%s", err), Snackbar.LENGTH_LONG).setAction("Action", null).show());
                }
            });

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        binding.loginButton.setOnClickListener(view -> {
            String username = binding.usernameEdit.getText().toString();
            String password = binding.passwordEdit.getText().toString();

            eclass.login(username, password)
                    .thenAccept(success -> {
                        if (!success) {
                            runOnUiThread(() -> Snackbar.make(view, "Incorrect username or password", Snackbar.LENGTH_LONG).setAction("Action", null).show());
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
                        Log.e("Login", String.format("Failed connecting to EClass: \n%s", err));
                        runOnUiThread(() -> Snackbar.make(view, String.format("Failed connecting to EClass: \n%s", err), Snackbar.LENGTH_LONG).setAction("Action", null).show());
                        return null;
                    });

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
                    Log.e("Login", String.format("Failed connecting to PocketBase: \n%s", err));
                    runOnUiThread(() -> Snackbar.make(view, String.format("Failed connecting to PocketBase: \n%s", err), Snackbar.LENGTH_LONG).setAction("Action", null).show());
                }
            });
        });
    }
}