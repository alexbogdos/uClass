package the.fellowship.uclass.course.chat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import the.fellowship.pocketbase.dtos.RecordSubscriptionEvent;
import the.fellowship.uclass.MainActivity;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";

    private List<RecordModel> items;
    private Course course;
    private String filter;
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
        filter = String.format("course = '%s'", id);

        binding.titleText.setText(course.getTitle());

        items = new ArrayList<>();
        adapter = new ChatAdapter(items);
        binding.recycler.setAdapter(adapter);

        fetchMessagesAsync();
        subscribeToTopicAsync();

        binding.addButton.setOnClickListener(this::pickAttachments);
        binding.sendButton.setOnClickListener(this::sendMessage);

        binding.messageEdit.setOnFocusChangeListener((View v, boolean hasFocus) -> {if (hasFocus) scrollToPosition(items.size() - 1);});
        binding.messageEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && binding.sendButton.getVisibility() == View.GONE) {
                    binding.sendButton.setVisibility(View.VISIBLE);
                    binding.addButton.setVisibility(View.GONE);
                } else if (s.length() == 0 && binding.sendButton.getVisibility() == View.VISIBLE) {
                    binding.sendButton.setVisibility(View.GONE);
                    binding.addButton.setVisibility(View.VISIBLE);
                }
            }
        });

        return binding.getRoot();
    }

    private void fetchMessagesAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                final List<RecordModel> messages = UClass.pocketbase.getCollection("chat").getFullList(filter);
                Log.d("Chat", String.format("Messages: %s", messages));
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        items.clear();
                        items.addAll(messages);
                        adapter.notifyDataSetChanged();
                        scrollToPosition(items.size() - 1);
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

    private void subscribeToTopicAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                UClass.pocketbase.getCollection("chat").subscribe("*", filter, this::receiveMessage);
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
        binding.messageEdit.clearFocus();

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
            } catch (ClientException err) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Snackbar.make(view, String.format("Failed connecting to PocketBase: \n%s", err), Snackbar.LENGTH_LONG).setTextMaxLines(16).setAction("Action", null).show());
                }
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });
    }

    private void receiveMessage(RecordSubscriptionEvent event) {
        // Check that the message is destined for the currently viewed course
        if (!course.getId().equals(event.getRecord().<String>getValue("course"))) return;
        Log.d("Chat", String.format("Received: %s", event));

        String id = event.getRecord().getId();
        String action = event.getAction().toUpperCase();

        if (getActivity() == null) {
            Log.e("Chat", "Can not use UI Thread");
            return;
        }

        getActivity().runOnUiThread(() -> {
            int index = -1;
            // Find item index for UPDATE & DELETE
            if (!"CREATE".equals(action)) {
                for (int i = 0; i < items.size(); i++) {
                    if (id.equals(items.get(i).getId())) {
                        index = i;
                        break;
                    }
                }
            }

            switch (action) {
                case "CREATE":
                    items.add(event.getRecord());
                    adapter.notifyItemInserted(items.size() - 1);
                    scrollToPosition(items.size() - 1);
                    break;
                case "UPDATE":
                    if (index >= 0) {
                        items.set(index, event.getRecord());
                        adapter.notifyItemChanged(index);
                    }
                    break;
                case "DELETE":
                    // FIXME: Deleting whole chat crashes app
                    if (index >= 0) {
                        items.remove(index);
                        adapter.notifyItemRemoved(index);
                        scrollToPosition(items.size() - 1);
                    }
                    break;
            }
        });
    }

    private void pickAttachments(View view) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Snackbar.make(binding.getRoot(), "Pick attachments", Snackbar.LENGTH_LONG).setTextMaxLines(16).setAction("Action", null).show());
        }
        Log.d("Chat", "Pick attachments");
    }

    private void scrollToPosition(int position) {
        if (position < 0) return;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.recycler.smoothScrollToPosition(items.size() - 1);
        }, 150);
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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        CompletableFuture.runAsync(() -> {
            try {
                UClass.pocketbase.getCollection("chat").unsubscribe("*");
                Log.d("Chat", String.format("Unsubscribed from \"%s\"", course.getTitle()));
            } catch (ClientException err) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Snackbar.make(binding.getRoot(), String.format("Failed connecting to PocketBase: \n%s", err), Snackbar.LENGTH_LONG).setTextMaxLines(16).setAction("Action", null).show());
                }
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });

        binding = null;
    }
}