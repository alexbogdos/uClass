package the.fellowship.uclass.course.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import the.fellowship.eclass.dtos.Course;
import the.fellowship.eclass.dtos.Lecturer;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentCourseDetailsBinding;

public class CourseDetailsFragment extends Fragment {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";

    private FragmentCourseDetailsBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCourseDetailsBinding.inflate(inflater, container, false);

        String id = getArguments().getString(EXTRA_COURSE_ID);
        Course course = UClass.eclass.getCourse(id);
        Lecturer lecturer = course.getLecturer();

        binding.titleText.setText(course.getTitle());
        binding.lecturerNameText.setText(lecturer.getName());

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
}