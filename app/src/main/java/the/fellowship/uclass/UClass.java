package the.fellowship.uclass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import the.fellowship.eclass.EClass;
import the.fellowship.eclass.cookies.PrefsCookieJar;
import the.fellowship.uclass.databinding.ActivityLoginBinding;

public class UClass extends AppCompatActivity {

    public static EClass eclass;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        eclass = new EClass("https://eclass.aueb.gr", new PrefsCookieJar(prefs));

        // Check for stored credentials, redirect to Main
        if (prefs.contains("username") && prefs.contains("password")) {
            // TODO: Instead of `login` create function `loadSession`
            eclass.login(prefs.getString("username", ""), prefs.getString("password", ""))
                    .thenAccept(success -> {
                        if (!success) {
                            Intent intent = new Intent(this, UClass.class);
                            startActivity(intent);
                        }
                        eclass.fetchAll();
                    })
                    .exceptionally(err -> {
                        Intent intent = new Intent(this, UClass.class);
                        startActivity(intent);
                        return null;
                    });
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        findViewById(R.id.login_button).setOnClickListener(view -> {
            String username = ((TextInputEditText) findViewById(R.id.username_edit)).getText().toString();
            String password = ((TextInputEditText) findViewById(R.id.password_edit)).getText().toString();

            eclass.login(username, password)
                    .thenAccept(success -> {
                        if (!success) {
                            runOnUiThread(() -> Snackbar.make(view, "Incorrect username or password", Snackbar.LENGTH_LONG).setAction("Action", null).show());
                            return;
                        }

                        // Store credentials
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.apply();

                        eclass.fetchAll();

                        runOnUiThread(() -> {
                            Snackbar.make(view, String.format("Welcome, %s!", username), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                        });
                    })
                    .exceptionally(err -> {
                        if (err != null)
                            runOnUiThread(() -> Snackbar.make(view, String.format("Failed to login: \"%s\"", err.getMessage().substring(err.getMessage().indexOf(":") + 2)), Snackbar.LENGTH_LONG).setAction("Action", null).show());
                        return null;
                    });
        });
    }
}