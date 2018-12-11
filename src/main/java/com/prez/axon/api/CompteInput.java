package com.prez.axon.api;

public class CompteInput {
    public String getNumero() {
        return numero;
    }

    public int getSoldeInitial() {
        return soldeInitial;
    }

    private final String numero;
    private final int soldeInitial;

    public CompteInput(String numero, int soldeInitial) {
        this.numero = numero;
        this.soldeInitial = soldeInitial;
    }
}
