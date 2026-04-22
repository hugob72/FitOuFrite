package com.example.fitoufrite;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class NutritionActivity extends AppCompatActivity {

    AutoCompleteTextView autoComplete = null;
    Button btnAjouter = null;
    Spinner spinnerRepas = null;

    private List<Ingredient> baseAliments;
    private List<IngredientSaisi> alimentsConsommes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nutrition);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvSectionRepas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        alimentsConsommes = new ArrayList<>();

        initialiserBaseAliments();
        setupSpinners();

        // Configuration de l'Auto-complétion des aliments
        autoComplete = findViewById(R.id.autoCompleteIngredient);
        List<String> nomsAliments = new ArrayList<>();
        for (Ingredient ingredient : baseAliments) {
            nomsAliments.add(ingredient.getNom());
        }
        ArrayAdapter<String> adapterAutoComplete = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nomsAliments
        );
        autoComplete.setAdapter(adapterAutoComplete);

        // Remplissage automatique des champs si un aliment enregistré est sélectionné
        autoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String nomChoisi = adapterAutoComplete.getItem(position);
            Ingredient alimentChoisi = null;
            for (Ingredient ingredient : baseAliments) {
                if (ingredient.getNom().equals(nomChoisi)) {
                    alimentChoisi = ingredient;
                    break;
                }
            }
            if (alimentChoisi != null) {
                ((EditText) findViewById(R.id.proteinesEditText)).setText(String.valueOf(alimentChoisi.getProteines()));
                ((EditText) findViewById(R.id.glucidesEditText)).setText(String.valueOf(alimentChoisi.getGlucides()));
                ((EditText) findViewById(R.id.lipidesEditText)).setText(String.valueOf(alimentChoisi.getLipides()));
            }
        });

        btnAjouter = findViewById(R.id.ajouterButton);
        btnAjouter.setOnClickListener(v -> ajouterIngredient());
    }


    private void initialiserBaseAliments() {
        baseAliments = new ArrayList<>();
        baseAliments.add(new Ingredient("Riz blanc (cuit)", 2.7, 28.0, 0.3, 130.0, "A"));
        baseAliments.add(new Ingredient("Steak haché 5%", 21.0, 0.0, 5.0, 129.0, "A"));
        baseAliments.add(new Ingredient("Pâtes (cuites)", 5.0, 30.0, 1.0, 150.0, "A"));
        baseAliments.add(new Ingredient("Tomates", 0.9, 3.9, 0.2, 18.0, "A"));
        baseAliments.add(new Ingredient("Pain blanc", 8.0, 49.0, 1.5, 265.0, "C"));
        baseAliments.add(new Ingredient("Huile d'olive", 0.0, 0.0, 100.0, 900.0, "C"));
    }

    private void setupSpinners() {
        spinnerRepas = findViewById(R.id.spinnerTypeRepas);
        String[] typesRepas = {"Petit-déjeuner", "Déjeuner", "Dîner", "Extra"};
        ArrayAdapter<String> adapterRepas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typesRepas);
        spinnerRepas.setAdapter(adapterRepas);
    }

    private void ajouterIngredient() {
        AutoCompleteTextView autoComplete = findViewById(R.id.autoCompleteIngredient);
        EditText quantiteEditText = findViewById(R.id.quantiteEditText);
        EditText proteinesEditText = findViewById(R.id.proteinesEditText);
        EditText glucidesEditText = findViewById(R.id.glucidesEditText);
        EditText lipidesEditText = findViewById(R.id.lipidesEditText);

        String nom = autoComplete.getText().toString();
        String quantiteStr = quantiteEditText.getText().toString();
        String protStr = proteinesEditText.getText().toString();
        String glucStr = glucidesEditText.getText().toString();
        String lipStr = lipidesEditText.getText().toString();

        if (!nom.isEmpty() && !quantiteStr.isEmpty() && !protStr.isEmpty() && !glucStr.isEmpty() && !lipStr.isEmpty()) {
            try {
                double quantite = Double.parseDouble(quantiteStr);
                double proteines = Double.parseDouble(protStr);
                double glucides = Double.parseDouble(glucStr);
                double lipides = Double.parseDouble(lipStr);

                // Estimation des Kcal pour les ajouts manuels (1g prot/gluc = 4kcal, 1g lip = 9kcal)
                double kcalEstime = (proteines * 4) + (glucides * 4) + (lipides * 9);

                Ingredient nouvelIngredient = new Ingredient(nom, proteines, glucides, lipides, kcalEstime, "N/A");
                IngredientSaisi saisi = new IngredientSaisi(nouvelIngredient, quantite);

                alimentsConsommes.add(saisi);

                Toast.makeText(this, nom + " ajouté avec succès !", Toast.LENGTH_SHORT).show();

                autoComplete.getText().clear();
                quantiteEditText.getText().clear();
                proteinesEditText.getText().clear();
                glucidesEditText.getText().clear();
                lipidesEditText.getText().clear();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Erreur de format dans les nombres", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show();
        }
    }

}