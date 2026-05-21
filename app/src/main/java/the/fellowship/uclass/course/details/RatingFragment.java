package the.fellowship.uclass.course.details;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import the.fellowship.eclass.dtos.Course;
import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentRatingBinding;

public class RatingFragment extends Fragment {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";

    private Course course;
    private FragmentRatingBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRatingBinding.inflate(inflater, container, false);

        String id = getArguments().getString(EXTRA_COURSE_ID);
        course = UClass.eclass.getCourse(id);

        binding.titleText.setText(course.getTitle());

        binding.submitButton.setOnClickListener(v -> submit());

        return binding.getRoot();
    }

    private void submit() {
        float rating = binding.ratingBar.getRating();
        String comment = binding.commentEdit.getText().toString();

        // Cleanup
        binding.ratingBar.setRating(0);
        binding.commentEdit.setText("");
        binding.commentEdit.clearFocus();

        CompletableFuture.runAsync(() -> {
            try {
                RecordModel auth = UClass.pocketbase.getAuthStore().getRecord();
                final Map<String, ?> body = Map.of(
                        "author", auth.getId(),
                        "course", course.getId(),
                        "name", auth.<String>getValue("name"),
                        "rating", rating,
                        "comment", comment.strip()
                );

                final RecordModel message = UClass.pocketbase.getCollection("ratings").create(body, null);

                //NavHostFragment.findNavController(RatingFragment.this).popBackStack();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Snackbar.make(binding.getRoot(), "Η αξιολόγηση υποβλήθηκε με επιτυχία!", Snackbar.LENGTH_LONG).setTextMaxLines(16).setAction("Action", null).show());
                }
                Log.d("Rating", String.valueOf(message));
            } catch (ClientException err) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Snackbar.make(binding.getRoot(), String.format("Failed connecting to PocketBase: \n%s", err), Snackbar.LENGTH_LONG).setTextMaxLines(16).setAction("Action", null).show());
                }
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}