package com.example.fitoufrite;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GestionnaireRepas {

    private static final String PREF_NAME = "FitOuFriteData";
    private static final String KEY_REPAS = "mes_repas_sauvegardes";

    /** Sauvegarde la liste complète des repas */
    public static void sauvegarderListe(Context context, List<Repas> listeRepas) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // On transforme la liste d'objets Java en texte JSON
        Gson gson = new Gson();
        String jsonText = gson.toJson(listeRepas);

        editor.putString(KEY_REPAS, jsonText);
        editor.apply();
    }

    /** Charge la liste des repas */
    public static List<Repas> chargerListe(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonText = prefs.getString(KEY_REPAS, null);

        if (jsonText == null) {
            return new ArrayList<>();
        }

        // On transforme le texte JSON en vrai liste d'objets Java
        Gson gson = new Gson();
        Type typeDeLaListe = new TypeToken<ArrayList<Repas>>() {}.getType();

        return gson.fromJson(jsonText, typeDeLaListe);
    }
}