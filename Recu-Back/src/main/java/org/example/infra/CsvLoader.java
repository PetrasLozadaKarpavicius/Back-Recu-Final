package org.example.infra;

import com.opencsv.CSVReader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.domain.Album;
import org.example.domain.Artist;
import org.example.domain.Genre;
import org.example.domain.MediaType;
import org.example.domain.Track;
import org.example.repo.AlbumRepository;
import org.example.repo.ArtistRepository;
import org.example.repo.TrackRepository;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loader adaptado al dominio musical del pre-enunciado.
 * CSV esperado: artistName,albumTitle,trackName,mediaType,genre,composer,milliseconds,bytes,unitPrice
 */
public class CsvLoader {

    private final EntityManager em;
    private final ArtistRepository artistRepo;
    private final AlbumRepository albumRepo;
    private final TrackRepository trackRepo;

    private final Map<String, Artist> artistCache = new HashMap<>();
    private final Map<String, Album> albumCache = new HashMap<>();
    private final Map<String, MediaType> mediaTypeCache = new HashMap<>();
    private final Map<String, Genre> genreCache = new HashMap<>();
    private final Set<String> trackKeys = new HashSet<>();

    public CsvLoader(EntityManager em) {
        this.em = em;
        this.artistRepo = new ArtistRepository(em);
        this.albumRepo = new AlbumRepository(em);
        this.trackRepo = new TrackRepository(em);
    }

    public Stats loadFromClasspath(String classpath) throws Exception {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(classpath.startsWith("/") ? classpath.substring(1) : classpath)) {
            if (in == null) throw new IllegalStateException("No se encontró en classpath: " + classpath);
            return load(in);
        }
    }

    public Stats load(InputStream csvStream) throws Exception {
        Stats st = new Stats();
        var tx = em.getTransaction();
        tx.begin();
        try (CSVReader reader = new CSVReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String[] row;
            boolean header = true;
            while ((row = reader.readNext()) != null) {
                if (header) { header = false; continue; }
                st.totalRows++;

                String artistName = str(row, 0);
                String albumTitle = str(row, 1);
                String trackName = str(row, 2);
                String mediaTypeName = str(row, 3);
                String genreName = str(row, 4);
                String composer = str(row, 5);
                String millisecondsS = str(row, 6);
                String bytesS = str(row, 7);
                String unitPriceS = str(row, 8);

                if (isBlank(artistName) || isBlank(albumTitle) || isBlank(trackName)) {
                    st.skipped++; st.reasons.add("Fila " + st.totalRows + ": datos básicos incompletos");
                    continue;
                }

                String key = (artistName + "|" + albumTitle + "|" + trackName).toUpperCase(Locale.ROOT);
                if (!trackKeys.add(key)) {
                    st.skipped++; st.reasons.add("Fila " + st.totalRows + ": duplicado en corrida -> " + trackName);
                    continue;
                }

                Integer millis = parsePositiveInt(millisecondsS, "MILLISECONDS");
                if (millis == null) {
                    st.skipped++; st.reasons.add("Fila " + st.totalRows + ": duración inválida");
                    continue;
                }
                Integer bytes = parseInt(bytesS);
                if (bytes != null && bytes < 0) {
                    bytes = null;
                    st.fixedBytes++;
                }
                BigDecimal unitPrice = parsePrice(unitPriceS);
                if (unitPrice == null) {
                    st.skipped++; st.reasons.add("Fila " + st.totalRows + ": precio inválido");
                    continue;
                }

                try {
                    Artist artist = getOrCreateArtist(artistName);
                    Album album = getOrCreateAlbum(albumTitle, artist);
                    MediaType mediaType = getOrCreateMediaType(safeName(mediaTypeName));
                    Genre genre = isBlank(genreName) ? null : getOrCreateGenre(safeName(genreName));

                    if (trackRepo.findByNameAndAlbum(trackName, album) != null) {
                        st.skipped++; st.reasons.add("Fila " + st.totalRows + ": track ya existe en BD");
                        continue;
                    }

                    Track track = new Track();
                    track.setName(trackName.trim());
                    track.setAlbum(album);
                    track.setMediaType(mediaType);
                    track.setGenre(genre);
                    track.setComposer(composer);
                    track.setMilliseconds(millis);
                    track.setBytes(bytes);
                    track.setUnitPrice(unitPrice);

                    em.persist(track);
                    st.inserted++;
                } catch (RuntimeException ex) {
                    st.skipped++;
                    st.reasons.add("Fila " + st.totalRows + ": error al persistir (" + ex.getMessage() + ")");
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
        return st;
    }

    private Artist getOrCreateArtist(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        if (artistCache.containsKey(key)) return artistCache.get(key);
        Artist existing = artistRepo.findbyName(name);
        if (existing == null) {
            existing = new Artist();
            existing.setName(name.trim());
            em.persist(existing);
        }
        artistCache.put(key, existing);
        return existing;
    }

    private Album getOrCreateAlbum(String title, Artist artist) {
        String key = (artist.getArtistid() + "|" + title.toUpperCase(Locale.ROOT));
        if (albumCache.containsKey(key)) return albumCache.get(key);
        Album album = albumRepo.getOrCreate(title.trim(), artist);
        albumCache.put(key, album);
        return album;
    }

    private MediaType getOrCreateMediaType(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        if (mediaTypeCache.containsKey(key)) return mediaTypeCache.get(key);
        MediaType mt = findMediaType(name);
        if (mt == null) {
            mt = new MediaType();
            mt.setName(name);
            em.persist(mt);
        }
        mediaTypeCache.put(key, mt);
        return mt;
    }

    private MediaType findMediaType(String name) {
        TypedQuery<MediaType> query = em.createQuery(
                "SELECT m FROM MediaType m WHERE UPPER(m.name) = :name", MediaType.class);
        List<MediaType> list = query.setParameter("name", name.toUpperCase(Locale.ROOT)).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private Genre getOrCreateGenre(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        if (genreCache.containsKey(key)) return genreCache.get(key);
        Genre genre = findGenre(name);
        if (genre == null) {
            genre = new Genre();
            genre.setName(name);
            em.persist(genre);
        }
        genreCache.put(key, genre);
        return genre;
    }

    private Genre findGenre(String name) {
        TypedQuery<Genre> query = em.createQuery(
                "SELECT g FROM Genre g WHERE UPPER(g.name) = :name", Genre.class);
        List<Genre> list = query.setParameter("name", name.toUpperCase(Locale.ROOT)).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private static String str(String[] row, int idx) {
        if (row == null || idx >= row.length) return null;
        String value = row[idx];
        return value == null ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static Integer parsePositiveInt(String value, String field) {
        Integer parsed = parseInt(value);
        if (parsed == null || parsed <= 0) {
            return null;
        }
        return parsed;
    }

    private static Integer parseInt(String value) {
        if (isBlank(value)) return null;
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parsePrice(String value) {
        if (isBlank(value)) return null;
        try {
            BigDecimal price = new BigDecimal(value.replace(',', '.').trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) return null;
            if (price.compareTo(new BigDecimal("9999.99")) > 0) price = new BigDecimal("9999.99");
            return price.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String safeName(String value) {
        if (isBlank(value)) return "Unknown";
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "Unknown" : trimmed;
    }

    public static class Stats {
        public int totalRows;
        public int inserted;
        public int skipped;
        public int fixedBytes;
        public List<String> reasons = new ArrayList<>();

        @Override
        public String toString() {
            return "Stats{rows=" + totalRows + ", inserted=" + inserted + ", skipped=" + skipped +
                    ", fixedBytes=" + fixedBytes + "}";
        }
    }
}

