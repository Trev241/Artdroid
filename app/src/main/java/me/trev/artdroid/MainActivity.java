package me.trev.artdroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.paint.R;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import me.trev.views.DrawingView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvBrushWidth = findViewById(R.id.tvBrushWidth);
        DrawingView drawingView = findViewById(R.id.drawingView);
        Slider sliderBrushWidth = findViewById(R.id.sliderBrushWidth);

        float defaultWidth = Math.round(sliderBrushWidth.getValue() * 10) / 10.0f;
        drawingView.getPaint().setStrokeWidth(defaultWidth);
        tvBrushWidth.setText(String.valueOf(defaultWidth));

        sliderBrushWidth.setLabelBehavior(LabelFormatter.LABEL_GONE);
        sliderBrushWidth.addOnChangeListener((slider, value, fromUser) -> {
            float width = Math.round(value * 10) / 10.0f;

            drawingView.getPaint().setStrokeWidth(width);
            tvBrushWidth.setText(String.valueOf(width));
        });
    }
}