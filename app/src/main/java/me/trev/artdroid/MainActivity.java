package me.trev.artdroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.IOException;
import java.io.OutputStream;

import me.trev.views.DrawingView;

public class MainActivity extends AppCompatActivity {
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tools
        MaterialButton btnColorPicker = findViewById(R.id.colorPickerButton);
        TextView tvBrushWidth = findViewById(R.id.tvBrushWidth);
        Slider sliderBrushWidth = findViewById(R.id.sliderBrushWidth);

        // Canvas
        drawingView = findViewById(R.id.drawingView);

        // Set default brush width
        float defaultWidth = Math.round(sliderBrushWidth.getValue() * 10) / 10.0f;
        drawingView.getPaint().setStrokeWidth(defaultWidth);
        tvBrushWidth.setText(String.valueOf(defaultWidth));

        // Set default brush color
        drawingView.getPaint().setColor(Color.BLACK);
        btnColorPicker.setBackgroundColor(Color.BLACK);

        sliderBrushWidth.setLabelBehavior(LabelFormatter.LABEL_GONE);
        sliderBrushWidth.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                float width = Math.round(value * 10) / 10.0f;

                drawingView.getPaint().setStrokeWidth(width);
                tvBrushWidth.setText(String.valueOf(width));
            }
        });

        btnColorPicker.setOnClickListener(view -> {
            new ColorPickerDialog.Builder(this)
                    .setTitle("Pick a color")
                    .setPositiveButton(getString(R.string.confirm),
                            new ColorEnvelopeListener() {
                                @Override
                                public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                    int pickedColor = envelope.getColor();

                                    drawingView.getPaint().setColor(pickedColor);
                                    btnColorPicker.setBackgroundColor(pickedColor);
                                }
                            })
                    .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                    .attachAlphaSlideBar(true)
                    .attachBrightnessSlideBar(true)
                    .setBottomSpace(12)
                    .show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private Uri mUri;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.save) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "drawing");
            values.put(MediaStore.Images.Media.DESCRIPTION, "A drawing created in Artdroid");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
//            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            mUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (mUri != null) {
                try {
                    OutputStream out = getContentResolver().openOutputStream(mUri);
                    drawingView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();

                    values.clear();
//                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(mUri, values, null, null);

                    Toast.makeText(this, "Successfully saved image to gallery", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    getContentResolver().delete(mUri, null, null);
                    Toast.makeText(this, "Failed to save image to gallery", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }

            return true;
        } else if (itemId == R.id.share) {
            if (mUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, mUri);
                startActivity(Intent.createChooser(shareIntent, "Share to..."));
            } else {
                Toast.makeText(this, "Images can only be shared after being saved", Toast.LENGTH_SHORT).show();
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}