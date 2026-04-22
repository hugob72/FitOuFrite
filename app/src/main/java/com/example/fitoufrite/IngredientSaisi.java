package com.example.fitoufrite;

public class IngredientSaisi {
    private Ingredient ingredient;
    private double quantiteEnGrammes;

    public IngredientSaisi(Ingredient ingredient, double quantiteEnGrammes) {
        this.ingredient = ingredient;
        this.quantiteEnGrammes = quantiteEnGrammes;
    }

    public double getProteinesTotales() {
        return (ingredient.getProteines() * quantiteEnGrammes) / 100.0;
    }

    public double getGlucidesTotaux() {
        return (ingredient.getGlucides() * quantiteEnGrammes) / 100.0;
    }

    public double getLipidesTotaux() {
        return (ingredient.getLipides() * quantiteEnGrammes) / 100.0;
    }

    public double getKcalTotales() {
        return (ingredient.getKcal() * quantiteEnGrammes) / 100.0;
    }
}