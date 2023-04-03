package me.trev.artdroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import me.trev.views.DrawingView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tools
        MaterialButton btnColorPicker = findViewById(R.id.colorPickerButton);
        TextView tvBrushWidth = findViewById(R.id.tvBrushWidth);
        Slider sliderBrushWidth = findViewById(R.id.sliderBrushWidth);

        // Canvas
        DrawingView drawingView = findViewById(R.id.drawingView);

        float defaultWidth = Math.round(sliderBrushWidth.getValue() * 10) / 10.0f;
        drawingView.getPaint().setStrokeWidth(defaultWidth);
        tvBrushWidth.setText(String.valueOf(defaultWidth));

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
}