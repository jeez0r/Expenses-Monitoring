package com.magnaye.spendmonitoring.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.magnaye.spendmonitoring.Model.Spent;
import com.magnaye.spendmonitoring.R;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SpentAdapter extends RecyclerView.Adapter<SpentAdapter.SpentViewHolder> {
     static List<Spent> spentList;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private static OnSpentActionListener listener;
    public SpentAdapter(List<Spent> spentList) {
        this.spentList = spentList;
    }

    public SpentAdapter() {
    }

    public interface OnSpentActionListener {
        void onEditSpent(Spent spent, int position);
        void onDeleteSpent(Spent spent, int position);
    }

    public void setOnSpentActionListener(OnSpentActionListener listener) {
        this.listener = listener;
    }

    public Spent getSpentAt(int position) {
        if (position >= 0 && position < spentList.size()) {
            return spentList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public SpentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.spend_item, parent, false);
        return new SpentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpentViewHolder holder, int position) {
        Spent spent = spentList.get(position);
        holder.amountTextView.setText(String.format("₱%.2f", spent.getAmount()));
        holder.categoryTextView.setText(spent.getCategory());
        holder.dateTextView.setText(dateFormat.format(spent.getDate()));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditSpent(spent, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteSpent(spent, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return spentList.size();
    }

    public void updateData(List<Spent> newSpentList) {
        spentList = newSpentList;
        notifyDataSetChanged();
    }

    static class SpentViewHolder extends RecyclerView.ViewHolder {
        TextView amountTextView, categoryTextView, dateTextView;
        ImageButton btnEdit, btnDelete;
        public SpentViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);


            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Spent spent = spentList.get(position);
                    listener.onEditSpent(spent, position);
                    return true; // Consume the event
                }
                return false;
            });
        }
    }

    // In SpentAdapter
    public void removeSpent(Spent spent) {
        int position = spentList.indexOf(spent);
        if (position != -1) {
            spentList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void restoreSpent(Spent spent) {
        // Add the item back to the original position (or at the end)
        spentList.add(spent);
        // Sort the list if needed (e.g., by date)
        Collections.sort(spentList, (s1, s2) -> s2.getDate().compareTo(s1.getDate()));
        // Notify adapter
//        notifyDataSetChanged();

        // OR if you want to add it at a specific position:
         int position = findInsertPosition(spent);
         spentList.add(position, spent);
         notifyItemInserted(position);
    }

    private int findInsertPosition(Spent newSpent) {
        for (int i = 0; i < spentList.size(); i++) {
            if (newSpent.getDate().compareTo(spentList.get(i).getDate()) > 0) {
                return i;
            }
        }
        return spentList.size();
    }
}
