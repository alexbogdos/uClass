package the.fellowship.uclass.course.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.uclass.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final List<RecordModel> items;
    private final SelectionListener listener;

    public ChatAdapter(List<RecordModel> items, SelectionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final RecordModel message = items.get(position);
        holder.author.setText(message.getValue("author"));
        holder.content.setText(message.getValue("content"));
        holder.view.setOnClickListener(view -> listener.select(message));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface SelectionListener {
        void select(RecordModel message);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView author;
        private final TextView content;
        MaterialCardView view;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.author = view.findViewById(R.id.author_text);
            this.content = view.findViewById(R.id.content_text);
        }
    }
}
