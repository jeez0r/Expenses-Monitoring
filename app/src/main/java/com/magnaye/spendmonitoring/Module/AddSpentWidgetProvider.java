// AddSpentWidgetProvider.java
package com.magnaye.spendmonitoring.Module;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.magnaye.spendmonitoring.Activity.CategorySelectionActivity;
import com.magnaye.spendmonitoring.DatabaseHelper.DatabaseClient;
import com.magnaye.spendmonitoring.Model.Spent;
import com.magnaye.spendmonitoring.R;

import java.util.Date;
import java.util.Locale;

public class AddSpentWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "ExpenseWidget";
    public static final String SHARED_PREFS = "widget_prefs";
    private static final String AMOUNT_KEY = "amount_";
    public static final String CATEGORY_KEY = "category_";

    // Define your categories
    private static final String[] CATEGORIES = {
            "Food", "Transport", "Shopping", "Bills", "Entertainment", "Other"
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            String amount = prefs.getString(AMOUNT_KEY + appWidgetId, "0.00");
            String category = prefs.getString(CATEGORY_KEY + appWidgetId, "Food");

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_add_spent);
            views.setTextViewText(R.id.etAmount, amount);
            views.setTextViewText(R.id.btnCategory, category);

            // Set click handlers
            setAmountButtonClick(context, views, R.id.btnAmount10, 10, appWidgetId);
            setAmountButtonClick(context, views, R.id.btnAmount100, 100, appWidgetId);
            setAmountButtonClick(context, views, R.id.btnAmount500, 500, appWidgetId);
            setAmountButtonClick(context, views, R.id.btnAmount1000, 1000, appWidgetId);
            setClearButtonClick(context, views, appWidgetId); // Add this line
            setCategoryButtonClick(context, views, appWidgetId);
            setSaveButtonClick(context, views, appWidgetId);

            Intent categoryIntent = new Intent(context, CategorySelectionActivity.class);
            categoryIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent categoryPendingIntent = PendingIntent.getActivity(
                    context, 0, categoryIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btnCategory, categoryPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "Error updating widget", e);
        }
    }

    private void setAmountButtonClick(Context context, RemoteViews views, int buttonId, double amount, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction("ADD_AMOUNT");
        intent.putExtra("amount", amount);
        intent.putExtra("appWidgetId", appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                buttonId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(buttonId, pendingIntent);
    }

    private void setCategoryButtonClick(Context context, RemoteViews views, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction("SELECT_CATEGORY");
        intent.putExtra("appWidgetId", appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnCategory, pendingIntent);
    }

    private void setSaveButtonClick(Context context, RemoteViews views, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction("SAVE_EXPENSE");
        intent.putExtra("appWidgetId", appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnSave, pendingIntent);
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction() != null) {
            int appWidgetId = intent.getIntExtra("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
            SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

            switch (intent.getAction()) {
                case "ADD_AMOUNT":
                    handleAddAmount(context, prefs, intent, appWidgetId);
                    break;
                case "SELECT_CATEGORY":
                    showCategorySelectionDialog(context, appWidgetId);
                    break;
                case "CATEGORY_SELECTED":
                    handleCategorySelected(context, prefs, intent, appWidgetId);
                    break;
                case "SAVE_EXPENSE":
                    handleSaveExpense(context, prefs, appWidgetId);
                    break;
                case "CLEAR_AMOUNT":
                    handleClearAmount(context, prefs, appWidgetId);
                    break;
            }
        }
    }

    private void handleAddAmount(Context context, SharedPreferences prefs, Intent intent, int appWidgetId) {
        try {
            double amountToAdd = intent.getDoubleExtra("amount", 0);
            String currentAmount = prefs.getString(AMOUNT_KEY + appWidgetId, "0.00");
            double newAmount = Double.parseDouble(currentAmount) + amountToAdd;
            String formattedAmount = String.format(Locale.US, "%.2f", newAmount);

            prefs.edit().putString(AMOUNT_KEY + appWidgetId, formattedAmount).apply();
            updateWidget(context, appWidgetId);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error adding amount", e);
        }
    }

    private void showCategorySelectionDialog(Context context, int appWidgetId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Category");
        builder.setItems(CATEGORIES, (dialog, which) -> {
            Intent intent = new Intent(context, getClass());
            intent.setAction("CATEGORY_SELECTED");
            intent.putExtra("category", CATEGORIES[which]);
            intent.putExtra("appWidgetId", appWidgetId);
            context.sendBroadcast(intent);
        });
        builder.setNegativeButton("Cancel", null);

        // Must use SYSTEM_ALERT_WINDOW permission for widgets
        builder.create().getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        builder.show();
    }

    private void handleCategorySelected(Context context, SharedPreferences prefs, Intent intent, int appWidgetId) {
        String category = intent.getStringExtra("category");
        prefs.edit().putString(CATEGORY_KEY + appWidgetId, category).apply();
        updateWidget(context, appWidgetId);
    }

    private void handleSaveExpense(Context context, SharedPreferences prefs, int appWidgetId) {
        try {
            String amount = prefs.getString(AMOUNT_KEY + appWidgetId, "0.00");
            String category = prefs.getString(CATEGORY_KEY + appWidgetId, "Food");

            if (Double.parseDouble(amount) > 0) {
                saveExpenseToDatabase(context, Double.parseDouble(amount), category);

                // Reset after saving
                prefs.edit()
                        .putString(AMOUNT_KEY + appWidgetId, "0.00")
                        .putString(CATEGORY_KEY + appWidgetId, "Food")
                        .apply();

                updateWidget(context, appWidgetId);
                showToast(context, "Expense saved!");
            } else {
                showToast(context, "Amount must be greater than 0");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error saving expense", e);
            showToast(context, "Invalid amount");
        }
    }

    private void saveExpenseToDatabase(Context context, double amount, String category) {
        new Thread(() -> {
            Spent expense = new Spent(amount, category, new Date());
            DatabaseClient.getInstance(context)
                    .getAppDatabase()
                    .spentDao()
                    .insert(expense);
        }).start();
    }

    private void updateWidget(Context context, int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        updateWidget(context, appWidgetManager, appWidgetId);
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    private void setClearButtonClick(Context context, RemoteViews views, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction("CLEAR_AMOUNT");
        intent.putExtra("appWidgetId", appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnClear, pendingIntent);
    }

    private void handleClearAmount(Context context, SharedPreferences prefs, int appWidgetId) {
        try {
            // Reset amount to 0.00
            prefs.edit().putString("amount_" + appWidgetId, "0.00").apply();

            // Update widget display
            updateWidget(context, appWidgetId);

            // Show confirmation
            showToast(context, "Amount cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing amount", e);
            showToast(context, "Failed to clear amount");
        }
    }
}