package com.magnaye.spendmonitoring.Module;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.magnaye.spendmonitoring.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateRangeDialog extends Dialog {
    private TextView tvStartDate, tvEndDate;
    private Button btnApply;
    private DateRangeListener listener;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    public interface DateRangeListener {
        void onDateRangeSelected(Date startDate, Date endDate);
    }

    public DateRangeDialog(@NonNull Context context, DateRangeListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_date_range);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnApply = findViewById(R.id.btnApplyRange);

        // Set default dates (last 30 days)
        endCalendar.setTime(new Date());
        startCalendar.setTime(new Date());
        startCalendar.add(Calendar.DAY_OF_MONTH, -30);

        updateDateTexts();

        tvStartDate.setOnClickListener(v -> showDatePicker(true));
        tvEndDate.setOnClickListener(v -> showDatePicker(false));

        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                // Set time to beginning of start day and end of end day
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                startCalendar.set(Calendar.MINUTE, 0);
                startCalendar.set(Calendar.SECOND, 0);

                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                endCalendar.set(Calendar.MINUTE, 59);
                endCalendar.set(Calendar.SECOND, 59);

                listener.onDateRangeSelected(startCalendar.getTime(), endCalendar.getTime());
            }
            dismiss();
        });
    }

    private void showDatePicker(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    if (isStartDate && calendar.after(endCalendar)) {
                        // If start date is after end date, adjust end date
                        endCalendar.set(year, month, dayOfMonth);
                    } else if (!isStartDate && calendar.before(startCalendar)) {
                        // If end date is before start date, adjust start date
                        startCalendar.set(year, month, dayOfMonth);
                    }
                    updateDateTexts();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateTexts() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvStartDate.setText(sdf.format(startCalendar.getTime()));
        tvEndDate.setText(sdf.format(endCalendar.getTime()));
    }
}