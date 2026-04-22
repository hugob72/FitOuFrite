package com.example.fitoufrite;

public class Ingredient {
    private String nom;
    private double proteines;
    private double glucides;
    private double lipides;
    private double kcal;
    private String nutriScore;

    public Ingredient(String nom, double proteines, double glucides, double lipides, double kcal, String nutriScore) {
        this.nom = nom;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.kcal = kcal;
        this.nutriScore = nutriScore;
    }

    public String getNom() { return nom; }
    public double getProteines() { return proteines; }
    public double getGlucides() { return glucides; }
    public double getLipides() { return lipides; }
    public double getKcal() { return kcal; }
    public String getNutriScore() { return nutriScore; }
}