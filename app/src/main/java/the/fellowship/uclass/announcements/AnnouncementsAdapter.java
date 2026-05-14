package the.fellowship.uclass.announcements;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

import the.fellowship.eclass.dtos.Announcement;
import the.fellowship.uclass.R;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder> {
    private final List<Announcement> items;

    public AnnouncementsAdapter(List<Announcement> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Announcement item = items.get(position);
        holder.name.setText(item.getTitle());
        holder.subtitle.setText(item.getCourse());
        holder.date.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView subtitle;
        private final TextView date;
        MaterialCardView view;

        public ViewHolder(@NonNull View view) {
            super(view);
            view = view.findViewById(R.id.item_view);
            this.name = view.findViewById(R.id.title_text);
            this.subtitle = view.findViewById(R.id.subtitle_text);
            this.date = view.findViewById(R.id.date_text);
        }
    }
}
