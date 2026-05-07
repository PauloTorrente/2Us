package com.coupleapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CoupleAppApplication {

    public static void main(String[] args) {
        // Carregar .env primeiro (se existir)
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            dotenv.entries().forEach(entry -> 
                System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            System.err.println("⚠️  Arquivo .env não encontrado, usando variáveis de ambiente do sistema");
        }

        SpringApplication.run(CoupleAppApplication.class, args);
    }
}
