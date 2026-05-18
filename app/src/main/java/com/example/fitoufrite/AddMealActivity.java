package com.example.fitoufrite;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class AddMealActivity extends AppCompatActivity {

    AutoCompleteTextView autoComplete = null;
    Button ajouterButton = null;
    Button supprimerButton = null;
    Spinner repasSpinner = null;

    private List<Ingredient> baseAliments;
    private List<IngredientSaisi> alimentsConsommes;
    private int indexRepasEdition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_meal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvSectionRepas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        alimentsConsommes = new ArrayList<>();

        initialiserBaseAliments();
        setupSpinners();

        // Cas de l'édition d'un repas
        indexRepasEdition = getIntent().getIntExtra("INDEX_REPAS", -1);
        if (indexRepasEdition != -1) {
            Repas repasAEditer = MockDataGenerator.getHistoriqueRepas().get(indexRepasEdition);

            EditText etDate = findViewById(R.id.etDate);
            etDate.setText(repasAEditer.getDate());
            repasSpinner.setSelection(repasAEditer.getTypeRepas().ordinal());
            alimentsConsommes.addAll(repasAEditer.getIngredientSaisis());
            afficherListeIngredients();

            Button btnEnregistrer = findViewById(R.id.btnEnregistrerRepas);
            btnEnregistrer.setText("Mettre à jour");
        }


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

        // Enregistrement
        Button btnEnregistrer = findViewById(R.id.btnEnregistrerRepas);
        btnEnregistrer.setOnClickListener(v -> {
            EditText etDate = findViewById(R.id.etDate);
            Spinner spinnerType = findViewById(R.id.spinnerTypeRepas);

            String date = etDate.getText().toString();
            TypeRepas typeEnum = (TypeRepas) spinnerType.getSelectedItem();

            if (!date.isEmpty() && !alimentsConsommes.isEmpty()) {
                Repas nouveauRepas = new Repas(date, typeEnum, new ArrayList<>(alimentsConsommes));
                if (indexRepasEdition != -1) {
                    MockDataGenerator.getHistoriqueRepas().set(indexRepasEdition, nouveauRepas);
                    Toast.makeText(this, "Repas mis à jour !", Toast.LENGTH_SHORT).show();
                }
                // Sinon, c'est une création classique
                else {
                    MockDataGenerator.ajouterUnRepas(nouveauRepas);
                    Toast.makeText(this, "Nouveau repas enregistré !", Toast.LENGTH_SHORT).show();
                }
                finish();
            } else {
                Toast.makeText(this, "Date vide ou aucun ingrédient !", Toast.LENGTH_SHORT).show();
            }
        });

        ajouterButton = findViewById(R.id.ajouterButton);
        ajouterButton.setOnClickListener(v -> ajouterIngredient());
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
        repasSpinner = findViewById(R.id.spinnerTypeRepas);
        ArrayAdapter<TypeRepas> adapterRepas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TypeRepas.values());
        repasSpinner.setAdapter(adapterRepas);
    }

    /** Ajoute un ingrédient au Repas */
    private void ajouterIngredient() {
        // TODO : A deplacer dans le onCreate() ?
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

                afficherListeIngredients();

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

    /** Reconstruit la liste des ingrédients qui composent un repas */
    private void afficherListeIngredients() {
        LinearLayout linearLayoutIngredients = findViewById(R.id.linearLayoutIngredients);
        linearLayoutIngredients.removeAllViews();

        for (int i = 0; i < alimentsConsommes.size(); i++) {
            IngredientSaisi saisi = alimentsConsommes.get(i);
            final int indexIngredient = i;

            // Création de la ligne de l'ingrédient
            LinearLayout ligneLayout = new LinearLayout(this);
            ligneLayout.setOrientation(LinearLayout.HORIZONTAL);
            ligneLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ligneLayout.setPadding(0, 16, 0, 16);

            // Création du texte
            TextView ingredientTextView = new TextView(this);
            ingredientTextView.setText("- " + saisi.getIngredient().getNom() + " (" + saisi.getQuantiteEnGrammes() + "g)");
            ingredientTextView.setTextSize(16f);
            LinearLayout.LayoutParams paramsTexte = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            ingredientTextView.setLayoutParams(paramsTexte);

            // Création du bouton Supprimer
            supprimerButton = new Button(this);
            supprimerButton.setText("X");
            supprimerButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            supprimerButton.setOnClickListener(v -> {
                alimentsConsommes.remove(indexIngredient);
                afficherListeIngredients();
                Toast.makeText(AddMealActivity.this, "Ingrédient supprimé", Toast.LENGTH_SHORT).show();
            });

            ligneLayout.addView(ingredientTextView);
            ligneLayout.addView(supprimerButton);
            linearLayoutIngredients.addView(ligneLayout);
        }
    }

}