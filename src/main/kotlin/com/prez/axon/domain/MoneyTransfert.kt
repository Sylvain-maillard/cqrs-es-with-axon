package com.prez.axon.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class TransactionInitialisée(val transactionId: String = UUID.randomUUID().toString(), val sourceNumero: String, val cibleNumero: String, val montant: Int)

data class StartTransaction(val transactionId: String = UUID.randomUUID().toString(), @TargetAggregateIdentifier val sourceNumero: String, val cibleNumero: String, val montant: Int)