package the.fellowship.uclass;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.RecordAuth;
import the.fellowship.uclass.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    final PocketBase pb = new PocketBase("https://guacamole230.duckdns.org:8090");
                    try {
                        RecordAuth auth = pb.getCollection("users").authWithPassword("", "");

                        runOnUiThread(() -> {
                            Snackbar.make(view, String.format("Welcome, %s\n", auth.getIdentifier()), Snackbar.LENGTH_LONG)
                                    .setAnchorView(R.id.fab)
                                    .setAction("Action", null).show();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG)
                                    .setAnchorView(R.id.fab)
                                    .setAction("Action", null).show();
                            e.printStackTrace();
                        });
                    }

                    // "logout"
                    pb.getAuthStore().clear();
                }).start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}