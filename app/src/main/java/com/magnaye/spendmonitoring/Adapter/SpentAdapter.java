package com.magnaye.spendmonitoring.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.magnaye.spendmonitoring.Model.Spent;
import com.magnaye.spendmonitoring.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SpentAdapter extends RecyclerView.Adapter<SpentAdapter.SpentViewHolder> {
    private List<Spent> spentList;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    public SpentAdapter(List<Spent> spentList) {
        this.spentList = spentList;
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

        public SpentViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
