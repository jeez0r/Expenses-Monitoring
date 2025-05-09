package com.magnaye.spendmonitoring.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.magnaye.spendmonitoring.Module.AddSpentWidgetProvider;
import com.magnaye.spendmonitoring.R;

// CategorySelectionActivity.java
public class CategorySelectionActivity extends Activity {
    private static final String TAG = "CategorySelection";
    private int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get widget ID from intent
        Intent intent = getIntent();
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        // Show category selection dialog
        showCategoryDialog();
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category");
        builder.setItems(R.array.categories_array, (dialog, which) -> {
            String[] categories = getResources().getStringArray(R.array.categories_array);
            String selectedCategory = categories[which];

            // Update widget with selected category
            updateWidgetCategory(selectedCategory);
            finish();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.setOnCancelListener(dialog -> finish());
        builder.show();
    }

    private void updateWidgetCategory(String category) {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            SharedPreferences prefs = getSharedPreferences(
                    AddSpentWidgetProvider.SHARED_PREFS, MODE_PRIVATE);
            prefs.edit()
                    .putString(AddSpentWidgetProvider.CATEGORY_KEY + appWidgetId, category)
                    .apply();

            // Update the widget
            Intent intent = new Intent(this, AddSpentWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = {appWidgetId};
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        }
    }
}
