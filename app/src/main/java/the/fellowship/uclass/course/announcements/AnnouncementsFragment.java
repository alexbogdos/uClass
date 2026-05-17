package the.fellowship.uclass.course.announcements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import the.fellowship.eclass.dtos.Announcement;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentAnnouncementsBinding;

public class AnnouncementsFragment extends Fragment implements AnnouncementsAdapter.SelectionListener {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";

    private List<Announcement> items;
    private AnnouncementsAdapter adapter;
    private FragmentAnnouncementsBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false);

        String id = getArguments().getString(EXTRA_COURSE_ID);
        items = UClass.eclass.getAnnouncements(id);
        adapter = new AnnouncementsAdapter(items, this);
        binding.recycler.setAdapter(adapter);

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

    @Override
    public void select(Announcement announcement) {
        Bundle bundle = new Bundle();
        bundle.putInt(AnnouncementFragment.EXTRA_ANNOUNCEMENT_ID, announcement.getId());

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.CourseAnnouncementFragment, bundle);
    }
}