package me.trev.artdroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class Home extends AppCompatActivity {
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvEmpty = findViewById(R.id.empty);

        findViewById(R.id.new_file).setOnClickListener(view -> {
            String title = ((TextView) findViewById(R.id.filename_new)).getText().toString();
            File file = new File(getFilesDir() + File.separator + "saved", title + ".png");
            if (!file.getName().equals(title + ".png")) {
                Toast.makeText(this, "A file name should not contain illegal characters!", Toast.LENGTH_LONG).show();
            } else if (file.exists()) {
                Toast.makeText(this, "A file with the name " + title + " already exists!", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("title", title);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        LinearLayout list = findViewById(R.id.list);
        list.removeViews(3, list.getChildCount() - 3);

        File saveDir = new File(getFilesDir(), "saved");
        File[] files = saveDir.listFiles();
        tvEmpty.setVisibility((files == null) ? View.VISIBLE : View.INVISIBLE);

        if (files != null) {
            for (File file : files) {
                TextView textView = new TextView(this);
                textView.setText(file.getName());

                textView.setOnClickListener(view -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("path", file.getAbsolutePath());
                    intent.putExtra("title", file.getName().substring(0, file.getName().length() - 4));
                    startActivity(intent);
                });
                list.addView(textView);
            }
        }
    }
}