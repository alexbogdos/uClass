package the.fellowship.uclass.course.chat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import the.fellowship.eclass.dtos.Course;
import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.uclass.MainActivity;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";

    private List<RecordModel> items;
    private Course course;
    private ChatAdapter adapter;
    private FragmentChatBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentChatBinding.inflate(inflater, container, false);

        String id = getArguments().getString(EXTRA_COURSE_ID);
        course = UClass.eclass.getCourse(id);

        binding.titleText.setText(course.getTitle());

        items = new ArrayList<>();
        adapter = new ChatAdapter(items);
        binding.recycler.setAdapter(adapter);

        fetchMessagesAsync(id);

        binding.sendButton.setOnClickListener(this::sendMessage);

        return binding.getRoot();
    }

    private void fetchMessagesAsync(String id) {
        CompletableFuture.runAsync(() -> {
            try {
                final List<RecordModel> messages = UClass.pocketbase.getCollection("chat").getFullList(String.format("course = '%s'", id));

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        items.clear();
                        items.addAll(messages);
                        adapter.notifyDataSetChanged();
                    });
                } else {
                    Log.e("Chat", "Can not use UI Thread");
                }
            } catch (ClientException err) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Snackbar.make(binding.getRoot(), String.format("Failed connecting to PocketBase: \n%s", err), Snackbar.LENGTH_LONG).setTextMaxLines(16).setAction("Action", null).show());
                }
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });
    }

    private void sendMessage(View view) {
        String content = binding.messageEdit.getText().toString();
        binding.messageEdit.setText("");

        CompletableFuture.runAsync(() -> {
            try {
                RecordModel auth = UClass.pocketbase.getAuthStore().getRecord();
                final Map<String, ?> body = Map.of(
                        "author", auth.getId(),
                        "course", course.getId(),
                        "name", auth.<String>getValue("name"),
                        "content", content.strip()
                );

                final RecordModel message = UClass.pocketbase.getCollection("chat").create(body, null);
                Log.d("Chat", String.format("Sent: \n%s", message));
            } catch (ClientException err) {
                getActivity().runOnUiThread(() -> Snackbar.make(view, String.format("Failed connecting to PocketBase: \n%s", err), Snackbar.LENGTH_LONG).setTextMaxLines(16).setAction("Action", null).show());
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            activity.findViewById(R.id.bottom_navigation).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            activity.findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}