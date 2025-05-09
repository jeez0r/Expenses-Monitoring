package com.magnaye.spendmonitoring.Activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.magnaye.spendmonitoring.Adapter.DateAdapter;
import com.magnaye.spendmonitoring.Adapter.SpentAdapter;
import com.magnaye.spendmonitoring.DatabaseHelper.DatabaseClient;
import com.magnaye.spendmonitoring.Fragment.CategoryChartFragment;
import com.magnaye.spendmonitoring.Fragment.MonthlyChartFragment;
import com.magnaye.spendmonitoring.Model.CalendarDate;
import com.magnaye.spendmonitoring.Model.Spent;
import com.magnaye.spendmonitoring.Module.DateRangeDialog;
import com.magnaye.spendmonitoring.R;
import com.magnaye.spendmonitoring.Module.AddSpentWidgetProvider;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DateAdapter.OnDateClickListener {
    private RecyclerView rvDates;
    private DateAdapter dateAdapter;
    private List<CalendarDate> dates = new ArrayList<>();
    private ImageButton btnPrevious, btnNext;
    private LocalDate currentStartDate;
    private LocalDate today = LocalDate.now();
    private DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
    private RecyclerView recyclerView;
    private SpentAdapter adapter;
    private List<Spent> spentList = new ArrayList<>();
    private LocalDate selectedDate;

    private RadioGroup rg_rgInterval;

    String interval = "daily";
    TextView tvTotalSpent,toolbarTitle,toolbarTitle1;
    FrameLayout container, container1;

    private TextView tvBudgetAmount, tvBudgetRemaining;
    private LinearProgressIndicator progressBudget;
    private ImageButton btnSetBudget;
    private SharedPreferences sharedPreferences;
    // Add these constants at the top of your MainActivity class
    private static final String BUDGET_PREF = "monthly_budget";
    private static final String BUDGET_AMOUNT_KEY = "budget_amount";
    private static final String BUDGET_PREFIX = "budget_"; // Prefix for monthly budget keys

    Toolbar toolbar;

    // In your MainActivity.java
    // In your MainActivity.java
    public static void updateWidget(Context context) {
        Intent intent = new Intent(context, AddSpentWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, AddSpentWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedDate = LocalDate.now();
        FloatingActionButton fabAddSpend = findViewById(R.id.fabAddSpend);
        container = findViewById(R.id.container);
        container1 = findViewById(R.id.container1);
        toolbar = findViewById(R.id.toolbarGlobal);
        toolbarTitle1 = findViewById(R.id.toolbarTitle1);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setTitle("");


        if (getIntent() != null && getIntent().getAction() != null &&
                getIntent().getAction().equals("SHOW_ADD_SPENT_DIALOG")) {
            showAddSpentDialog(LocalDate.now());
        }

        tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
        tvBudgetRemaining = findViewById(R.id.tvBudgetRemaining);
        progressBudget = findViewById(R.id.progressBudget);

        sharedPreferences = getSharedPreferences(BUDGET_PREF, MODE_PRIVATE);


        // Initialize budget display
        updateBudgetDisplay();


        fabAddSpend.setOnClickListener(view -> {
            if (selectedDate == null) {
                // If no date selected, use today's date
                selectedDate = LocalDate.now();
            }
            showAddSpentDialog(selectedDate);
        });


        rg_rgInterval = findViewById(R.id.rg_rgInterval);
        rvDates = findViewById(R.id.rvDates);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        recyclerView = findViewById(R.id.rvSpend);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SpentAdapter(spentList);
        recyclerView.setAdapter(adapter);

        rg_rgInterval.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_custome) {
                showDateRangeDialog();
                interval = "custom";
            } else if (checkedId == R.id.rb_daily) {
                interval = "daily";
            } else if (checkedId == R.id.rb_weekly) {
                interval = "weekly";
            } else if (checkedId == R.id.rb_monthly) {
                interval = "monthly";
            }

            checkInterval();

        });


        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialize with the current week (starting from Monday)
        currentStartDate = LocalDate.now().with(DayOfWeek.MONDAY);
        loadWeekDates(currentStartDate);
        updateMonthYearHeader();
        updateButtonStates();

        btnPrevious.setOnClickListener(v -> {
            currentStartDate = currentStartDate.minusDays(7);
            loadWeekDates(currentStartDate);
            updateMonthYearHeader();
            updateButtonStates();

            // Check if we moved to a different month
            if (currentStartDate.getMonthValue() != selectedDate.getMonthValue() ||
                    currentStartDate.getYear() != selectedDate.getYear()) {
                selectedDate = currentStartDate;
                updateBudgetDisplay();

            }
        });

        btnNext.setOnClickListener(v -> {
            currentStartDate = currentStartDate.plusDays(7);
            loadWeekDates(currentStartDate);
            updateMonthYearHeader();
            updateButtonStates();

            // Check if we moved to a different month
            if (currentStartDate.getMonthValue() != selectedDate.getMonthValue() ||
                    currentStartDate.getYear() != selectedDate.getYear()) {
                selectedDate = currentStartDate;
                updateBudgetDisplay();

            }
        });


        // Load data from database
        checkInterval();
    }


    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Monthly Budget");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount");

        // Get current budget if exists
        String currentMonthKey = getMonthBudgetKey(selectedDate);
        float currentBudget = sharedPreferences.getFloat(currentMonthKey, 0);
        if (currentBudget > 0) {
            input.setText(String.format(Locale.getDefault(), "%.2f", currentBudget));
        }

        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                float budget = Float.parseFloat(input.getText().toString());
                if (budget > 0) {
                    // Save for current month
                    String monthKey = getMonthBudgetKey(selectedDate);
                    sharedPreferences.edit().putFloat(monthKey, budget).apply();

                    updateBudgetDisplay();
                    Toast.makeText(this, "Budget set successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void checkPreviousMonthBudgetStatus(LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        Date startDate = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(lastDayOfMonth.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        String monthKey = getMonthBudgetKey(date);
        float budgetAmount = sharedPreferences.getFloat(monthKey, 0);

        if (budgetAmount <= 0) {
            // No budget set for this month
            return;
        }

        DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase()
                .spentDao()
                .getTotalSpentByDateRange(startDate, endDate)
                .observe(this, totalSpent -> {
                    if (totalSpent == null) totalSpent = 0.0;

                    float remaining = (float) (budgetAmount - totalSpent);
                    String monthYear = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

                    if (remaining >= 0) {
                      /*  Toast.makeText(this,
                                monthYear + ": Budget remaining ₱" + String.format(Locale.getDefault(), "%.2f", remaining),
                                Toast.LENGTH_SHORT).show();*/
                    } else {
                        Toast.makeText(this,
                                monthYear + ": Budget exceeded by ₱" + String.format(Locale.getDefault(), "%.2f", Math.abs(remaining)),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to create a key for monthly budget storage
    private String getMonthBudgetKey(LocalDate date) {
        return BUDGET_PREFIX + date.getYear() + "_" + date.getMonthValue();
    }

    private void updateBudgetDisplay() {
        LocalDate displayDate = selectedDate != null ? selectedDate : LocalDate.now();
        String monthKey = getMonthBudgetKey(displayDate);
        float budgetAmount = sharedPreferences.getFloat(monthKey, 0);

        if (budgetAmount <= 0) {
            tvBudgetAmount.setText("Not set");
            tvBudgetRemaining.setText("Not set");
            progressBudget.setProgress(0);
            return;
        }

        // Get the month's start and end dates for the selected date
        LocalDate firstDayOfMonth = displayDate.withDayOfMonth(1);
        LocalDate lastDayOfMonth = displayDate.withDayOfMonth(displayDate.lengthOfMonth());

        Date startDate = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(lastDayOfMonth.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        // Get total spent for the selected month
        DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase()
                .spentDao()
                .getTotalSpentByDateRange(endDate, startDate)
                .observe(this, totalSpent -> {
                    if (totalSpent == null) totalSpent = 0.0;

                    float remaining = (float) (budgetAmount - totalSpent);
                    int progress = (int) ((totalSpent / budgetAmount) * 100);

                    // Update UI
                    String monthYear = displayDate.format(DateTimeFormatter.ofPattern("MMM yyyy"));
                    tvBudgetRemaining.setText(String.format(Locale.getDefault(), "%s: ₱%.2f" , monthYear, remaining));
                    tvBudgetAmount.setText(String.format(Locale.getDefault(), "₱%.2f", Math.max(0, budgetAmount)));

                    // Set progress (cap at 100% if overspent)
                    progressBudget.setProgress(Math.min(progress, 100));

                    // Change color if overspent
                    if (remaining < 0) {
                        progressBudget.setIndicatorColor(ContextCompat.getColor(this, R.color.textspend));
                        tvBudgetRemaining.setTextColor(ContextCompat.getColor(this, R.color.textspend));
                        tvBudgetRemaining.setText(String.format(Locale.getDefault(), "%s -₱%.2f", monthYear, Math.abs(remaining)));
                    } else {
                        progressBudget.setIndicatorColor(ContextCompat.getColor(this, R.color.button_selected));
                        tvBudgetRemaining.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                    }

                    // Show toast if viewing a past month
                    if (displayDate.getMonthValue() != LocalDate.now().getMonthValue() ||
                            displayDate.getYear() != LocalDate.now().getYear()) {
                        String status = remaining >= 0 ?
                                String.format(Locale.getDefault(), "Budget remaining: ₱%.2f", remaining) :
                                String.format(Locale.getDefault(), "Budget exceeded by: ₱%.2f", Math.abs(remaining));

                       /* Toast.makeText(this,
                                displayDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")) + ": " + status,
                                Toast.LENGTH_SHORT).show();*/
                    }
                });
    }

    private void showDateRangeDialog() {
        DateRangeDialog dialog = new DateRangeDialog(this, new DateRangeDialog.DateRangeListener() {
            @Override
            public void onDateRangeSelected(Date startDate, Date endDate) {
                filterByDateRange(endDate, startDate);
                filterByDateRangeSpent(endDate, startDate);
            }
        });
        dialog.show();
    }

    private void filterByDateRange(Date startDate, Date endDate) {
        new Thread(() -> {
            List<Spent> filteredList = DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .spentDao()
                    .getSpentByDateRange(startDate, endDate);

            runOnUiThread(() -> {
                adapter.updateData(filteredList);
                updateFilterStatus(startDate, endDate);
            });
        }).start();
    }

    private void filterByDateRangeSpent(Date startDate, Date endDate) {
        DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase()
                .spentDao()
                .getTotalSpentByDateRange(startDate, endDate)
                .observe(this, total -> {
                    if (total != null) {
                        String formattedAmount = String.format(Locale.getDefault(), "₱%.2f", total);
                        toolbarTitle.setText(formattedAmount);
                    } else {
                        toolbarTitle.setText(String.format(Locale.getDefault(), "₱%.2f", 0.0));
                    }

                });
    }

    private List<String> getCategoriesFromDatabase() {
        return Arrays.asList(
                "Food",
                "Groceries",
                "Transport",
                "Fuel",
                "Parking",
                "Car Maintenance",
                "Housing",
                "Rent",
                "Utilities",
                "Electricity",
                "Water",
                "Formula milk",
                "Pampers",
                "Gives",
                "Internet",
                "Mobile Phone",
                "Entertainment",
                "Shopping",
                "Health",
                "Insurance",
                "Education",
                "Tuition",
                "Travel",
                "Flights",
                "Vacation",
                "Personal Care",
                "Haircut",
                "Cosmetics",
                "Kids",
                "Pets",
                "Investments",
                "Savings",
                "Taxes",
                "Hobbies",
                "Sports",
                "Alcohol",
                "Tobacco"
        );
    }

    private void updateFilterStatus(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        SimpleDateFormat sdfe = new SimpleDateFormat("MMM d ", Locale.getDefault());
        String rangeText = String.format("From %s to %s",
                sdfe.format(endDate), sdf.format(startDate));
        toolbarTitle1.setText(rangeText);
    }

    private void loadWeekDates(LocalDate startDate) {
        dates.clear();

        // First create all dates without data
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            boolean isSelected = date.equals(today);
            boolean isFuture = date.isAfter(today);

            dates.add(new CalendarDate(dayName, date.getDayOfMonth(), isSelected, isFuture, date, false));
        }

        // Then check database for each date
        checkDatesForData();

        dateAdapter = new DateAdapter(this, dates, this);
        rvDates.setAdapter(dateAdapter);
    }

    private void checkDatesForData() {
        for (CalendarDate calendarDate : dates) {
            LocalDate date = calendarDate.getDate();
            Date startOfDay = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endOfDay = Date.from(date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .spentDao()
                    .hasDataForDate(startOfDay, endOfDay)
                    .observe(this, hasData -> {
                        if (hasData != null && hasData != 0) {
                            calendarDate.setHasData(true);
                            dateAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private void showCategoryChart(Date start, Date end) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new CategoryChartFragment(start, end));
        transaction.addToBackStack(null);
        transaction.commit();

    }

    private void showCategoryBar() {
        FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
        transaction1.replace(R.id.container1, new MonthlyChartFragment());
        transaction1.addToBackStack(null);
        transaction1.commit();

    }

    private void updateMonthYearHeader() {
        // Get the middle date of the week for better month representation
        LocalDate middleDate = currentStartDate.plusDays(3);
        String monthYear = middleDate.format(monthYearFormatter);

    }

    private void updateButtonStates() {
        if (!dates.isEmpty()) {
            LocalDate lastDate = dates.get(dates.size() - 1).getDate();
            btnNext.setEnabled(lastDate.isBefore(today));
        }
    }

    public void onDateClick(int position) {
        if (interval.equals("custom"))
            Toast.makeText(this, "Custom interval is Selected", Toast.LENGTH_SHORT).show();

        CalendarDate clickedDate = dates.get(position);
        if (!clickedDate.isFuture()) {
            for (CalendarDate date : dates) {
                date.setSelected(date == clickedDate);
            }
            dateAdapter.notifyDataSetChanged();

            selectedDate = clickedDate.getDate(); // Store the selected date
/*            tvSelectedDate.setText(today.equals(selectedDate) ? "Today" :
                    selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));*/

//            showDailyTotalForSelectedDate(selectedDate);
            updateBudgetDisplay();
            checkInterval();

        }
    }


    private void checkInterval() {
        Date startOfDay = new Date();
        Date endOfDay = new Date();
        if (interval.equals("daily")) {
            startOfDay = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            endOfDay = Date.from(selectedDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            filterByDateRange(endOfDay, startOfDay);
            filterByDateRangeSpent(endOfDay, startOfDay);
            showCategoryChart(endOfDay, startOfDay);
            return;

        } else if (interval.equals("weekly")) {
            startOfDay = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            endOfDay = Date.from(selectedDate.plusWeeks(-1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            filterByDateRange(startOfDay, endOfDay);
            filterByDateRangeSpent(startOfDay, endOfDay);
        } else if (interval.equals("monthly")) {
            startOfDay = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            endOfDay = Date.from(selectedDate.plusMonths(-1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            filterByDateRange(startOfDay, endOfDay);
            filterByDateRangeSpent(startOfDay, endOfDay);
        }

        showCategoryChart(startOfDay, endOfDay);
        updateBudgetDisplay();
    }

    private void addToAmount(double amountToAdd, EditText etAmount) {

        try {
            // Get current amount (default to 0 if empty)
            double currentAmount = etAmount.getText().toString().isEmpty() ?
                    0 : Double.parseDouble(etAmount.getText().toString());

            // Add the new amount
            double newAmount = currentAmount + amountToAdd;

            // Update the field (format to 2 decimal places)
            etAmount.setText(String.format(Locale.getDefault(), "%.2f", newAmount));
        } catch (NumberFormatException e) {
            // Handle invalid number format
            etAmount.setText(String.format(Locale.getDefault(), "%.2f", amountToAdd));
        }
    }

    private void showAddSpentDialog(LocalDate initialDate) {
        selectedDate = initialDate;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.modal_add_spent, null);
        builder.setView(dialogView);

        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        EditText etDate = dialogView.findViewById(R.id.etDate);


        // Amount buttons
        dialogView.findViewById(R.id.btnAmount10).setOnClickListener(v -> {
            addToAmount(10, etAmount);
            animateAmountChange(etAmount);
            playClickSound();
        });
        dialogView.findViewById(R.id.btnAmount100).setOnClickListener(v -> {
            addToAmount(100, etAmount);
            animateAmountChange(etAmount);
            playClickSound();
        });
        dialogView.findViewById(R.id.btnAmount500).setOnClickListener(v -> {
            addToAmount(500, etAmount);
            animateAmountChange(etAmount);
            playClickSound();
        });
        dialogView.findViewById(R.id.btnAmount1000).setOnClickListener(v -> {
            addToAmount(1000, etAmount);
            animateAmountChange(etAmount);
            playClickSound();
        });

        // Category dropdown
        AutoCompleteTextView categoryDropdown = dialogView.findViewById(R.id.etCategory);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getCategoriesFromDatabase() // Implement this to get existing categories
        );
        categoryDropdown.setAdapter(categoryAdapter);
        categoryDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                categoryDropdown.showDropDown();
            }
        });

        // Date picker

        // Format the selected date
        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        etDate.setText(formattedDate);

        // Date picker - still allow changing if needed
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(selectedDate.getYear(),
                    selectedDate.getMonthValue() - 1,
                    selectedDate.getDayOfMonth());

            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDate = LocalDate.of(year, month + 1, day);
                        etDate.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        builder.setTitle("Add New Expense")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Validate inputs
                    if (etAmount.getText().toString().isEmpty() ||
                            etCategory.getText().toString().isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(etAmount.getText().toString());
                        String category = etCategory.getText().toString();

                        // Convert LocalDate to Date for your Spent object
                        Date date = Date.from(selectedDate.atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

                        // Create new Spent object
                        Spent newSpent = new Spent(amount, category, date);

                        // Insert into database
                        insertSpent(newSpent);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void animateAmountChange(EditText etAmount) {
        etAmount.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction(() -> etAmount.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100));
    }

    private void playClickSound() {
        MediaPlayer.create(this, R.raw.click).start();
    }

    private void insertSpent(Spent spent) {
        new Thread(() -> {
            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .spentDao()
                    .insert(spent);

            // Refresh the list and update widget
            runOnUiThread(() -> {
                checkInterval();
                updateWidget(getApplicationContext());
            });
        }).start();
    }

    MenuItem menuChart,menuBar,menuBudget;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
         menuChart = menu.findItem(R.id.menuChart);
        menuBar = menu.findItem(R.id.menuBar);
        menuBudget = menu.findItem(R.id.menuBudget);


        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuChart) {

            if (container.getVisibility() == View.GONE) {
                container.setVisibility(View.VISIBLE);
                Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                container.startAnimation(slideUp);
            } else {
                Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
                slideDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        container.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                container.startAnimation(slideDown);

            }

        } else if (item.getItemId() == R.id.menuBar) {
            showCategoryBar();

            if (container1.getVisibility() == View.GONE) {

                container1.setVisibility(View.VISIBLE);
                Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                container1.startAnimation(slideUp);

                // Rotate FAB
            } else {
                Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
                slideDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        container1.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                container1.startAnimation(slideDown);
            }

        } else if (item.getItemId() == R.id.menuBudget) {
            showSetBudgetDialog();
        } else {

        }

        return super.onOptionsItemSelected(item);
    }
}
