package com.example.fitoufrite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    // Image animée
    private ImageView logoImage;

    // Champ de saisie du prénom
    private EditText prenomInputText;

    // Bouton Calculer
    private Button buttonCalcul;

    // Textes de la zone d'affichage
    private TextView bonjourText;
    private TextView imcText;
    private TextView questionText;

    // Handler pour gérer l'animation
    private final Handler handler = new Handler(Looper.getMainLooper());

    // SharedPreferences : nom du fichier et clés de stockage
    private static final String PREF_NAME = "FitOuFritePrefs";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_IMC = "dernier_imc";

    // Animation du logo : yeux ouverts / yeux fermés
    private final Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {

            // Yeux fermés
            logoImage.setImageResource(R.drawable.android_close);

            // Après 150 ms, retour aux yeux ouverts
            handler.postDelayed(() -> {
                logoImage.setImageResource(R.drawable.android_open);
            }, 150);

            // Recommencer toutes les 2 secondes
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        // Liaison entre le Java et le XML
        logoImage = findViewById(R.id.logoImage);
        prenomInputText = findViewById(R.id.prenomInputText);
        buttonCalcul = findViewById(R.id.buttonCalcul);

        bonjourText = findViewById(R.id.bonjourText);
        imcText = findViewById(R.id.imcText);
        questionText = findViewById(R.id.questionText);

        // Image affichée au départ
        logoImage.setImageResource(R.drawable.android_open);

        // Démarrage de l'animation
        handler.postDelayed(blinkRunnable, 2000);

        // Ouverture du stockage SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Récupération des données sauvegardées
        String prenomSauvegarde = preferences.getString(KEY_PRENOM, "");
        String imcSauvegarde = preferences.getString(KEY_IMC, "");

        // Si un prénom existe déjà, on le remet dans le champ
        if (!prenomSauvegarde.isEmpty()) {
            prenomInputText.setText(prenomSauvegarde);
        }

        // Mise à jour de la zone d'affichage au lancement
        afficherMessage(prenomSauvegarde, imcSauvegarde);

        // Le bouton est actif uniquement si le prénom n'est pas vide
        buttonCalcul.setEnabled(!prenomInputText.getText().toString().trim().isEmpty());

        // Surveillance du champ prénom
        prenomInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Rien à faire ici
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String prenomActuel = s.toString().trim();

                // Activer / désactiver le bouton selon la saisie
                buttonCalcul.setEnabled(!prenomActuel.isEmpty());

                // Récupérer l'IMC déjà sauvegardé s'il existe
                SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                String imcActuel = preferences.getString(KEY_IMC, "");

                // Mettre à jour la zone d'affichage
                afficherMessage(prenomActuel, imcActuel);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Rien à faire ici
            }
        });

        // Action du bouton Calculer
        buttonCalcul.setOnClickListener(v -> {
            String prenomActuel = prenomInputText.getText().toString().trim();

            SharedPreferences preferencesClick = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferencesClick.edit();

            // Sauvegarde du prénom
            editor.putString(KEY_PRENOM, prenomActuel);

            /*
             * TEMPORAIRE POUR TESTER L'AFFICHAGE DE L'IMC :
             * editor.putString(KEY_IMC, "23.6");
             */

            editor.apply();

            // Relire l'IMC sauvegardé
            String imcActuel = preferencesClick.getString(KEY_IMC, "");

            // Mettre à jour la zone d'affichage
            afficherMessage(prenomActuel, imcActuel);

            // Ouvrir la deuxième activité
            Intent intent = new Intent(MainActivity.this, CalculActivity.class);
            startActivity(intent);
        });

        // Code généré par Android Studio pour gérer les barres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Met à jour la zone d'affichage.
     *
     * Cas 1 : aucun prénom saisi
     * -> demande de saisir le prénom.
     *
     * Cas 2 : prénom saisi mais aucun IMC sauvegardé
     * -> affiche Bonjour + prénom et indique qu'aucun IMC précédent n'existe.
     *
     * Cas 3 : prénom + IMC sauvegardé
     * -> affiche Bonjour + prénom et l'ancien IMC.
     */
    private void afficherMessage(String prenom, String imc) {

        if (prenom == null || prenom.trim().isEmpty()) {
            bonjourText.setText("");
            imcText.setText("Veuillez saisir votre prénom.");
            questionText.setText("");
            return;
        }

        bonjourText.setText("Bonjour " + prenom);

        if (imc == null || imc.trim().isEmpty()) {
            imcText.setText("Aucun IMC précédent enregistré.");
            questionText.setText("Voulez-vous le calculer ?");
        } else {
            imcText.setText("Votre dernier IMC était de " + imc);
            questionText.setText("Voulez-vous le recalculer ?");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Arrêter l'animation quand l'activité est détruite
        handler.removeCallbacks(blinkRunnable);
    }
}