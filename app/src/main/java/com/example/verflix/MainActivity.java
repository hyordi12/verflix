package com.example.verflix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private LinearLayout categories;
    private static final String CURRENT_USER_FILE_NAME = "current_user.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGreeting = findViewById(R.id.tvGreeting);
        categories = findViewById(R.id.categories);

        String userData = readFromFile(CURRENT_USER_FILE_NAME);
        if (userData != null && !userData.isEmpty()) {
            String[] data = userData.split(",");
            if (data.length == 3) {
                String name = data[0];
                int age;
                try {
                    age = Integer.parseInt(data[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.error_edad_invalida, Toast.LENGTH_SHORT).show();
                    return;
                }

                tvGreeting.setText(getString(R.string.hola, name));

                if (age < 12) {
                    addCategory(getString(R.string.caricaturas), R.drawable.caricaturas);
                } else if (age < 18) {
                    addCategory(getString(R.string.caricaturas), R.drawable.caricaturas);
                    addCategory(getString(R.string.accion), R.drawable.accion);
                } else {
                    addCategory(getString(R.string.caricaturas), R.drawable.caricaturas);
                    addCategory(getString(R.string.accion), R.drawable.accion);
                    addCategory(getString(R.string.terror), R.drawable.terror);
                }
            } else {
                Toast.makeText(this, R.string.error_datos_incompletos, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.error_lectura_datos, Toast.LENGTH_SHORT).show();
        }
    }

    private void addCategory(String category, int imageResId) {
        View categoryView = getLayoutInflater().inflate(R.layout.category_item, null);
        ImageView ivCategoryImage = categoryView.findViewById(R.id.ivCategoryImage);
        TextView tvCategoryLabel = categoryView.findViewById(R.id.tvCategoryLabel);

        ivCategoryImage.setImageResource(imageResId);
        tvCategoryLabel.setText(category);

        categoryView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VideoActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        });

        categories.addView(categoryView);
    }

    private String readFromFile(String fileName) {
        FileInputStream fis = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            fis = openFileInput(fileName);
            int character;
            while ((character = fis.read()) != -1) {
                stringBuilder.append((char) character);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }
}
