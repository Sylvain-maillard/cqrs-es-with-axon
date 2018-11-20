package com.prez.axon.views

import com.prez.axon.domain.AllCompteQuery
import com.prez.axon.domain.CompteQuery

data class CompteView (val numero: String, val solde: Int)

interface ComptesViewsRepository {
    fun query(compteQuery: CompteQuery): CompteView
    fun query(allCompteQuery: AllCompteQuery): List<CompteView>
}