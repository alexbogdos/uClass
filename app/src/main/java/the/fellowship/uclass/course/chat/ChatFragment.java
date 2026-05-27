package the.fellowship.uclass.course.chat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import the.fellowship.eclass.dtos.Course;
import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.dtos.MultipartFile;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.pocketbase.dtos.RecordSubscriptionEvent;
import the.fellowship.uclass.MainActivity;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment implements ChatAdapter.SelectionListener {
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";
    private static final int PICK_FILE = 1;
    private static final int CAPTURE_IMAGE = 2;

    private List<RecordModel> items;
    private Course course;
    private FileDescriptor attachmentDescriptor;
    private String attachmentName;
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
        adapter = new ChatAdapter(items, this);
        binding.recycler.setAdapter(adapter);

        fetchMessagesAsync();
        subscribeToTopicAsync();

        binding.addButton.setOnClickListener(l -> pickIntent());
        binding.sendButton.setOnClickListener(l -> sendMessage());

        // On messageEdit focused, since the keyboard appears and hides last messages, scroll to last message
        binding.messageEdit.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (hasFocus) scrollToPosition(items.size() - 1);
        });

        // When starting to write, switch the "Attach Button" with the "Send Button"
        binding.messageEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

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
                        adapter.notifyItemRangeInserted(0, this.items.size());
                        binding.recycler.scrollToPosition(items.size() - 1);
                    });
                } else {
                    Log.e("Chat", "Cannot use UI Thread");
                }
            } catch (ClientException err) {
                UClass.pocketbase.postNotification("Αποτυχία σύνδεσης με το διακομιστή");
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });
    }

    private void subscribeToTopicAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                UClass.pocketbase.getCollection("chat").subscribe("*", filter, this::receiveMessage);
            } catch (ClientException err) {
                UClass.pocketbase.postNotification("Αποτυχία σύνδεσης με το διακομιστή");
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });
    }

    private void sendMessage() {
        String content = binding.messageEdit.getText().toString();
        FileDescriptor fileDescriptor = attachmentDescriptor;
        String fileName = attachmentName;

        binding.messageEdit.setText("");
        binding.messageEdit.clearFocus();

        binding.attachmentText.setVisibility(View.GONE);
        attachmentDescriptor = null;
        attachmentName = null;

        CompletableFuture.runAsync(() -> {
            try {
                RecordModel auth = UClass.pocketbase.getAuthStore().getRecord();
                final Map<String, ?> body = Map.of(
                        "author", auth.getId(),
                        "course", course.getId(),
                        "name", auth.<String>getValue("name"),
                        "content", content.strip()
                );

                List<MultipartFile> files = null;
                if (fileDescriptor != null && fileName != null) {
                    files = List.of(new MultipartFile("file", fileName, fileDescriptor));
                }
                final RecordModel message = UClass.pocketbase.getCollection("chat").create(body, files);
            } catch (ClientException err) {
                UClass.pocketbase.postNotification("Αποτυχία σύνδεσης με το διακομιστή");
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
            Log.e("Chat", "Cannot use UI Thread");
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
                    binding.recycler.smoothScrollToPosition(items.size() - 1);
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

    /**
     *  Choose between selecting a file or capturing an image
     */
    private void pickIntent() {
        // TODO: Pick between selection or image
        pickAttachment();
    }

    /**
     * https://developer.android.com/training/data-storage/shared/documents-files
     */
    private void pickAttachment() {
        attachmentDescriptor = null;
        attachmentName = null;
        binding.attachmentText.setVisibility(View.GONE);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, PICK_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == PICK_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (resultData != null && resultData.getData() != null) {
                Uri uri = resultData.getData();
                // Perform operations on the document using its URI.
                Log.d("Chat", String.format("Selected: %s", uri));
                try (Cursor cursor = binding.getRoot().getContext().getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                    // Get file's name
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            attachmentName = cursor.getString(nameIndex);
                        }
                    }

                    // Get file's descriptor
                    ParcelFileDescriptor parcelDescriptor = binding.getRoot().getContext().getContentResolver().openFileDescriptor(uri, "r");
                    attachmentDescriptor = parcelDescriptor.getFileDescriptor();

                    // Update UI to show selected attachment
                    binding.attachmentText.setText(attachmentName);
                    binding.attachmentText.setVisibility(View.VISIBLE);
                } catch (NullPointerException | FileNotFoundException e) {
                    UClass.pocketbase.postNotification("Αποτυχία προσθήκης συννημένου");
                    Log.e("Chat", String.format("Failed reading attachment: \n%s", binding.getRoot()));
                }
            }
        }
    }

    private void scrollToPosition(int position) {
        if (position < 0) return;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.recycler.smoothScrollToPosition(items.size() - 1);
        }, 150);
    }

    /**
     * Use MediaStore.Downloads to create an empty file with the received file's name
     * and write the response's InputBuffer to it.
     * <br>
     * https://developer.android.com/reference/android/provider/MediaStore.Downloads
     */
    @Override
    public void downloadAttachment(View view, RecordModel record) {
        CompletableFuture.runAsync(() -> {
            try {
                final String name = record.<String>getValue("file");
                // Generate short-lived access token & build file's url
                final String token = UClass.pocketbase.getFiles().getToken();
                final HttpUrl url = UClass.pocketbase.getFiles().getURL(record, name, token);
                Log.d("Chat", String.format("Downloading: %s", name));

                // Create new request to download file
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = new OkHttpClient().newCall(request).execute()) {
                    // Initialize file in Downloads to save to
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, name);
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                    values.put(MediaStore.Downloads.IS_PENDING, 1);
                    Uri uri = view.getContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) throw new IOException("Failed to create MediaStore entry");

                    // Read response's InputBuffer
                    try (OutputStream outputStream = view.getContext().getContentResolver().openOutputStream(uri)) {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = response.body().byteStream().read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }

                        // Notify user upon download completion
                        UClass.pocketbase.postNotification(String.format("Επιτυχής λήψη συννημένου: %s", name));
                        Log.d("Chat", String.format("Downloaded: %s", name));
                    } finally {
                        // Finalize initialized file
                        values.clear();
                        values.put(MediaStore.Downloads.IS_PENDING, 0);
                        view.getContext().getContentResolver().update(uri, values, null, null);
                    }
                } catch (IOException e) {
                    throw new ClientException(url, e);
                }
            } catch (ClientException err) {
                UClass.pocketbase.postNotification("Αποτυχία λήψης αρχείου");
                Log.e("Chat", String.format("Failed downloading file: \n%s", err));
            }
        });
    }

    /**
     * On screen enter: Hide bottom navigation bar to create space for the message edit
     */
    @Override
    public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            activity.findViewById(R.id.bottom_navigation).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * On screen exit: Show bottom navigation bar
     */
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

    /**
     * Unsubscribe from SSE (Server Sent Events)
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        CompletableFuture.runAsync(() -> {
            try {
                UClass.pocketbase.getCollection("chat").unsubscribe("*");
                Log.d("Chat", String.format("Unsubscribed from \"%s\"", course.getTitle()));
            } catch (ClientException err) {
                UClass.pocketbase.postNotification("Αποτυχία σύνδεσης με το διακομιστή");
                Log.e("Chat", String.format("Failed connecting to PocketBase: \n%s", err));
            }
        });

        binding = null;
    }
}