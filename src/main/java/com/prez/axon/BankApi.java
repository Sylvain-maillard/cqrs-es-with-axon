package com.prez.axon;

import com.prez.axon.api.CompteInput;
import com.prez.axon.api.VirementInput;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class BankApi {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;

    public BankApi(QueryGateway queryGateway, CommandGateway commandGateway) {
        this.queryGateway = queryGateway;
        this.commandGateway = commandGateway;
    }

    @PostMapping("/comptes")
    public ResponseEntity<?> ouvrirCompte(CompteInput compteInput) {
        this.commandGateway.sendAndWait(new OuvrirCompteCommand(compteInput.getNumero(), compteInput.getSoldeInitial()));
        return ResponseEntity.created(URI.create("/comptes/" + compteInput.getNumero())).build();
    }

    @PutMapping("/comptes/{numero}/fermer")
    public ResponseEntity<?> fermerCompte(@PathVariable("numero") String numero) {
        this.commandGateway.sendAndWait(new FermerCompteCommand(numero));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/comptes")
    public ResponseEntity<List<CompteVue>> allComptes() throws ExecutionException, InterruptedException {
        List<CompteVue> c = this.queryGateway.query(new CompteQueryAll(), ResponseTypes.multipleInstancesOf(CompteVue.class)).get();
        if (c != null) {
            return ResponseEntity.ok(c);
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/comptes/{numero}")
    public ResponseEntity<CompteVue> getCompte(@PathVariable("numero") String numero) throws ExecutionException, InterruptedException {
        CompteVue compteVue = this.queryGateway.query(new CompteQueryParNumero(numero), CompteVue.class).get();
        if (compteVue != null) {
            return ResponseEntity.ok(compteVue);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity demandeVirement(VirementInput virementInput) {
        this.commandGateway.sendAndWait(new DemandeDeVirementCommand(virementInput.getSource(), virementInput.getCible(), virementInput.getId(), virementInput.getMontant()));
        return ResponseEntity.created(URI.create("/virements/" + virementInput.getId())).build();
    }

    @GetMapping("/virements/{id}")
    public ResponseEntity<VirementVue> getVirement(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        VirementVue virementVue = this.queryGateway.query(new VirementQueryParId(id), VirementVue.class).get();
        if (virementVue != null) {
            return ResponseEntity.ok(virementVue);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}