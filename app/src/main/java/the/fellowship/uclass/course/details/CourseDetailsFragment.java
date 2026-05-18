package the.fellowship.uclass.course.details;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import the.fellowship.eclass.dtos.Assignment;
import the.fellowship.eclass.dtos.Course;
import the.fellowship.eclass.dtos.Lecturer;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentCourseDetailsBinding;

public class CourseDetailsFragment extends Fragment {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";

    private List<Assignment> assignments;
    private AssignmentsAdapter assignmentAdapter;
    private Course course;
    private FragmentCourseDetailsBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCourseDetailsBinding.inflate(inflater, container, false);

        String id = getArguments().getString(EXTRA_COURSE_ID);
        course = UClass.eclass.getCourse(id);
        Lecturer lecturer = course.getLecturer();

        binding.titleText.setText(course.getTitle());
        binding.lecturerNameText.setText(lecturer.getName());

        assignments = new ArrayList<>();
        assignmentAdapter = new AssignmentsAdapter(assignments);
        binding.assignmentsRecycler.setAdapter(assignmentAdapter);

        UClass.eclass.getAssignments().observe(getViewLifecycleOwner(), this::populateAssignment);

        UClass.eclass.fetchLecturerDetails(id).thenAccept(lec -> {
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                binding.lecturerEmailText.setText(lec.getEmail() != null ? lec.getEmail() : "Δεν βρέθηκε ηλεκτρονική διεύθυνση");
                binding.lecturerHoursText.setText(lec.getHours() != null ? lec.getHours() : "Δεν βρέθηκαν ώρες γραφείου");
                binding.lecturerOfficeText.setText(lec.getOffice() != null ? lec.getOffice() : "Δεν βρέθηκε γραφείο");
            });
        });

        return binding.getRoot();
    }

    public void populateAssignment(List<Assignment> list) {
        if (getActivity() == null) {
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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}