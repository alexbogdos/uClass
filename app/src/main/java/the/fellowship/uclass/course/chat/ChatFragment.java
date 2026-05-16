package the.fellowship.uclass.course.chat;

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

import the.fellowship.eclass.dtos.Course;
import the.fellowship.eclass.dtos.Lecturer;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment implements ChatAdapter.SelectionListener {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";

    private List<RecordModel> items;
    private RecyclerView recycler;
    private ChatAdapter adapter;
    private FragmentChatBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentChatBinding.inflate(inflater, container, false);

        String id = getArguments().getString(EXTRA_COURSE_ID);
        Course course = UClass.eclass.getCourse(id);

        binding.titleText.setText(course.getTitle());

        items = new ArrayList<>();  // TODO: Receive from PocketBase
        adapter = new ChatAdapter(items, this);
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
    public void select(RecordModel message) {
        Log.d("Chat", String.valueOf(message));
    }
}