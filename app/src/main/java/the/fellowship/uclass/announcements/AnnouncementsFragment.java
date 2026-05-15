package the.fellowship.uclass.announcements;

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

import java.util.ArrayList;
import java.util.List;

import the.fellowship.eclass.dtos.Announcement;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentAnnouncementsBinding;

public class AnnouncementsFragment extends Fragment implements AnnouncementsAdapter.SelectionListener {

    private List<Announcement> items;
    private RecyclerView recycler;
    private AnnouncementsAdapter adapter;
    private FragmentAnnouncementsBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false);

        items = new ArrayList<>();
        adapter = new AnnouncementsAdapter(items, this);
        binding.recycler.setAdapter(adapter);

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

    @Override
    public void select(Announcement announcement) {
        if (getActivity() == null || getView() == null) {
            Log.e("CoursesFragment", "Can not use UI Thread");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(AnnouncementFragment.EXTRA_ANNOUNCEMENT_ID, announcement.getId());

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.AnnouncementFragment, bundle);
    }
}