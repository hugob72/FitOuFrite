package com.example.fitoufrite;

import java.util.ArrayList;
import java.util.List;

public class Repas {

    private String date;

    private TypeRepas typeRepas;

    private List<IngredientSaisi> ingredientSaisis;

    public Repas(String date, TypeRepas typeRepas) {
        this.date = date;
        this.typeRepas = typeRepas;
        this.ingredientSaisis = new ArrayList<>();
    }

    public Repas(String date, TypeRepas typeRepas, List<IngredientSaisi> ingredientSaisis) {
        this.date = date;
        this.typeRepas = typeRepas;
        this.ingredientSaisis = ingredientSaisis;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TypeRepas getTypeRepas() {
        return typeRepas;
    }

    public void setTypeRepas(TypeRepas typeRepas) {
        this.typeRepas = typeRepas;
    }

    public List<IngredientSaisi> getIngredientSaisis() {
        return ingredientSaisis;
    }

    public void setIngredientSaisis(List<IngredientSaisi> ingredientSaisis) {
        this.ingredientSaisis = ingredientSaisis;
    }
}
