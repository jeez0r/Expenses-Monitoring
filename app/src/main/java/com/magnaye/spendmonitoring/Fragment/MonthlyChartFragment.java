package com.magnaye.spendmonitoring.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.magnaye.spendmonitoring.Dao.SpentDao;
import com.magnaye.spendmonitoring.R;
import com.magnaye.spendmonitoring.ViewModel.MonthlyChartViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonthlyChartFragment extends Fragment {
    private BarChart barChart;
    private MonthlyChartViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.monthly_chart_layout, container, false);
        barChart = view.findViewById(R.id.barChart);
        setupChart();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MonthlyChartViewModel.class);
        observeMonthlyData();
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(true);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setTextColor(getContext().getColor(R.color.orange));


        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(getContext().getColor(R.color.orange));

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(1500);

    }

    private void observeMonthlyData() {
        viewModel.getMonthlyTotals().observe(getViewLifecycleOwner(), monthlyTotals -> {
            if (monthlyTotals != null && !monthlyTotals.isEmpty()) {
                updateChart(monthlyTotals);
            } else {
                barChart.clear();
                barChart.setNoDataText("No monthly data available");
            }
        });
    }

    private void updateChart(List<SpentDao.MonthlyTotal> monthlyTotals) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < monthlyTotals.size(); i++) {
            SpentDao.MonthlyTotal total = monthlyTotals.get(i);
            entries.add(new BarEntry(i, (float) total.total));

            // Format month label (e.g., "Jan 2023")
            String[] parts = total.month.split("-");
            String monthName = new SimpleDateFormat("MMM", Locale.getDefault())
                    .format(new Date(0, Integer.parseInt(parts[1])-1, 1));
            labels.add(monthName + " " + parts[0]);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Expenses");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(getContext().getColor(R.color.orange));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // Set custom bar width

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.setData(barData);
        barChart.setFitBars(true); // Make all bars visible
        barChart.invalidate(); // Refresh chart
    }
}
