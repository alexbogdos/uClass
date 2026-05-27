package the.fellowship.uclass.course.details;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import the.fellowship.eclass.dtos.Assignment;
import the.fellowship.eclass.dtos.Course;
import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentCourseDetailsBinding;

public class CourseDetailsFragment extends Fragment {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";

    private List<Assignment> assignments;
    private AssignmentsAdapter assignmentAdapter;
    private List<RecordModel> ratings;
    private RatingsAdapter ratingsAdapter;
    private Course course;
    private String filter;
    private FragmentCourseDetailsBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCourseDetailsBinding.inflate(inflater, container, false);

        String id = getArguments().getString(EXTRA_COURSE_ID);
        course = UClass.eclass.getCourse(id);
        filter = String.format("course = '%s'", id);

        binding.titleText.setText(course.getTitle());
        binding.lecturerNameText.setText(course.getLecturer().getName());

        assignments = new ArrayList<>();
        assignmentAdapter = new AssignmentsAdapter(assignments);
        binding.assignmentsRecycler.setAdapter(assignmentAdapter);

        ratings = new ArrayList<>();
        ratingsAdapter = new RatingsAdapter(ratings);
        binding.ratingsRecycler.setAdapter(ratingsAdapter);

        fetchRatingsAsync();

        UClass.eclass.getAssignments().observe(getViewLifecycleOwner(), this::populateAssignment);

        UClass.eclass.fetchLecturerDetails(id).thenAccept(lec -> {
            if (getActivity() == null || binding == null) {
                Log.e("Course", "Cannot use UI Thread");
                return;
            }

            getActivity().runOnUiThread(() -> {
                binding.lecturerEmailText.setText(lec.getEmail() != null ? lec.getEmail() : "Δεν βρέθηκε ηλεκτρονική διεύθυνση");
                binding.lecturerHoursText.setText(lec.getHours() != null ? lec.getHours() : "Δεν βρέθηκαν ώρες γραφείου");
                binding.lecturerOfficeText.setText(lec.getOffice() != null ? lec.getOffice() : "Δεν βρέθηκε γραφείο");
            });
        });

        binding.buttonAdd.setOnClickListener(this::navigateToRating);

        return binding.getRoot();
    }

    public void populateAssignment(List<Assignment> list) {
        if (getActivity() == null || binding == null) {
            Log.e("Course", "Cannot use UI Thread");
            return;
        }

        List<Assignment> filtered = list.stream().filter(assignment -> course.getId().equals(assignment.getCourseId())).collect(Collectors.toList());
        getActivity().runOnUiThread(() -> {
            if (filtered.isEmpty()) {
                binding.assignmentsLabel.setVisibility(View.GONE);
                binding.assignmentsRecycler.setVisibility(View.GONE);
            } else {
                assignments.clear();
                assignments.addAll(filtered);
                assignmentAdapter.notifyDataSetChanged();

                binding.assignmentsLabel.setVisibility(View.VISIBLE);
                binding.assignmentsRecycler.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchRatingsAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                final List<RecordModel> ratings = UClass.pocketbase.getCollection("ratings").getFullList(filter);
                Log.d("Course", String.format("Ratings: %s", ratings));
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        this.ratings.clear();
                        this.ratings.addAll(ratings);
                        ratingsAdapter.notifyItemRangeInserted(0, this.ratings.size());
                    });
                } else {
                    Log.e("Chat", "Cannot use UI Thread");
                }
            } catch (ClientException err) {
                UClass.pocketbase.postNotification("Αποτυχία σύνδεσης με το διακομιστή");
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });
    }

    private void navigateToRating(View view) {
        Bundle bundle = new Bundle();
        bundle.putString(RatingFragment.EXTRA_COURSE_ID, course.getId());

        NavHostFragment.findNavController(CourseDetailsFragment.this).navigate(R.id.action_CourseDetailsFragment_to_RatingFragment, bundle);
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