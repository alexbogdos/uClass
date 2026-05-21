package the.fellowship.uclass.course.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.uclass.R;
import the.fellowship.uclass.UClass;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final List<RecordModel> items;
    private final SelectionListener listener;

    public ChatAdapter(List<RecordModel> items, SelectionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        final RecordModel message = items.get(position);
        return UClass.pocketbase.getAuthStore().getRecord().getId().equals(message.<String>getValue("author")) ? 1 : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType == 1 ? R.layout.item_message_owned : R.layout.item_message, parent, false), viewType == 1);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final RecordModel message = items.get(position);
        if (!holder.owned) holder.author.setText(message.<String>getValue("name"));
        holder.content.setText(message.<String>getValue("content"));

        String file = message.<String>getValue("file", "");
        if (!file.isEmpty()) {
            holder.thumbnail.setVisibility(View.VISIBLE);
            holder.file.setText(file);
            holder.thumbnail.setOnClickListener(v -> listener.select(v, message));
        } else {
            holder.thumbnail.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface SelectionListener {
        void select(View view, RecordModel record);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView author;
        private final TextView content;
        private final LinearLayout thumbnail;
        private final TextView file;
        private final LinearLayout view;
        private final Boolean owned;

        public ViewHolder(@NonNull View view, Boolean owned) {
            super(view);
            this.owned = owned;
            this.view = view.findViewById(R.id.item_view);
            this.author = view.findViewById(R.id.author_text);
            this.content = view.findViewById(R.id.content_text);
            this.thumbnail = view.findViewById(R.id.thumbnail_view);
            this.file = view.findViewById(R.id.file_text);
        }
    }
}
