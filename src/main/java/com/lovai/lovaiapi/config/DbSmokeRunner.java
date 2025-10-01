package com.lovai.lovaiapi.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
@Profile({"default","local"})
public class DbSmokeRunner implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public DbSmokeRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        try {
            Integer one = jdbc.queryForObject("select 1", Integer.class);
            Integer tables = jdbc.queryForObject(
                    "select count(*) from information_schema.tables where table_schema = 'public'",
                    Integer.class
            );
            System.out.println("[DB] select 1 = " + one + " | public.tables = " + tables);
        } catch (Exception e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
        }
    }
}
