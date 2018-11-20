@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package com.prez.axon.domain

import com.prez.axon.infrastructure.Logger.debugLive
import com.prez.axon.infrastructure.Logger.log
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.spring.stereotype.Aggregate
import java.util.*

data class OuvrirCompteCommand(val numero: String = UUID.randomUUID().toString(), val soldeInitial: Int, val limiteDébit: Int = 100)
data class CompteOuvertEvent(val numero: String, val soldeInitial: Int, val limiteDébit: Int)

data class CompteDebité(val numero: String, val montant: Int)
data class DébitRefusé(val numero: String, val raison: String = "pas assez de fond")

data class CompteCrédité(val numero: String, val montant: Int)

data class CréditeCompte(@TargetAggregateIdentifier val numero: String, val montant: Int)
data class DébiteCompte(@TargetAggregateIdentifier val numero: String, val montant: Int)

data class CompteQuery(val byNumber: String)
class AllCompteQuery

@Aggregate
class Compte {

    constructor() {
      //  log.debug("replaying events.")
    }

    @CommandHandler constructor(compteCommand: OuvrirCompteCommand) {
        log.debug("handler commande ouvrir compte commande.")
        apply(CompteOuvertEvent(compteCommand.numero, compteCommand.soldeInitial, compteCommand.limiteDébit))
    }


    @CommandHandler
    fun handle(command: StartTransaction) {
        apply(TransactionInitialisée(command.transactionId, command.sourceNumero, command.cibleNumero, command.montant))
    }

    @AggregateIdentifier
    lateinit var numero: String

    @EventSourcingHandler
    fun on(event: CompteOuvertEvent) {
        this.numero = event.numero
        this.solde = event.soldeInitial;

        debugLive("Création d'un nouveau compte avec le numéro. ${numero}")
    }

    private var solde: Int = 0;

    @CommandHandler
    fun handle(commande: DébiteCompte) {
        // assert y a du pognon:
        if (solde - commande.montant >= 0) {
            // ok le compte est débité:
            apply(CompteDebité(commande.numero, commande.montant))
        } else {
            apply(DébitRefusé(commande.numero))
        }
    }

    @CommandHandler
    fun handle(commande: CréditeCompte) {
        // ok le compte est crédité:
        apply(CompteCrédité(commande.numero, commande.montant))
    }

    @EventSourcingHandler
    fun on(event: CompteDebité) {
        this.solde = this.solde - event.montant;
        debugLive ( "le compte ${numero} est débité de ${event.montant}, nouveau solde: ${solde}" )
    }

    @EventSourcingHandler
    fun on(event: CompteCrédité) {
        this.solde = this.solde + event.montant;
        debugLive ( "le compte ${numero} est crédité de ${event.montant}, nouveau solde: ${solde}" )
    }
}