package com.example.fitoufrite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class NutritionMainActivity extends AppCompatActivity {

    LinearLayout linearLayoutRepas = null;
    Button newRepasButton = null;
    private List<Repas> mesRepas = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_nutrition);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_nutrition), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mesRepas = MockDataGenerator.genererHistoriqueRepas();
        linearLayoutRepas = findViewById(R.id.linearLayoutRepas);

        newRepasButton = findViewById(R.id.newRepasButton);
        newRepasButton.setOnClickListener(v -> {
            Intent intent = new Intent(NutritionMainActivity.this, AddMealActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        afficherListeRepas();
    }

    private void afficherListeRepas() {
        linearLayoutRepas.removeAllViews();

        for (int i = 0; i < mesRepas.size(); i++) {
            Repas repas = mesRepas.get(i);
            final int indexDuRepas = i;

            Button buttonRepas = new Button(this);
            buttonRepas.setText("Repas du " + repas.getDate() + " - " + repas.getTypeRepas());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 2);
            buttonRepas.setLayoutParams(params);

            buttonRepas.setOnClickListener(v -> {
                Intent intent = new Intent(NutritionMainActivity.this, AddMealActivity.class);
                intent.putExtra("INDEX_REPAS", indexDuRepas);
                startActivity(intent);
            });

            linearLayoutRepas.addView(buttonRepas);
        }
    }

}