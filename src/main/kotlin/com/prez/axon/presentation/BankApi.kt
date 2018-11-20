package com.prez.axon.presentation

import com.prez.axon.domain.*
import com.prez.axon.infrastructure.Logger.log
import com.prez.axon.views.CompteView
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BankApi(private val commandGateway: CommandGateway,
              private val queryGateway: QueryGateway) : CommandLineRunner {

    override fun run(vararg args: String) {
        println("Hello Axon World")
        val commandResult = commandGateway.sendAndWait<Any>(OuvrirCompteCommand(numero = "123456", soldeInitial = 10))
        log.debug("compte ouvert avec l'id: $commandResult")

        commandGateway.sendAndWait<Any>(OuvrirCompteCommand(numero = "023456", soldeInitial = 10))
        commandGateway.sendAndWait<Any>(OuvrirCompteCommand(numero = "789456", soldeInitial = 10))
        commandGateway.sendAndWait<Any>(OuvrirCompteCommand(numero = "564456", soldeInitial = 10))

        val compte1 = queryGateway.query(CompteQuery("123456"), CompteView::class.java).get();
        println(compte1)

        // crédite le compte:
        commandGateway.sendAndWait<Any>(CréditeCompte(numero = "123456", montant = 150))
        // débite le compte.
        commandGateway.sendAndWait<Any>(DébiteCompte(numero = "123456", montant = 50))

        var allComptes = queryGateway.query(AllCompteQuery(), ResponseTypes.multipleInstancesOf(CompteView::class.java)).get();

        println(allComptes.joinToString(separator = "\n"))

        // débit le compte A, crédite le compte B.
        commandGateway.sendAndWait<Any>(OuvrirCompteCommand(numero = "A", soldeInitial = 150))
        commandGateway.sendAndWait<Any>(OuvrirCompteCommand(numero = "B", soldeInitial = 0))

        commandGateway.sendAndWait<Any>(StartTransaction(sourceNumero = "A", cibleNumero = "B", montant = 10))

        allComptes = queryGateway.query(AllCompteQuery(), ResponseTypes.multipleInstancesOf(CompteView::class.java)).get();

        println(allComptes.joinToString(separator = "\n"))
    }
}
