package com.prez.axon

import org.springframework.boot.SpringApplication.run
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Bank

fun main(vararg args: String) {
    run(Bank::class.java, *args)
}
