package com.prez.axon;

import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.isLive;

@Slf4j
@ToString
@Aggregate
public class Compte {

    enum Etat {
        OUVERT, FERME
    }

    @AggregateIdentifier
    private String numero;
    private int solde;
    private Etat etat;

    public Compte() {}

    @CommandHandler
    public Compte(OuvrirCompteCommand command) {
        apply(new CompteOuvertEvent(command.getNumero(), command.getSoldeInitial()));
    }

    @CommandHandler
    void handle(FermerCompteCommand command) {
        apply(new CompteFerméEvent(command.getNumero()));
    }

    @CommandHandler
    void handle(DébiterCompteCommand command) {
        if (this.solde < command.getMontant()) {
            apply(new DébitImpossibleCarPasDeSousSurLeCompte(this.numero));
        } else {
            apply(new CompteDébitéEvent(command.getNumero(), this.solde - command.getMontant()));
        }
    }

    @EventSourcingHandler
    void on(CompteDébitéEvent event) {
        this.solde = event.getNouveauSolde();
        if (isLive()) {
            log.debug("{}", this);
        }
    }

    @EventSourcingHandler
    void on(CompteOuvertEvent event) {
        this.numero = event.getNumero();
        this.solde = event.getSoldeInitial();
        this.etat = Etat.OUVERT;
        if (isLive()) {
            log.debug("{}", this);
        }
    }

    @EventSourcingHandler
    void on(CompteFerméEvent event) {
        this.etat = Etat.FERME;
        if (isLive()) {
            log.debug("Compte fermé: {}", this);
        }
    }

    @CommandHandler
    void handle(CréditerCompteCommand command) {

        // attention, si compte ferme ne peut pas créditer
        if (etat == Etat.FERME) {
            apply(new CréditImpossibleEvent(this.numero, "Le compte est fermé."));
        } else {
            apply(new CompteCréditéEvent(this.numero, this.solde + command.getMontant()));
        }
    }

    @EventSourcingHandler
    private void on(CompteCréditéEvent event) {
        this.solde = event.getNouveauSolde();
        if (isLive()) {
            log.debug("{}", this);
        }
    }

    @CommandHandler
    void handle(DemandeDeVirementCommand command) {
        apply(new VirementDemandéEvent(command.getVirementId(), this.numero, command.getCible(), command.getMontant()));
    }
}

@ToString
@Value
class OuvrirCompteCommand { String numero;  int soldeInitial; }

@Value class FermerCompteCommand {  @TargetAggregateIdentifier String numero; }

@Value class CompteFerméEvent { String numero; }

@ToString @Value
class CompteOuvertEvent { String numero; int soldeInitial; }

@Value
class DébiterCompteCommand { @TargetAggregateIdentifier String numero; int montant; }

@ToString @Value
class CompteDébitéEvent { String numero; int nouveauSolde; }

@Value
class CréditerCompteCommand { @TargetAggregateIdentifier String numero; int montant; }

@Value
class CompteCréditéEvent { String numero; int nouveauSolde; }

@Value
class DébitImpossibleCarPasDeSousSurLeCompte { String numero; }

@Value
class CréditImpossibleEvent { String numero; String raison; }

@Value
class DemandeDeVirementCommand { @TargetAggregateIdentifier String source, cible, virementId; int montant; }

@Value
class VirementDemandéEvent { String id, source, cible; int montant; }