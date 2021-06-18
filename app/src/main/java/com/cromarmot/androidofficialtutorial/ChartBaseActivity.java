package com.cromarmot.androidofficialtutorial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;

/**
 * Base class of all Activities of the Demo Application.
 *
 * @author Philipp Jahoda
 */
public abstract class ChartBaseActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_STORAGE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToGallery();
            } else {
                Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    protected void saveToGallery(Chart chart, String name) {
        if (chart.saveToGallery(name + "_" + System.currentTimeMillis(), 70))
            Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!",
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                    .show();
    }

    protected abstract void saveToGallery();
}
