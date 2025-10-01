package com.lovai.lovaiapi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppUrlLogger implements CommandLineRunner {
    @Override
    public void run(String... args) {
        String port = System.getProperty("server.port", "8080");
        String contextPath = System.getProperty("server.servlet.context-path", "");
        System.out.println("Application started at: http://localhost:" + port + contextPath);
    }
}
