package the.fellowship.uclass.courses;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

import the.fellowship.eclass.EClass;
import the.fellowship.uclass.R;
import the.fellowship.uclass.databinding.FragmentCoursesBinding;

public class CoursesFragment extends Fragment {

    private FragmentCoursesBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCoursesBinding.inflate(inflater, container, false);

        final EClass eclass = new EClass("https://eclass.aueb.gr");
        final String username = "";
        final String password = "";

        eclass.login(username, password)
                .thenAccept(success -> {
                    if (!success) {
                        showMessage("Incorrect username or password");
                        return;
                    }
                    showMessage(String.format("Welcome, %s!", username));

                    eclass.getCourses().thenAccept(courses -> {
                        StringBuilder list = new StringBuilder();
                        for (Map<String, String> course : courses) {
                            list.append(String.format("%s\n", course.get("title")));
                        }
                        populateCourses(list.toString());
                    });
                })
                .exceptionally(err -> {
                    if (err != null) {
                        showMessage(String.format("Failed to login: \"%s\"", err.getMessage().substring(err.getMessage().indexOf(":") + 2)));
                    }
                    return null;
                });

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void navigateToCourse(String courseId) {
        showMessage(String.format("Navigate to: %s\n", courseId));
    }

    public void populateCourses(String courses) {
        if (getActivity() == null || !isAdded() || getView() == null) {
            Log.e("CoursesFragment/populateCourses", "Can not use UI Thread");
            return;
        }

        TextView text = getView().findViewById(R.id.textview_second);
        getActivity().runOnUiThread(() -> {
            text.setText(courses);
        });
    }

    public void showMessage(String msg) {
        if (getActivity() == null || !isAdded() || getView() == null) {
            Log.e("CoursesFragment/showMessage", String.format("Can not use UI Thread. MSG: %s", msg));
            return;
        }

        getActivity().runOnUiThread(() -> {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.bottom_navigation)
                    .setAction("Action", null).show();
        });
    }
}