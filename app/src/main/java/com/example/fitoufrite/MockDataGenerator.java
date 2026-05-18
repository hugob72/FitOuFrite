package com.example.fitoufrite;

import java.util.ArrayList;
import java.util.List;

public class MockDataGenerator {
    private static List<Repas> historiqueRepas = null;
    /**
     * Génère une liste de 3 repas contenant plusieurs ingrédients saisis.
     */
    public static List<Repas> genererHistoriqueRepas() {

        if (historiqueRepas == null) {
            historiqueRepas = new ArrayList<>();

            // 1. Création de quelques ingrédients de base (Nom, Protéines, Glucides, Lipides, Kcal, NutriScore)
            Ingredient riz = new Ingredient("Riz blanc", 2.7, 28.0, 0.3, 130.0, "A");
            Ingredient poulet = new Ingredient("Blanc de poulet", 23.0, 0.0, 1.5, 110.0, "A");
            Ingredient pain = new Ingredient("Pain blanc", 8.0, 49.0, 1.5, 265.0, "C");
            Ingredient beurre = new Ingredient("Beurre doux", 0.7, 0.6, 81.0, 717.0, "E");
            Ingredient pate = new Ingredient("Pâtes", 5.0, 30.0, 1.0, 150.0, "A");
            Ingredient steak = new Ingredient("Steak haché 5%", 21.0, 0.0, 5.0, 129.0, "A");

            // ---------------------------------------------------------
            // REPAS 1 : Petit-déjeuner
            // ---------------------------------------------------------
            Repas petitDej = new Repas("22/04/2026", TypeRepas.PETIT_DEJEUNER);

            // Ajout des ingrédients directement via la liste initialisée dans le 1er constructeur
            petitDej.getIngredientSaisis().add(new IngredientSaisi(pain, 80.0));   // 80g de pain
            petitDej.getIngredientSaisis().add(new IngredientSaisi(beurre, 15.0)); // 15g de beurre

            historiqueRepas.add(petitDej);

            // ---------------------------------------------------------
            // REPAS 2 : Déjeuner
            // ---------------------------------------------------------
            Repas dejeuner = new Repas("22/04/2026", TypeRepas.DEJEUNER);

            dejeuner.getIngredientSaisis().add(new IngredientSaisi(riz, 150.0));    // 150g de riz
            dejeuner.getIngredientSaisis().add(new IngredientSaisi(poulet, 120.0)); // 120g de poulet

            historiqueRepas.add(dejeuner);

            // ---------------------------------------------------------
            // REPAS 3 : Dîner (en utilisant le 2ème constructeur)
            // ---------------------------------------------------------
            List<IngredientSaisi> ingredientsDiner = new ArrayList<>();
            ingredientsDiner.add(new IngredientSaisi(pate, 200.0));  // 200g de pâtes
            ingredientsDiner.add(new IngredientSaisi(steak, 100.0)); // 100g de steak haché

            Repas diner = new Repas("22/04/2026", TypeRepas.DINER, ingredientsDiner);

            historiqueRepas.add(diner);
        }

        return historiqueRepas;
    }

    public static void ajouterUnRepas(Repas nouveauRepas) {
        historiqueRepas.add(nouveauRepas);
    }

    public static List<Repas> getHistoriqueRepas() {
        return historiqueRepas;
    }
}
