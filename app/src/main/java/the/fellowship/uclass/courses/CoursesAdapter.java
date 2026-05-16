package the.fellowship.uclass.courses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import the.fellowship.eclass.dtos.Course;
import the.fellowship.uclass.R;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.ViewHolder> {
    private final List<Course> items;
    private final SelectionListener listener;

    public CoursesAdapter(List<Course> items, SelectionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Course item = items.get(position);
        holder.name.setText(String.valueOf(item.getTitle()));
        holder.subtitle.setText(String.valueOf(item.getLecturer().getName()));

        holder.chat.setOnClickListener(view -> listener.navigateChat(item));
        holder.announcements.setOnClickListener(view -> listener.navigateAnnouncements(item));

        holder.view.setOnClickListener(view -> listener.select(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface SelectionListener {
        void select(Course course);
        void navigateChat(Course course);
        void navigateAnnouncements(Course course);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView subtitle;
        private final MaterialButton chat;
        private final MaterialButton announcements;
        MaterialCardView view;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.view = view.findViewById(R.id.item_view);
            this.name = view.findViewById(R.id.title_text);
            this.subtitle = view.findViewById(R.id.subtitle_text);
            this.chat = view.findViewById(R.id.button_chat);
            this.announcements = view.findViewById(R.id.button_announcements);
        }
    }
}
