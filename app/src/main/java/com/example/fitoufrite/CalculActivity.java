package com.example.fitoufrite;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CalculActivity extends AppCompatActivity {

    // Boutons de navigation et d'action
    private Button buttonDate;
    private Button buttonCalculImc;
    private Button buttonRaz;

    // Liste déroulante pour le genre
    private Spinner spinnerGenre;

    // Champs de saisie utilisateur
    private EditText dateInputText;
    private EditText poidsInputText;
    private EditText tailleInputText;

    // Choix de l'unité de taille
    private RadioButton radioMetre;
    private RadioButton radioCentimetre;

    // Option d'affichage détaillé du résultat
    private CheckBox checkAffichage;

    // Zone d'affichage du résultat
    private TextView resultatText;

    // Dernier IMC calculé (-1 = aucun calcul encore fait)
    private double dernierImc = -1;

    // Constantes pour le stockage local SharedPreferences
    private static final String PREF_NAME = "FitOuFritePrefs";
    private static final String KEY_IMC = "dernier_imc";

    // Message affichée par défaut avant le calcul
    private static final String RESULTAT_DEFAUT =
            "Vous devez cliquer sur le bouton ‘Calculer’ pour obtenir un résultat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calcul);
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        buttonDate = findViewById(R.id.buttonDate);
        buttonCalculImc = findViewById(R.id.buttonCalculImc);
        buttonRaz = findViewById(R.id.buttonRaz);

        spinnerGenre = findViewById(R.id.spinnerGenre);

        dateInputText = findViewById(R.id.dateInputText);
        poidsInputText = findViewById(R.id.poidsInputText);
        tailleInputText = findViewById(R.id.tailleInputText);

        radioMetre = findViewById(R.id.radioMetre);
        radioCentimetre = findViewById(R.id.radioCentimetre);

        checkAffichage = findViewById(R.id.checkAffichage);

        resultatText = findViewById(R.id.resultatText);

        initialiserSpinner();
        initialiserActions();
        initialiserTextWatchers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_email) {
            envoyerEmail();
            return true;
        } else if (item.getItemId() == R.id.action_nutrition) {
            calculNutrition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initialiserSpinner() {
        String[] genres = {getString(R.string.genre_homme), getString(R.string.genre_femme)};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genres
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(adapter);
    }

    private void initialiserActions() {
        buttonDate.setOnClickListener(v -> ouvrirCalendrier());

        buttonCalculImc.setOnClickListener(v -> calculerImc());

        buttonRaz.setOnClickListener(v -> afficherDialogRaz());
    }

    private void initialiserTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resultatText.setText(RESULTAT_DEFAUT);
                dernierImc = -1;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        dateInputText.addTextChangedListener(watcher);
        poidsInputText.addTextChangedListener(watcher);
        tailleInputText.addTextChangedListener(watcher);
    }

    private void calculerImc() {
        String dateNaissance = dateInputText.getText().toString().trim();
        String poidsTexte = poidsInputText.getText().toString().trim();
        String tailleTexte = tailleInputText.getText().toString().trim();

        if (dateNaissance.isEmpty() || poidsTexte.isEmpty() || tailleTexte.isEmpty()) {
            Toast.makeText(this, getString(R.string.erreur_champs_vides), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dateNaissance.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(this, getString(R.string.erreur_format_date), Toast.LENGTH_SHORT).show();
            return;
        }

        double poids;
        double taille;

        try {
            poids = Double.parseDouble(poidsTexte);
            taille = Double.parseDouble(tailleTexte);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.erreur_valeur_invalide), Toast.LENGTH_SHORT).show();
            return;
        }

        if (poids <= 0 || taille <= 0) {
            Toast.makeText(this, getString(R.string.erreur_valeur_zero), Toast.LENGTH_SHORT).show();
            return;
        }

        if (radioCentimetre.isChecked()) {
            taille = taille / 100.0;
        }

        double imc = poids / (taille * taille);
        dernierImc = imc;

        int age = calculerAge(dateNaissance);
        String categorie = determinerCategorie(imc, age);
        String genre = spinnerGenre.getSelectedItem().toString();

        String imcFormate = String.format("%.2f", imc);

        if (checkAffichage.isChecked()) {
            String civilite = genre.equals(getString(R.string.genre_homme)) ? getString(R.string.civilite_homme) : getString(R.string.civilite_femme);

            resultatText.setText(getString(R.string.format_resultat_complet, civilite, imcFormate, age, categorie));
        } else {
            resultatText.setText(getString(R.string.format_resultat_simple, imcFormate));
        }

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_IMC, imcFormate);
        editor.apply();
    }

    private int calculerAge(String dateNaissance) {
        String[] morceaux = dateNaissance.split("/");

        int jour = Integer.parseInt(morceaux[0]);
        int mois = Integer.parseInt(morceaux[1]);
        int annee = Integer.parseInt(morceaux[2]);

        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - annee;

        int moisActuel = today.get(Calendar.MONTH) + 1;
        int jourActuel = today.get(Calendar.DAY_OF_MONTH);

        if (moisActuel < mois || (moisActuel == mois && jourActuel < jour)) {
            age--;
        }

        return age;
    }

    private String determinerCategorie(double imc, int age) {
        if (age < 35) {
            if (imc < 18.5) return getString(R.string.categorie_insuffisance);
            if (imc < 24.9) return getString(R.string.categorie_normal);
            if (imc < 29.9) return getString(R.string.categorie_surpoids);
            return getString(R.string.categorie_obesite);
        } else if (age < 65) {
            if (imc < 19) return getString(R.string.categorie_insuffisance);
            if (imc < 25.9) return getString(R.string.categorie_normal);
            if (imc < 30.9) return getString(R.string.categorie_surpoids);
            return getString(R.string.categorie_obesite);
        } else {
            if (imc < 21) return getString(R.string.categorie_insuffisance);
            if (imc < 27) return getString(R.string.categorie_normal);
            if (imc < 32) return getString(R.string.categorie_surpoids);
            return getString(R.string.categorie_obesite);
        }
    }

    private void ouvrirCalendrier() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    dateInputText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void envoyerEmail() {
        String body;

        if (dernierImc > 0) {
            body = getString(R.string.format_email_imc, String.format(java.util.Locale.FRANCE, "%.2f", dernierImc));
        } else {
            body = getString(R.string.email_imc_vide);
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "IMC");
        intent.putExtra(Intent.EXTRA_TEXT, body);

        startActivity(intent);
    }

    private void calculNutrition() {
        Intent intent = new Intent(CalculActivity.this, NutritionMainActivity.class);
        startActivity(intent);
    }

    private void afficherDialogRaz() {
        new AlertDialog.Builder(this)
                .setTitle("Remise à zéro")
                .setMessage("Voulez-vous vraiment réinitialiser les champs ?")
                .setPositiveButton("Oui", (dialog, which) -> razChamps())
                .setNegativeButton("Non", null)
                .show();
    }

    private void razChamps() {
        spinnerGenre.setSelection(0);
        dateInputText.setText("");
        poidsInputText.setText("");
        tailleInputText.setText("");
        radioCentimetre.setChecked(true);
        checkAffichage.setChecked(false);
        resultatText.setText(RESULTAT_DEFAUT);
        dernierImc = -1;
    }

    private void retournerAccueil() {
        Intent resultIntent = new Intent();

        if (dernierImc > 0) {
            resultIntent.putExtra("imc", String.format("%.2f", dernierImc));
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}