package com.prez.axon.api;

public class VirementInput {
    private final String id;
    private final String source;
    private final String cible;
    private final int montant;

    public VirementInput(String id, String source, String cible, int montant) {
        this.id = id;
        this.source = source;
        this.cible = cible;
        this.montant = montant;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getCible() {
        return cible;
    }

    public int getMontant() {
        return montant;
    }
}
