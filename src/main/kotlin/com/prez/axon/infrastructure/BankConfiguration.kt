package com.prez.axon.infrastructure

import com.prez.axon.domain.*
import com.prez.axon.infrastructure.Logger.log
import com.prez.axon.views.CompteView
import com.prez.axon.views.ComptesViewsRepository
import org.axonframework.config.Configurer
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.queryhandling.QueryHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Configuration
class BankConfiguration {

    @Autowired
    fun configure(configurer: Configurer) {
        configurer.eventProcessing().usingSubscribingEventProcessors()
    }

    @Bean
    internal fun eventStorageEngine(): EventStorageEngine {
        return InMemoryEventStorageEngine()
    }
}

@Repository
class ListeComptesRepository : ComptesViewsRepository {
    private val comptes: MutableMap<String, CompteView> = HashMap()

    @EventHandler
    fun on(event: CompteOuvertEvent) {
        comptes.put(event.numero, CompteView(event.numero, event.soldeInitial))
    }

    @EventHandler
    fun on(event: CompteDebité) {
        comptes.computeIfPresent(event.numero) { key, value -> CompteView(key, value.solde - event.montant) }
    }

    @EventHandler
    fun on(event: CompteCrédité) {
        comptes.computeIfPresent(event.numero) { key, value -> CompteView(key, value.solde + event.montant) }
    }

    @QueryHandler
    override fun query(compteQuery: CompteQuery): CompteView {
        log.debug("query {}", compteQuery.byNumber)
        return comptes.getOrElse(compteQuery.byNumber) { throw IllegalArgumentException("not found.")}
    }

    @QueryHandler
    override fun query(allCompteQuery: AllCompteQuery): List<CompteView> {
        return comptes.values.sortedBy { view -> view.numero }
    }
}


@Component
class LoggingEventHandler {

    @EventHandler
    fun on(event: Any) {
        log.info("event -> {}", event)
    }
}

