package me.trev.artdroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

        // Assessing intent
        String path;
        Intent intent = getIntent();
        drawingView.setTitle(intent.getStringExtra("title"));
        if ((path = intent.getStringExtra("path")) != null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inMutable = true;
            drawingView.setBitmap(BitmapFactory.decodeFile(path, opts));
        }

        // Set default brush width
        float defaultWidth = Math.round(sliderBrushWidth.getValue() * 10) / 10.0f;
        drawingView.getPaint().setStrokeWidth(defaultWidth);
        tvBrushWidth.setText(String.valueOf(defaultWidth));

        // Set default brush color
        drawingView.getPaint().setColor(Color.BLACK);
        btnColorPicker.setBackgroundColor(Color.BLACK);

        sliderBrushWidth.setLabelBehavior(LabelFormatter.LABEL_GONE);
        sliderBrushWidth.addOnChangeListener((slider, value, fromUser) -> {
            float width = Math.round(value * 10) / 10.0f;

            drawingView.getPaint().setStrokeWidth(width);
            tvBrushWidth.setText(String.valueOf(width));
        });

        btnColorPicker.setOnClickListener(view -> new ColorPickerDialog.Builder(this)
                .setTitle("Pick a color")
                .setPositiveButton(getString(R.string.confirm),
                        (ColorEnvelopeListener) (envelope, fromUser) -> {
                            int pickedColor = envelope.getColor();

                            drawingView.getPaint().setColor(pickedColor);
                            btnColorPicker.setBackgroundColor(pickedColor);
                        })
                .setNegativeButton(getString(R.string.cancel),
                        (dialogInterface, i) -> dialogInterface.dismiss())
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show());
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
            File saveDir = new File(getFilesDir(), "saved");
            if (!saveDir.exists())
                saveDir.mkdirs();

            File file = new File(saveDir, drawingView.getTitle() + ".png");
            FileOutputStream fos;

            try {
                fos = new FileOutputStream(file);
                drawingView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(file));
                sendBroadcast(mediaScanIntent);

                Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        } else if (itemId == R.id.export) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, drawingView.getTitle());
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