package the.fellowship.uclass.course.details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.uclass.R;

public class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.ViewHolder> {
    private final List<RecordModel> items;

    public RatingsAdapter(List<RecordModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rating, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final RecordModel item = items.get(position);
        holder.author.setText(item.<String>getValue("name"));
        holder.rating.setRating(item.<Long>getValue("rating"));

        String comment = item.getValue("comment");
        holder.comment.setVisibility(comment.isEmpty() ? View.GONE : View.VISIBLE);
        if (!comment.isEmpty()) {
            holder.comment.setText(comment);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView author;
        private final RatingBar rating;
        private final TextView comment;
        MaterialCardView view;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.view = view.findViewById(R.id.item_view);
            this.author = view.findViewById(R.id.author_text);
            this.rating = view.findViewById(R.id.rating_bar);
            this.comment = view.findViewById(R.id.comment_text);
        }
    }
}
