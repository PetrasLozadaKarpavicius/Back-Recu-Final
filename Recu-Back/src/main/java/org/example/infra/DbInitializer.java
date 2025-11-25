package org.example.infra;

import org.h2.tools.RunScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class DbInitializer {

    public static void init() {
        try (Connection conn = DataSourceProvider.getDataSource().getConnection()) {
            try (InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("sql/database-ddl.sql")) {
                if (in == null) {
                    throw new IllegalStateException("No se encontró sql/database-ddl.sql");
                }
                try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    RunScript.execute(conn, reader);
                }
            }
            System.out.println("[OK] Esquema creado");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[FAIL] " + e.getMessage());
        }
    }
}