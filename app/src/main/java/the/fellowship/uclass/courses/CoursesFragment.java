package the.fellowship.uclass.courses;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import the.fellowship.eclass.dtos.Course;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.course.announcements.AnnouncementsFragment;
import the.fellowship.uclass.course.chat.ChatFragment;
import the.fellowship.uclass.course.details.CourseDetailsFragment;
import the.fellowship.uclass.databinding.FragmentCoursesBinding;

public class CoursesFragment extends Fragment implements CoursesAdapter.SelectionListener {

    private List<Course> items;
    private CoursesAdapter adapter;
    private FragmentCoursesBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCoursesBinding.inflate(inflater, container, false);

        items = new ArrayList<>();
        adapter = new CoursesAdapter(items, this);
        binding.recycler.setAdapter(adapter);

        UClass.eclass.getCourses().observe(getViewLifecycleOwner(), this::populateCourses);

        return binding.getRoot();
    }

    public void populateCourses(List<Course> courses) {
        if (getActivity() == null) {
            Log.e("CoursesFragment", "Cannot use UI Thread");
            return;
        }

        getActivity().runOnUiThread(() -> {
            items.clear();
            items.addAll(courses);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void navigateToDetails(Course course) {
        Bundle bundle = new Bundle();
        bundle.putString(CourseDetailsFragment.EXTRA_COURSE_ID, course.getId());

        NavHostFragment.findNavController(CoursesFragment.this).navigate(R.id.action_CoursesFragment_to_CourseDetailsFragment, bundle);
    }

    @Override
    public void navigateToChat(Course course) {
        Bundle bundle = new Bundle();
        bundle.putString(ChatFragment.EXTRA_COURSE_ID, course.getId());

        NavHostFragment.findNavController(CoursesFragment.this).navigate(R.id.action_CoursesFragment_to_ChatFragment, bundle);
    }

    @Override
    public void navigateToAnnouncements(Course course) {
        Bundle bundle = new Bundle();
        bundle.putString(AnnouncementsFragment.EXTRA_COURSE_ID, course.getId());

        NavHostFragment.findNavController(CoursesFragment.this).navigate(R.id.action_CoursesFragment_to_CourseAnnouncementsFragment, bundle);
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