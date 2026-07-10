package com.example.AUHT_SERVICE.CONFIG;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j; // Para trazabilidad
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@Slf4j // Agregamos logs para auditoría de inicio
public class MailConfiguration {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Bean
    public JavaMailSender javaMailSender() {
        log.info(" Inicializando JavaMailSender para el host: {}", mailHost);

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);
        mailSender.setDefaultEncoding("UTF-8"); // Asegura que caracteres como 'ñ' o tildes se vean bien

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        // Protocolos modernos y seguros
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", mailHost); // Más seguro que "*"

        // Timeouts críticos para evitar bloqueos del hilo principal
        props.put("mail.smtp.connectiontimeout", "10000"); // 10s para conectar
        props.put("mail.smtp.timeout", "10000");           // 10s para leer
        props.put("mail.smtp.writetimeout", "10000");      // 10s para escribir

        return mailSender;
    }
}