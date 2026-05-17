package the.fellowship.uclass.course.details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import the.fellowship.eclass.dtos.Assignment;
import the.fellowship.uclass.R;

public class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentsAdapter.ViewHolder> {
    private final List<Assignment> items;

    public AssignmentsAdapter(List<Assignment> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Assignment item = items.get(position);
        holder.name.setText(item.getTitle());
        holder.subtitle.setText(item.getEnd());
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
            this.view = view.findViewById(R.id.item_view);
            this.name = view.findViewById(R.id.title_text);
            this.subtitle = view.findViewById(R.id.subtitle_text);
        }
    }
}
