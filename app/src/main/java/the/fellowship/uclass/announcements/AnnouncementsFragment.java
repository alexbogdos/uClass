package the.fellowship.uclass.announcements;

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

import the.fellowship.eclass.dtos.Announcement;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentCoursesBinding;

public class AnnouncementsFragment extends Fragment {

    private List<Announcement> items;
    private RecyclerView recycler;
    private AnnouncementsAdapter adapter;
    private FragmentCoursesBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCoursesBinding.inflate(inflater, container, false);
        recycler = binding.getRoot().findViewById(R.id.recycler);

        items = new ArrayList<>();
        adapter = new AnnouncementsAdapter(items);
        recycler.setAdapter(adapter);

        UClass.eclass.getAnnouncements().observe(getViewLifecycleOwner(), this::populateAnnouncements);

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

    public void navigateToAnnouncement(String courseId) {
        Log.d("CoursesFragment", String.format("Navigate to: %s\n", courseId));
    }

    public void populateAnnouncements(List<Announcement> announcements) {
        if (getActivity() == null) {
            Log.e("AnnouncementsFragment", "Can not use UI Thread");
            return;
        }

        getActivity().runOnUiThread(() -> {
            items.clear();
            items.addAll(announcements);
            adapter.notifyDataSetChanged();
        });
    }
}