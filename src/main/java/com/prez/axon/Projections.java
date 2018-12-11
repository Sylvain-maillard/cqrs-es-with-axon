package com.prez.axon;

import lombok.ToString;
import lombok.Value;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value @ToString class CompteVue { String numero; int solde; String etat; }

@Repository
class ComptesProjection {
    private final Map<String, CompteVue> comptes = new HashMap<>();

    private final QueryUpdateEmitter queryUpdateEmitter;

    ComptesProjection(QueryUpdateEmitter queryUpdateEmitter) {
        this.queryUpdateEmitter = queryUpdateEmitter;
    }

    @EventHandler
    public void on(CompteOuvertEvent event) {
        CompteVue vue = new CompteVue(event.getNumero(), event.getSoldeInitial(), "ouvert");
        comptes.put(event.getNumero(), vue);
        queryUpdateEmitter.emit(CompteQueryParNumero.class, compteQueryParNumero -> compteQueryParNumero.getNumero().equalsIgnoreCase(event.getNumero()), vue);
    }

    @EventHandler
    public void on(CompteFerméEvent event) {
        comptes.compute(event.getNumero(), (s, compteVue) -> new CompteVue(s, compteVue.getSolde(), "fermé"));
    }

    @EventHandler
    public void on(CompteDébitéEvent event) {
        comptes.computeIfPresent(event.getNumero(), (s, vue) -> new CompteVue(s, event.getNouveauSolde(), vue.getEtat()));
    }

    @EventHandler
    public void on(CompteCréditéEvent event) {
        comptes.computeIfPresent(event.getNumero(), (s, vue) -> new CompteVue(s, event.getNouveauSolde(), vue.getEtat()));
    }

    @QueryHandler
    public List<CompteVue> query(CompteQueryAll q) {
        return new ArrayList<>(comptes.values());
    }

    @QueryHandler
    public CompteVue query(CompteQueryParNumero q) {
        return comptes.get(q.getNumero());
    }
}


@Value @ToString class CompteQueryAll { }

@Value @ToString class CompteQueryParNumero { String numero; }

@Value @ToString class VirementVue { String id; Virement.Etat etat; }

@Value @ToString class VirementQueryParId { String id; }

@Repository
class VirementsProjection {
    private Map<String, VirementVue> virements = new HashMap<>();

    @EventHandler
    void on(VirementDemandéEvent event) {
        virements.put(event.getId(), new VirementVue(event.getId(), Virement.Etat.DEMARRE));
    }

    @EventHandler
    void on(VirementTerminéEvent event) {
        virements.compute(event.getId(), (s, virementVue) -> new VirementVue(s, event.getEtat()));
    }

    @QueryHandler
    VirementVue query(VirementQueryParId id) {
        return virements.get(id.getId());
    }
}