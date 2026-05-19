package com.example.fitoufrite;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NutritionReportActivity extends AppCompatActivity {

    private TextView dataConsommeTextView;
    private TextView dataObjectifTextView;
    private Spinner spinnerFiltreTemps;
    private BarChart nutritionBarChart;

    // Valeurs moyennes recommandées par JOUR en grammes
    private final double RECO_KCAL_JOUR = 2000.0;
    private final double RECO_PROT_JOUR = 75.0;
    private final double RECO_GLUC_JOUR = 250.0;
    private final double RECO_LIP_JOUR = 70.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_report);

        dataConsommeTextView = findViewById(R.id.dataConsommeTextView);
        dataObjectifTextView = findViewById(R.id.dateObjectifTextView);
        spinnerFiltreTemps = findViewById(R.id.spinnerFiltreTemps);

        // Configuration du Spinner
        String[] filtres = {"Aujourd'hui (1 jour)", "Cette semaine (7 jours)", "Ce mois (30 jours)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filtres);
        spinnerFiltreTemps.setAdapter(adapter);
        spinnerFiltreTemps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int elementFiltre, long id) {
                int nbJoursMultiplicateur = 1;
                if (elementFiltre == 1) { // Semaine
                    nbJoursMultiplicateur = 7;
                } else if (elementFiltre == 2) { // Mois
                    nbJoursMultiplicateur = 30;
                }
                calculerEtAfficherBilan(nbJoursMultiplicateur);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    /** Cacul et affichage du bilan */
    private void calculerEtAfficherBilan(int nbJours) {
        // 1. Calcul des objectifs (recommandations) selon la période
        double recoKcal = RECO_KCAL_JOUR * nbJours;
        double recoProt = RECO_PROT_JOUR * nbJours;
        double recoGluc = RECO_GLUC_JOUR * nbJours;
        double recoLip = RECO_LIP_JOUR * nbJours;

        String affichageRecommande = String.format("%.0f Kcal\n%.1f g Prot\n%.1f g Gluc\n%.1f g Lip", recoKcal, recoProt, recoGluc, recoLip);
        dataObjectifTextView.setText(affichageRecommande);

        // Définit la date de seuil
        Calendar calendar = Calendar.getInstance();
        int joursARetirer = (nbJours == 1) ? 0 : -nbJours;
        calendar.add(Calendar.DAY_OF_YEAR, joursARetirer);

        // On remet l'heure à 00:00:00 pour prendre toute la journée en compte
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date dateSeuil = calendar.getTime();


        // 2. Calcul des nutriments absorbés
        List<Repas> historique = MockDataGenerator.getHistoriqueRepas();
        double totalKcal = 0;
        double totalProt = 0;
        double totalGluc = 0;
        double totalLip = 0;

        for (Repas repas : historique) {
            if (repas.getDate() != null && !repas.getDate().before(dateSeuil)) {
                for (IngredientSaisi saisi : repas.getIngredientSaisis()) {
                    totalKcal += saisi.getKcalTotales();
                    totalProt += saisi.getProteinesTotales();
                    totalGluc += saisi.getGlucidesTotaux();
                    totalLip += saisi.getLipidesTotaux();
                }
            }
        }

        String affichageAbsorbe = String.format("%.0f Kcal\n%.1f g Prot\n%.1f g Gluc\n%.1f g Lip", totalKcal, totalProt, totalGluc, totalLip);
        dataConsommeTextView.setText(affichageAbsorbe);

        // Mise en place du graphique (partie faite avec l'assistance de l'IA)
        nutritionBarChart = findViewById(R.id.nutritionBarChart);

        // Calcul des pourcentages
        float pctKcal = (float) ((totalKcal / recoKcal) * 100);
        float pctProt = (float) ((totalProt / recoProt) * 100);
        float pctGluc = (float) ((totalGluc / recoGluc) * 100);
        float pctLip = (float) ((totalLip / recoLip) * 100);

        // 2. Création des barres (X, Y)
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, pctKcal));
        entries.add(new BarEntry(1f, pctProt));
        entries.add(new BarEntry(2f, pctGluc));
        entries.add(new BarEntry(3f, pctLip));

        // 3. Configuration du design de l'ensemble de barres
        BarDataSet dataSet = new BarDataSet(entries, "% de l'objectif atteint");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        nutritionBarChart.setData(barData);

        // Axe des abscisses
        String[] labels = new String[]{"Calories", "Protéines", "Glucides", "Lipides"};
        XAxis xAxis = nutritionBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        nutritionBarChart.getDescription().setEnabled(false);
        nutritionBarChart.getAxisRight().setEnabled(false);
        nutritionBarChart.getAxisLeft().setAxisMinimum(0f);
        nutritionBarChart.animateY(1000);
        nutritionBarChart.invalidate();
    }
}