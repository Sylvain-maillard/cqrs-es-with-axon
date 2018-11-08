package com.prez.axon.presentation;

import com.prez.axon.domain.Compte;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BankApi implements CommandLineRunner {

    private final CommandGateway commandGateway;

    public BankApi(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Override
    public void run(String... args) {
        System.out.println("Hello Axon World");
        log.debug("=> debug.");
        log.info("=> info.");
        commandGateway.sendAndWait(new Compte.OuvrirCompteCommand("1423456",10,100));
    }
}
