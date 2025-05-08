package com.magnaye.spendmonitoring.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.magnaye.spendmonitoring.Dao.SpentDao;
import com.magnaye.spendmonitoring.R;
import com.magnaye.spendmonitoring.ViewModel.CategoryChartViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryChartFragment extends Fragment {
    private PieChart pieChart;
    private CategoryChartViewModel viewModel;
    Date start;
    Date end;

    public CategoryChartFragment(Date start, Date end) {
        this.start = start;
        this.end = end;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_chart, container, false);
        pieChart = view.findViewById(R.id.pieChart);
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(CategoryChartViewModel.class);
        setupChart();
        loadCategoryData(start, end);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(CategoryChartViewModel.class);

        observeCategoryData(start,end);
    }

    private void loadCategoryData(Date startDate, Date endDate) {
        viewModel.getCategoryTotals(startDate, endDate)
                .observe(getViewLifecycleOwner(), categoryTotals -> {
                    if (categoryTotals != null && !categoryTotals.isEmpty()) {
                        updateChart(categoryTotals);
                    } else {
                        pieChart.clear();
                        pieChart.setNoDataText("No data for selected period");
                    }
                });
    }
    private void setupChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
    }

    private void observeCategoryData(Date start, Date end) {
        viewModel.getCategoryTotals(start,end).observe(getViewLifecycleOwner(), categoryTotals -> {
            if (categoryTotals != null && !categoryTotals.isEmpty()) {
                updateChart(categoryTotals);
            }
        });
    }

    private void updateChart(List<SpentDao.CategoryTotal> categoryTotals) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // Prepare chart data
        for (SpentDao.CategoryTotal categoryTotal : categoryTotals) {
            entries.add(new PieEntry((float) categoryTotal.total, categoryTotal.category));
        }

        // Create data set
        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Add colors
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);


        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        // Apply data
        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        pieChart.setData(data);

        // Refresh chart
        pieChart.invalidate();
        pieChart.animateY(1000, Easing.EaseInOutQuad);
    }
}
