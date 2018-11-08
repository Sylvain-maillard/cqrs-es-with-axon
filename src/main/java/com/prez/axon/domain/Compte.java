package com.prez.axon.domain;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Slf4j
@Aggregate
public class Compte {

    @AggregateIdentifier
    private String numero;

    @Value
    public static class OuvrirCompteCommand {
        @TargetAggregateIdentifier
        String numero;
        int soldeInitial;
        int limiteDébit;
    }

    @Value
    public static class CompteOuvertEvent {

        String numero;
        int soldeInitial;
        int limiteDébit;
    }


    @CommandHandler
    Compte(OuvrirCompteCommand compteCommand) {
        log.debug("handler commande ouvrir compte commande.");
        apply(new CompteOuvertEvent(compteCommand.numero, compteCommand.soldeInitial, compteCommand.limiteDébit));
    }

    @EventSourcingHandler
    public void on(CompteOuvertEvent event) {
        this.numero = event.numero;
        log.debug("Created new account with number {}", this.numero);
    }
}
