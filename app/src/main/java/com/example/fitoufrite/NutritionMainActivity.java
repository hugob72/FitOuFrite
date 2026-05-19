package com.example.fitoufrite;

import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NutritionMainActivity extends AppCompatActivity {

    LinearLayout linearLayoutRepas = null;
    Button newRepasButton = null;
    Button nutritionsButton = null;
    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );
    private List<Repas> mesRepas = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM", Locale.FRANCE);


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
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        params.setMargins(0, 0, 0, 2);

        linearLayoutRepas = findViewById(R.id.linearLayoutRepas);


        // Définit le bouton pour ajouter un repas
        newRepasButton = findViewById(R.id.newRepasButton);
        newRepasButton.setOnClickListener(v -> {
            Intent intent = new Intent(NutritionMainActivity.this, AddMealActivity.class);
            startActivity(intent);
        });

        // Définit le bouton pour voir le rapport de nutrition
        nutritionsButton = findViewById(R.id.btnVoirNutritions);
        nutritionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(NutritionMainActivity.this, NutritionReportActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mesRepas = GestionnaireRepas.chargerListe(this);
        afficherListeRepas();
    }

    /** Affiche dans le LinearLayout la liste des repas enregistrés */
    private void afficherListeRepas() {
        linearLayoutRepas.removeAllViews();

        for (int i = 0; i < mesRepas.size(); i++) {
            Repas repas = mesRepas.get(i);
            final int indexDuRepas = i;

            Button buttonRepas = new Button(this);
            String dateFormatee = repas.getDate() != null ? sdf.format(repas.getDate()) :getString(R.string.erreur_date);
            buttonRepas.setText(getString(R.string.format_bouton_repas, dateFormatee, repas.getTypeRepas().toString()));
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