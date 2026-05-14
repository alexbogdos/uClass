package the.fellowship.uclass.courses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import the.fellowship.eclass.dtos.Course;
import the.fellowship.uclass.R;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.ViewHolder> {
    private final List<Course> items;

    public CoursesAdapter(List<Course> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Course item = items.get(position);
        holder.name.setText(String.valueOf(item.getName()));
        holder.subtitle.setText(String.valueOf(item.getLecturer()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView subtitle;
        MaterialCardView view;

        public ViewHolder(@NonNull View view) {
            super(view);
            view = view.findViewById(R.id.item_view);
            this.name = view.findViewById(R.id.title_text);
            this.subtitle = view.findViewById(R.id.subtitle_text);
        }
    }
}
