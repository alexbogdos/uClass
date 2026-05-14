package the.fellowship.uclass.courses;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentCoursesBinding;

public class CoursesFragment extends Fragment {

    private List<Map<String, ?>> items;
    private RecyclerView recycler;
    private CoursesAdapter adapter;
    private FragmentCoursesBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCoursesBinding.inflate(inflater, container, false);
        recycler = binding.getRoot().findViewById(R.id.courses_recycler);

        items = new ArrayList<>();
        adapter = new CoursesAdapter(items);
        recycler.setAdapter(adapter);

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

    public void navigateToCourse(String courseId) {
        Log.d("CoursesFragment", String.format("Navigate to: %s\n", courseId));
    }

    public void populateCourses(List<Map<String, ?>> courses) {
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
}