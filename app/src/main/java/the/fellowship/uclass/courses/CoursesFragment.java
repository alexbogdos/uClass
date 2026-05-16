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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import the.fellowship.eclass.dtos.Course;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.course.announcements.AnnouncementsFragment;
import the.fellowship.uclass.course.details.CourseDetailsFragment;
import the.fellowship.uclass.databinding.FragmentCoursesBinding;

public class CoursesFragment extends Fragment implements CoursesAdapter.SelectionListener {

    private List<Course> items;
    private RecyclerView recycler;
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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void populateCourses(List<Course> courses) {
        if (getActivity() == null) {
            Log.e("CoursesFragment", "Can not use UI Thread");
            return;
        }

        getActivity().runOnUiThread(() -> {
            items.clear();
            items.addAll(courses);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void select(Course course) {
        Bundle bundle = new Bundle();
        bundle.putString(CourseDetailsFragment.EXTRA_COURSE_ID, course.getId());

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.CourseDetailsFragment, bundle);
    }

    @Override
    public void navigateChat(Course course) {
        if (getActivity() == null || getView() == null) {
            Log.e("CoursesFragment", "Can not use UI Thread");
            return;
        }

        getActivity().runOnUiThread(() -> Snackbar.make(getView(), String.format("Chat: [%s] %s", course.getId(), course.getTitle()), Snackbar.LENGTH_LONG).setAction("Action", null).show());
    }

    @Override
    public void navigateAnnouncements(Course course) {
        Bundle bundle = new Bundle();
        bundle.putString(AnnouncementsFragment.EXTRA_COURSE_ID, course.getId());

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.CourseAnnouncementsFragment, bundle);
    }
}