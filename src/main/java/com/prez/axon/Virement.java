package com.prez.axon;

import lombok.ToString;
import lombok.Value;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class Virement {

    private String id;
    private String source;
    private String cible;
    private int montant;
    private Etat etat;

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient EventBus eventBus;

    public Virement() {}

    @StartSaga
    @SagaEventHandler(associationProperty = "source")
    void on(VirementDemandéEvent event) {
        this.id = event.getId();
        this.source = event.getSource();
        this.cible = event.getCible();
        this.montant = event.getMontant();
        this.etat = Etat.DEMARRE;

        SagaLifecycle.associateWith("compte_source", this.source);
        SagaLifecycle.associateWith("compte_cible", this.cible);

        commandGateway.sendAndWait(new DébiterCompteCommand(source, montant));
    }

    @SagaEventHandler(associationProperty = "numero", keyName = "compte_source")
    void on(CompteDébitéEvent event) {
        commandGateway.sendAndWait(new CréditerCompteCommand(cible, montant));
    }

    @SagaEventHandler(associationProperty = "numero", keyName = "compte_cible")
    void on(CréditImpossibleEvent event) {
        this.etat = Etat.ANNULATION_EN_COURS;
        commandGateway.sendAndWait(new CréditerCompteCommand(this.source, this.montant));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "numero", keyName = "compte_source")
    void on(DébitImpossibleCarPasDeSousSurLeCompte event) {
        this.etat = Etat.EN_ERREUR;
        eventBus.publish(GenericEventMessage.asEventMessage(new VirementTerminéEvent(this.id, this.etat)));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "numero", keyName = "compte_source")
    void onSourceCredit(CompteCréditéEvent event) {
        this.etat = Etat.ANNULE;
        eventBus.publish(GenericEventMessage.asEventMessage(new VirementTerminéEvent(this.id, this.etat)));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "numero", keyName = "compte_cible")
    void on(CompteCréditéEvent event) {
        this.etat = Etat.SUCCES;
        eventBus.publish(GenericEventMessage.asEventMessage(new VirementTerminéEvent(this.id, this.etat)));
    }

    enum Etat {
        DEMARRE, SUCCES, EN_ERREUR, ANNULATION_EN_COURS, ANNULE
    }
}

@Value @ToString
class VirementTerminéEvent { String id; Virement.Etat etat; }
