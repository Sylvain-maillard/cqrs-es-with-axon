package com.prez.axon;

import org.axonframework.test.saga.SagaTestFixture;
import org.junit.Test;

public class VirementTest {

    SagaTestFixture<Virement> testFixture = new SagaTestFixture<>(Virement.class);

    @Test
    public void test_virement() {

        testFixture.givenNoPriorActivity()
                .whenAggregate("A").publishes(new VirementDemandéEvent("0", "123","456",10))
                .expectActiveSagas(1)
                .expectDispatchedCommands(new DébiterCompteCommand("123",10))
                .expectAssociationWith("compte_source","123")
                .expectAssociationWith("compte_cible","456");
    }

    @Test
    public void test_virement_credit() {

        testFixture
                .givenAggregate("A").published(new VirementDemandéEvent("0", "123","456", 10))
                .whenAggregate("A").publishes(new CompteDébitéEvent("123",10))
                .expectAssociationWith("compte_source","123")
                .expectActiveSagas(1)
                .expectDispatchedCommands(new CréditerCompteCommand("456",10));
    }

    @Test
    public void test_virement_termine() {

        testFixture
                .givenAggregate("A").published(new VirementDemandéEvent("0", "123","456", 10))
                .andThenAggregate("A").published(new CompteDébitéEvent("123",0))
                .whenAggregate("B").publishes(new CompteCréditéEvent("456",10))
                .expectNoAssociationWith("compte_cible","456")
                .expectNoAssociationWith("compte_source","123")
                .expectActiveSagas(0)
                .expectNoDispatchedCommands()
                .expectPublishedEvents(new VirementTerminéEvent("0", Virement.Etat.SUCCES));
    }

    @Test
    public void test_virement_credit_en_erreur_sur_le_compte_cible_est_compensé_par_un_credit_sur_le_compte_source() {

        testFixture
                .givenAggregate("A").published(new VirementDemandéEvent("0", "123","456", 10))
                .andThenAggregate("A").published(new CompteDébitéEvent("123",0))
                .whenAggregate("B").publishes(new CréditImpossibleEvent("456","impossible de créditer"))
                .expectDispatchedCommands(new CréditerCompteCommand("123",10));
    }

    @Test
    public void test_virement_annulé() {

        testFixture
                .givenAggregate("A").published(new VirementDemandéEvent("0", "123","456", 10))
                .andThenAggregate("A").published(new CompteDébitéEvent("123",0))
                .andThenAggregate("B").published(new CréditImpossibleEvent("456","impossible de créditer"))
                .whenAggregate("A").publishes(new CompteCréditéEvent("123",10))
                .expectActiveSagas(0)
                .expectNoDispatchedCommands()
                .expectPublishedEvents(new VirementTerminéEvent("0",Virement.Etat.ANNULE));
    }
}