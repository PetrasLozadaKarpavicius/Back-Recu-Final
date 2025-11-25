package org.example;

import jakarta.persistence.EntityManager;
import org.example.domain.Genre;
import org.example.infra.CsvLoader;
import org.example.infra.DbInitializer;
import org.example.infra.LocalEntityManagerProvider;

import java.util.List;

public class App {

    public static void main(String[] args) {
        try {
            DbInitializer.init();
            var emf = LocalEntityManagerProvider.get();
            try (EntityManager em = emf.createEntityManager()) {
                CsvLoader loader = new CsvLoader(em);
                CsvLoader.Stats stats = loader.loadFromClasspath("DATA/playlists.csv");

                mostrarResultadosImportacion(stats);
                mostrarTopPromedios(em);
                mostrarPlaylistsSoloJazz(em);
            }
            System.out.println("[OK] Proceso finalizado correctamente");
        } catch (Exception e) {
            System.out.println("[FAIL] Error en inicialización/carga CSV");
            e.printStackTrace();
        }
    }

    private static void mostrarResultadosImportacion(CsvLoader.Stats stats) {
        System.out.println("1) RESULTADOS DE LA IMPORTACIÓN");
        System.out.println("----------------------------------------");
        System.out.printf("Tracks insertados      : %d%n", stats.insertedTracks);
        System.out.printf("Playlists insertadas   : %d%n", stats.insertedPlaylists);
        System.out.printf("Álbums insertados      : %d%n", stats.insertedAlbums);
        /*if (stats.missingRequiredRows > 0) {
            System.out.printf("Filas omitidas por campos obligatorios incompletos: %d%n",
                    stats.missingRequiredRows);
        }*/
        if (!stats.reasons.isEmpty()) {
            System.out.println("Detalle de otros descartes:");
            stats.reasons.stream()
                    .sorted()
                    .forEach(r -> System.out.println(" - " + r));
        }
        System.out.println();
    }

   /* private static void mostrarTopPromedios(EntityManager em) {
        System.out.println("2) TOP 5 PLAYLISTS CON MAYOR PROMEDIO COSTO/MINUTO");
        System.out.println("----------------------------------------");
        List<Object[]> top = em.createQuery("""
                        SELECT p.name,
                               AVG(t.unitPrice / (t.milliseconds / 60000.0))
                        FROM PlaylistTrack pt
                        JOIN pt.playlist p
                        JOIN pt.track t
                        WHERE t.milliseconds > 0
                        GROUP BY p.name
                        ORDER BY AVG(t.unitPrice / (t.milliseconds / 60000.0)) DESC
                        """, Object[].class)
                .setMaxResults(5)
                .getResultList();
        if (top.isEmpty()) {
            System.out.println("No se registraron playlists con tracks válidos.");
        } else {
            top.forEach(row -> {
                String name = (String) row[0];
                double avg = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
                System.out.printf(" - %s: %.2f u$s/min%n", name, avg);
            });
        }
        System.out.println();
    }*/ //Corregido el cálculo del promedio según indicaciones

    private static void mostrarTopPromedios(EntityManager em) {
        System.out.println("2) TOP 5 PLAYLISTS CON MAYOR PROMEDIO COSTO/MINUTO");
        System.out.println("----------------------------------------");

        List<Object[]> top = em.createQuery("""
                    SELECT p.name,
                           SUM(t.unitPrice) / SUM(t.milliseconds / 60000.0)
                    FROM PlaylistTrack pt
                    JOIN pt.playlist p
                    JOIN pt.track t
                    WHERE t.milliseconds > 0
                    GROUP BY p.name
                    ORDER BY (SUM(t.unitPrice) / SUM(t.milliseconds / 60000.0)) DESC
                    """, Object[].class)
                .setMaxResults(5)
                .getResultList();

        if (top.isEmpty()) {
            System.out.println("No se registraron playlists con tracks válidos.");
        } else {
            top.forEach(row -> {
                String name = (String) row[0];
                double avg = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
                System.out.printf(" - %s: %.2f u$s/min%n", name, avg);
            });
        }
        System.out.println();
    }




    private static void mostrarPlaylistsSoloJazz(EntityManager em) {
        System.out.println("3) PLAYLISTS CON SOLO TRACKS DEL GÉNERO 2 (JAZZ)");
        System.out.println("----------------------------------------");
        Integer jazzGenreId = resolverGeneroJazz(em);
        long cantidad = jazzGenreId == null ? 0 : contarPlaylistsSoloGenero(em, jazzGenreId);
        System.out.printf("Cantidad: %d%n", cantidad);
    }

    private static Integer resolverGeneroJazz(EntityManager em) {
        Genre genreById = em.find(Genre.class, 2);
        if (genreById != null && "JAZZ".equalsIgnoreCase(genreById.getName())) {
            return genreById.getGenreId();
        }
        return em.createQuery("SELECT g FROM Genre g WHERE UPPER(g.name) = 'JAZZ'", Genre.class)
                .getResultStream()
                .findFirst()
                .map(Genre::getGenreId)
                .orElse(null);
    }

    private static long contarPlaylistsSoloGenero(EntityManager em, Integer genreId) {
        return em.createQuery("""
                        SELECT COUNT(p)
                        FROM Playlist p
                        WHERE EXISTS (
                            SELECT 1 FROM PlaylistTrack pt
                            WHERE pt.playlist = p
                        )
                        AND NOT EXISTS (
                            SELECT 1 FROM PlaylistTrack pt
                            WHERE pt.playlist = p
                            AND (pt.track.genre IS NULL OR pt.track.genre.genreId <> :genreId)
                        )
                        """, Long.class)
                .setParameter("genreId", genreId)
                .getSingleResult();
    }
}

