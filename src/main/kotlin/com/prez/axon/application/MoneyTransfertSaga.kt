package com.prez.axon.application

import com.prez.axon.domain.*
import com.prez.axon.infrastructure.Logger.log
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired


@Saga
class MoneyTransfertSaga {

    @Autowired
    @Transient lateinit var commandGateway: CommandGateway

    lateinit var transactionId: String;
    lateinit var sourceNumero: String
    lateinit var cibleNumero: String
    var montant: Int = 0

    @StartSaga
    @SagaEventHandler(associationProperty = "transactionId")
    fun on(event: TransactionInitialisée) {
        // créer une transaction.
        transactionId = event.transactionId;
        sourceNumero = event.sourceNumero;
        cibleNumero = event.cibleNumero;
        montant = event.montant

        SagaLifecycle.associateWith("sourceNumero", sourceNumero)
        SagaLifecycle.associateWith("cibleNumero", cibleNumero)

        log.debug("Init transaction de ${montant} de ${sourceNumero} vers ${cibleNumero}")

        // lancer une commande de crédit sur l'autre compte.
        commandGateway.sendAndWait<Any>(DébiteCompte(event.sourceNumero, event.montant))
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "numero", keyName = "sourceNumero")
    fun on(event: DébitRefusé) {
        log.error { "Impossible de faire le transfert: ${event.raison}" }
    }

    @SagaEventHandler(associationProperty = "numero", keyName = "sourceNumero")
    fun on(event: CompteDebité) {
        log.debug { "compte débité" }

        // ok, le compte est débité. crédite l'autre compte.
        commandGateway.sendAndWait<Any>(CréditeCompte(cibleNumero, montant))
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "numero", keyName = "cibleNumero")
    fun on(event: CompteCrédité) {
        log.debug("Done transfert de ${montant} de ${sourceNumero} vers ${cibleNumero}")
    }
}
