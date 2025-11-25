package org.example;

import jakarta.persistence.EntityManager;
import org.example.infra.CsvLoader;
import org.example.infra.DbInitializer;
import org.example.infra.LocalEntityManagerProvider;

public class App {

    public static void main(String[] args) {
        try {
            DbInitializer.init();
            var emf = LocalEntityManagerProvider.get();
            try (EntityManager em = emf.createEntityManager()) {
                CsvLoader loader = new CsvLoader(em);
                CsvLoader.Stats stats = null;

                try (var is = App.class.getResourceAsStream("/data/database.csv")) {
                    if (is == null) {
                        System.out.println("[WARN] No se encontró /data/database.csv en resources");
                    } else {
                        stats = loader.load(is);
                    }
                }

                if (stats != null) {
                    System.out.println("RESULTADO PUNTO UNO: Resumen de importación");
                    System.out.println(stats);
                    if (!stats.reasons.isEmpty()) {
                        System.out.println("[CSV] Líneas inválidas:");
                        stats.reasons.forEach(System.out::println);
                    }
                }

                Long artists = em.createQuery("select count(a) from Artist a", Long.class).getSingleResult();
                Long albums = em.createQuery("select count(a) from Album a", Long.class).getSingleResult();
                Long tracks = em.createQuery("select count(t) from Track t", Long.class).getSingleResult();

                System.out.printf("[OK] Totales -> Artistas: %d, Álbumes: %d, Tracks: %d%n",
                        artists, albums, tracks);
            }
            System.out.println("[OK] H2 + DDL inicializados y mapeos JPA verificados");
        } catch (Exception e) {
            System.out.println("[FAIL] Error en inicialización/carga CSV");
            e.printStackTrace();
        }
    }
}

