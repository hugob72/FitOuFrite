package com.example.fitoufrite;

public enum TypeRepas {
    PETIT_DEJEUNER("Petit-déjeuner"),
    DEJEUNER("Déjeuner"),
    DINER("Dîner"),
    EXTRA("Extra");

    // Variable pour stocker le joli texte
    private final String nomAffichage;

    TypeRepas(String nomAffichage) {
        this.nomAffichage = nomAffichage;
    }

    @Override
    public String toString() {
        return nomAffichage;
    }
}
