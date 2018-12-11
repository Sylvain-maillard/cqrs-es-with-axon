package com.prez.axon;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.Test;

public class CompteTest {

    AggregateTestFixture<Compte> testFixture = new AggregateTestFixture<>(Compte.class);

    @Test
    public void ouvrir_compte() {
        testFixture.givenNoPriorActivity()
                .when(new OuvrirCompteCommand("123",10))
                .expectEvents(new CompteOuvertEvent("123",10));
    }

    @Test
    public void debit_compte() {
        testFixture.given(new CompteOuvertEvent("123",10))
                .when(new DébiterCompteCommand("123", 10))
                .expectEvents(new CompteDébitéEvent("123",0));
    }

    @Test
    public void debit_dépassant_la_limite_doit_provoquer_une_erreur() {
        testFixture.given(new CompteOuvertEvent("123", 10))
                .when(new DébiterCompteCommand("123", 1000))
                .expectEvents(new DébitImpossibleCarPasDeSousSurLeCompte("123"));
    }
}