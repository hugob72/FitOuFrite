package com.example.fitoufrite;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AddMealActivity extends AppCompatActivity {

    AutoCompleteTextView autoComplete = null;
    Spinner spinnerTypeRepas = null;
    Button ajouterButton = null;
    Button supprimerButton = null;
    Spinner repasSpinner = null;
    EditText dateRepasEditText = null;
    Button enregistrerButton = null;

    private java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE);
    private List<Ingredient> baseAliments;
    private List<IngredientSaisi> alimentsConsommes;
    private int indexRepasEdition = -1;
    private Timer tempsRecherche = new Timer();
    private String appId = "7dc497fa";
    private String appKey = "bf8ea977fb074b79e0729f96bca1aed2";
    private String urlAPI = "https://api.edamam.com/api/food-database/v2/parser?app_id=" + appId + "&app_key=" + appKey;

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
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        alimentsConsommes = new ArrayList<>();
        baseAliments = new ArrayList<>();
        setupSpinners();

        // Si on crée un nouveau repas, on met la date d'aujourd'hui par défaut
        dateRepasEditText = findViewById(R.id.dateRepasEditText);
        if (indexRepasEdition == -1) {
            dateRepasEditText.setText(sdf.format(new java.util.Date()));
        }

        dateRepasEditText.setOnClickListener(v -> {
            // On récupère la date affichée dans le champs
            java.util.Calendar calendrier = java.util.Calendar.getInstance();
            try {
                if (!dateRepasEditText.getText().toString().isEmpty()) {
                    calendrier.setTime(sdf.parse(dateRepasEditText.getText().toString()));
                }
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            int annee = calendrier.get(java.util.Calendar.YEAR);
            int mois = calendrier.get(java.util.Calendar.MONTH);
            int jour = calendrier.get(java.util.Calendar.DAY_OF_MONTH);

            // On affiche le calendrier
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    AddMealActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        java.util.Calendar nouvelleDate = java.util.Calendar.getInstance();
                        nouvelleDate.set(year, monthOfYear, dayOfMonth);
                        dateRepasEditText.setText(sdf.format(nouvelleDate.getTime()));
                    },
                    annee, mois, jour
            );
            datePickerDialog.show();
        });

        // Cas de l'édition d'un repas
        indexRepasEdition = getIntent().getIntExtra("INDEX_REPAS", -1);
        if (indexRepasEdition != -1) {
            Repas repasExistant = MockDataGenerator.getHistoriqueRepas().get(indexRepasEdition);

            dateRepasEditText.setText(sdf.format(repasExistant.getDate()));
            repasSpinner.setSelection(repasExistant.getTypeRepas().ordinal());
            alimentsConsommes.addAll(repasExistant.getIngredientSaisis());
            afficherListeIngredients();

            enregistrerButton = findViewById(R.id.enregistrerRepasButton);
            enregistrerButton.setText(getString(R.string.btn_enregistrer_maj));
        }


        // Configuration de l'auto-complétion des aliments récupéré depuis l'API EDANAM
        autoComplete = findViewById(R.id.autoCompleteIngredient);
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Réagit à ce que l'utilisateur saisie
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String recherche = s.toString().trim();
                tempsRecherche.cancel();
                // On lance la recherche à partir de 2 lettres et si aucune lettre n'a été saisie depuis 1000 ms
                if (recherche.length() >= 2) {
                    tempsRecherche = new Timer();
                    tempsRecherche.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            JsonObjectRequest request = new JsonObjectRequest(
                                    Request.Method.GET,
                                    urlAPI + "&ingr=" + recherche,
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Gson gson = new Gson();
                                            JsonElement jsonElement = gson.fromJson(response.toString(), JsonElement.class);

                                            try {
                                                JSONObject rootObject = new JSONObject(jsonElement.toString());
                                                JSONArray hintsArray = rootObject.getJSONArray("hints");
                                                baseAliments.clear();
                                                List<String> nomIngredients = new ArrayList<>();

                                                if (hintsArray.length() > 0) {
                                                    for (int i = 0; i < hintsArray.length(); i++) {
                                                        JSONObject food = hintsArray.getJSONObject(i).getJSONObject("food");
                                                        JSONObject nutriments = food.getJSONObject("nutrients");

                                                        String nom = food.getString("label");

                                                        double kcal = nutriments.optDouble("ENERC_KCAL", 0.0);
                                                        double proteines = nutriments.optDouble("PROCNT", 0.0);
                                                        double glucides = nutriments.optDouble("CHOCDF", 0.0);
                                                        double lipides = nutriments.optDouble("FAT", 0.0);

                                                        baseAliments.add(new Ingredient(nom, proteines, glucides, lipides, kcal, "N/A"));
                                                        nomIngredients.add(nom);
                                                    }
                                                }

                                                // Mise à jour de la liste déroulante
                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                                        AddMealActivity.this,
                                                        android.R.layout.simple_dropdown_item_1line,
                                                        nomIngredients
                                                );
                                                autoComplete.setAdapter(adapter);
                                                autoComplete.showDropDown();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("Edamam API Error", error.toString());
                                        }
                                    }
                            );
                            requestQueue.add(request);
                        }
                    }, 1000);

                }
            }
        });

        // Remplissage des champs lorsqu'un ingredient est choisie
        autoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String ingredientChoisi = autoComplete.getAdapter().getItem(position).toString();

            for (Ingredient ingredient : baseAliments) {
                if (ingredient.getNom().equals(ingredientChoisi)) {
                    ((EditText) findViewById(R.id.proteinesEditText)).setText(String.format(java.util.Locale.US, "%.1f", ingredient.getProteines()));
                    ((EditText) findViewById(R.id.glucidesEditText)).setText(String.format(java.util.Locale.US, "%.1f", ingredient.getGlucides()));
                    ((EditText) findViewById(R.id.lipidesEditText)).setText(String.format(java.util.Locale.US, "%.1f", ingredient.getLipides()));
                    break;
                }
            }
        });

        // Enregistrement du Repas
        enregistrerButton = findViewById(R.id.enregistrerRepasButton);
        enregistrerButton.setOnClickListener(v -> {
            spinnerTypeRepas = findViewById(R.id.spinnerTypeRepas);

            String dateStr = dateRepasEditText.getText().toString();
            TypeRepas typeEnum = (TypeRepas) spinnerTypeRepas.getSelectedItem();

            if (!dateStr.isEmpty() && !alimentsConsommes.isEmpty()) {
                try {
                    java.util.Date dateDuRepas = sdf.parse(dateStr);
                    Repas nouveauRepas = new Repas(dateDuRepas, typeEnum, new ArrayList<>(alimentsConsommes));

                    if (indexRepasEdition != -1) {
                        MockDataGenerator.getHistoriqueRepas().set(indexRepasEdition, nouveauRepas);
                        Toast.makeText(this, getString(R.string.toast_repas_maj), Toast.LENGTH_SHORT).show();
                    } else {
                        MockDataGenerator.ajouterUnRepas(nouveauRepas);
                        Toast.makeText(this, getString(R.string.toast_repas_nouveau), Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } catch (java.text.ParseException e) {
                    Toast.makeText(this, getString(R.string.toast_erreur_date_format), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_erreur_vide), Toast.LENGTH_SHORT).show();
            }
        });

        ajouterButton = findViewById(R.id.ajouterButton);
        ajouterButton.setOnClickListener(v -> ajouterIngredient());
    }

    /** Met en place le spinner */
    private void setupSpinners() {
        repasSpinner = findViewById(R.id.spinnerTypeRepas);
        ArrayAdapter<TypeRepas> adapterRepas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TypeRepas.values());
        repasSpinner.setAdapter(adapterRepas);
    }

    /** Ajoute un ingrédient au Repas */
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

                // Calcul des Kcal (1g protéine et glucide = 4kcal, 1g lipide = 9kcal)
                double kcalEstime = (proteines * 4) + (glucides * 4) + (lipides * 9);

                Ingredient nouvelIngredient = new Ingredient(nom, proteines, glucides, lipides, kcalEstime, "N/A");
                IngredientSaisi saisi = new IngredientSaisi(nouvelIngredient, quantite);

                alimentsConsommes.add(saisi);

                Toast.makeText(this, getString(R.string.toast_ingredient_ajoute, nom), Toast.LENGTH_SHORT).show();

                afficherListeIngredients();

                autoComplete.getText().clear();
                quantiteEditText.getText().clear();
                proteinesEditText.getText().clear();
                glucidesEditText.getText().clear();
                lipidesEditText.getText().clear();

            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.toast_erreur_nombres), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_erreur_champs_obligatoires), Toast.LENGTH_SHORT).show();
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
            ingredientTextView.setText(getString(R.string.format_ingredient_liste, saisi.getIngredient().getNom(), String.valueOf(saisi.getQuantiteEnGrammes())));
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
                Toast.makeText(AddMealActivity.this, getString(R.string.toast_ingredient_supprime), Toast.LENGTH_SHORT).show();
            });

            ligneLayout.addView(ingredientTextView);
            ligneLayout.addView(supprimerButton);
            linearLayoutIngredients.addView(ligneLayout);
        }
    }

}