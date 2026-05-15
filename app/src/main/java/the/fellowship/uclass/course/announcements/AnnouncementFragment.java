package the.fellowship.uclass.course.announcements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import the.fellowship.eclass.dtos.Announcement;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentAnnouncementBinding;

public class AnnouncementFragment extends Fragment {
    public static final String EXTRA_ANNOUNCEMENT_ID = "EXTRA_ANNOUNCEMENT_ID";

    private FragmentAnnouncementBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAnnouncementBinding.inflate(inflater, container, false);

        String id = getArguments().getString(EXTRA_ANNOUNCEMENT_ID);
        Announcement announcement = UClass.eclass.getAnnouncement(id);

        binding.titleText.setText(announcement.getTitle());
        binding.bodyText.setText(announcement.getBody());

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