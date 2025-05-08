package com.magnaye.spendmonitoring.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.magnaye.spendmonitoring.Model.CalendarDate;
import com.magnaye.spendmonitoring.R;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private Context context;
    private List<CalendarDate> dates;
    private OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(int position);
    }

    public DateAdapter(Context context, List<CalendarDate> dates, OnDateClickListener listener) {
        this.context = context;
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        CalendarDate date = dates.get(position);
        holder.dayName.setText(date.getDayName());
        holder.dayNumber.setText(String.valueOf(date.getDayNumber()));

        // Change appearance based on state
        int bgColor, textColor;
        if (date.isSelected()) {
            bgColor = context.getResources().getColor(R.color.button_selected);
            textColor = context.getResources().getColor(R.color.selected_date_text);
        } else if (date.isFuture()) {
            bgColor = context.getResources().getColor(R.color.future_date_bg);
            textColor = context.getResources().getColor(R.color.future_date_text);
        } else {
            bgColor = context.getResources().getColor(R.color.default_date_bg);
            textColor = context.getResources().getColor(R.color.default_date_text);
        }

        holder.cardView.setCardBackgroundColor(bgColor);
        holder.dayName.setTextColor(textColor);
        holder.dayNumber.setTextColor(textColor);

        if (date.hasData()) {
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!date.isFuture()) {
                listener.onDateClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView dayName;
        TextView dayNumber;
        View indicator;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            dayName = itemView.findViewById(R.id.tvDayName);
            indicator = itemView.findViewById(R.id.indicator);
            dayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}