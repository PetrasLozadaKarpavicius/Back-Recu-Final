package org.example.infra;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.domain.Album;
import org.example.domain.Artist;
import org.example.domain.Genre;
import org.example.domain.MediaType;
import org.example.domain.Playlist;
import org.example.domain.Track;
import org.example.repo.AlbumRepository;
import org.example.repo.ArtistRepository;
import org.example.repo.PlaylistRepository;
import org.example.repo.PlaylistTrackRepository;
import org.example.repo.TrackRepository;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loader adaptado al formato del archivo playlists.csv entregado en el enunciado:
 * playListName, trackName, composer, milliseconds, bytes, unitPrice,
 * albumTitle, artistName, genreName, mediaTypeName
 */
public class CsvLoader {

    private final EntityManager em;
    private final ArtistRepository artistRepo;
    private final AlbumRepository albumRepo;
    private final TrackRepository trackRepo;
    private final PlaylistRepository playlistRepo;
    private final PlaylistTrackRepository playlistTrackRepo;

    private final Map<String, Artist> artistCache = new HashMap<>();
    private final Map<String, Album> albumCache = new HashMap<>();
    private final Map<String, MediaType> mediaTypeCache = new HashMap<>();
    private final Map<String, Genre> genreCache = new HashMap<>();
    private final Map<String, Track> trackCache = new HashMap<>();
    private final Map<String, Playlist> playlistCache = new HashMap<>();
    private final Set<String> playlistTrackKeys = new HashSet<>();

    public CsvLoader(EntityManager em) {
        this.em = em;
        this.artistRepo = new ArtistRepository(em);
        this.albumRepo = new AlbumRepository(em);
        this.trackRepo = new TrackRepository(em);
        this.playlistRepo = new PlaylistRepository(em);
        this.playlistTrackRepo = new PlaylistTrackRepository(em);
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
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .build();
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(csvStream, StandardCharsets.UTF_8))
                .withCSVParser(parser)
                .build()) {
            String[] row;
            boolean header = true;
            while ((row = reader.readNext()) != null) {
                if (header) { header = false; continue; }
                st.totalRows++;

                String playlistName = str(row, 0);
                String trackName = str(row, 1);
                String composer = str(row, 2);
                String millisecondsS = str(row, 3);
                String bytesS = str(row, 4);
                String unitPriceS = str(row, 5);
                String albumTitle = str(row, 6);
                String artistName = str(row, 7);
                String genreName = str(row, 8);
                String mediaTypeName = str(row, 9);

                if (hasMissingRequiredField(playlistName, trackName, composer, millisecondsS,
                        bytesS, unitPriceS, albumTitle, artistName, genreName, mediaTypeName)) {
                    st.skippedRows++;
                    st.missingRequiredRows++;
                    continue;
                }

                Integer millis = parsePositiveInt(millisecondsS);
                if (millis == null) {
                    st.skippedRows++; st.reasons.add("Fila " + st.totalRows + ": duración inválida");
                    continue;
                }

                Integer bytes = parseInt(bytesS);
                if (bytes == null) {
                    st.skippedRows++; st.reasons.add("Fila " + st.totalRows + ": bytes inválidos");
                    continue;
                }
                if (bytes < 0) {
                    st.fixedBytes++;
                    bytes = null;
                }

                BigDecimal unitPrice = parsePrice(unitPriceS);
                if (unitPrice == null) {
                    st.skippedRows++; st.reasons.add("Fila " + st.totalRows + ": precio inválido");
                    continue;
                }

                try {
                    Artist artist = getOrCreateArtist(artistName);
                    Album album = getOrCreateAlbum(albumTitle, artist, st);
                    MediaType mediaType = getOrCreateMediaType(mediaTypeName);
                    Genre genre = getOrCreateGenre(genreName);
                    Track track = getOrCreateTrack(trackName, album, mediaType, genre, composer, millis, bytes, unitPrice, st);
                    Playlist playlist = getOrCreatePlaylist(playlistName, st);
                    linkPlaylistTrack(playlist, track, st);
                    st.processedRows++;
                } catch (RuntimeException ex) {
                    st.skippedRows++;
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

    private Album getOrCreateAlbum(String title, Artist artist, Stats stats) {
        String key = title.toUpperCase(Locale.ROOT);
        if (albumCache.containsKey(key)) return albumCache.get(key);
        Album album = albumRepo.findByTitle(title.trim());
        if (album == null) {
            album = new Album();
            album.setTitle(title.trim());
            album.setArtistId(artist);
            em.persist(album);
            stats.insertedAlbums++;
        }
        albumCache.put(key, album);
        return album;
    }

    private MediaType getOrCreateMediaType(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        if (mediaTypeCache.containsKey(key)) return mediaTypeCache.get(key);
        MediaType mt = findMediaType(name);
        if (mt == null) {
            mt = new MediaType();
            mt.setName(name.trim());
            em.persist(mt);
        }
        mediaTypeCache.put(key, mt);
        return mt;
    }

    private MediaType findMediaType(String name) {
        TypedQuery<MediaType> query = em.createQuery(
                "SELECT m FROM MediaType m WHERE UPPER(m.name) = :name", MediaType.class);
        return query.setParameter("name", name.trim().toUpperCase())
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private Genre getOrCreateGenre(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        if (genreCache.containsKey(key)) return genreCache.get(key);
        Genre genre = findGenre(name);
        if (genre == null) {
            genre = new Genre();
            genre.setName(name.trim());
            em.persist(genre);
        }
        genreCache.put(key, genre);
        return genre;
    }

    private Genre findGenre(String name) {
        TypedQuery<Genre> query = em.createQuery(
                "SELECT g FROM Genre g WHERE UPPER(g.name) = :name", Genre.class);
        return query.setParameter("name", name.trim().toUpperCase())
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private Track getOrCreateTrack(String name,
                                   Album album,
                                   MediaType mediaType,
                                   Genre genre,
                                   String composer,
                                   Integer millis,
                                   Integer bytes,
                                   BigDecimal unitPrice,
                                   Stats stats) {
        String normalizedName = normalizeTrackName(name);
        if (trackCache.containsKey(normalizedName)) {
            return trackCache.get(normalizedName);
        }
        Track track = trackRepo.findByName(name.trim());
        if (track == null) {
            track = new Track();
            track.setName(name.trim());
            track.setAlbum(album);
            track.setMediaType(mediaType);
            track.setGenre(genre);
            track.setComposer(composer != null ? composer.trim() : null);
            track.setMilliseconds(millis);
            track.setBytes(bytes);
            track.setUnitPrice(unitPrice);
            em.persist(track);
            em.flush(); // Asegurar que el track esté disponible para búsquedas posteriores
            stats.insertedTracks++;
        }
        trackCache.put(normalizedName, track);
        return track;
    }

    private static String normalizeTrackName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private Playlist getOrCreatePlaylist(String name, Stats stats) {
        String key = name.toUpperCase(Locale.ROOT);
        if (playlistCache.containsKey(key)) return playlistCache.get(key);
        Playlist playlist = playlistRepo.findByName(name);
        if (playlist == null) {
            playlist = new Playlist();
            playlist.setName(name.trim());
            em.persist(playlist);
            stats.insertedPlaylists++;
        }
        playlistCache.put(key, playlist);
        return playlist;
    }

    private void linkPlaylistTrack(Playlist playlist, Track track, Stats stats) {
        String key = (playlist.getName().toUpperCase(Locale.ROOT) + "|" + track.getName().toUpperCase(Locale.ROOT));
        if (playlistTrackKeys.contains(key)) {
            return;
        }
        if (playlistTrackRepo.exists(playlist, track)) {
            playlistTrackKeys.add(key);
            return;
        }
        playlistTrackRepo.create(playlist, track);
        playlistTrackKeys.add(key);
        stats.insertedPlaylistTracks++;
    }

    private static String str(String[] row, int idx) {
        if (row == null || idx >= row.length) return null;
        String value = row[idx];
        return value == null ? null : value.trim();
    }

    private static boolean hasMissingRequiredField(String... values) {
        for (String value : values) {
            if (value == null || value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static Integer parsePositiveInt(String value) {
        if (isBlank(value)) return null;
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
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
            return price.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class Stats {
        public int totalRows;
        public int processedRows;
        public int skippedRows;
        public int insertedTracks;
        public int insertedPlaylists;
        public int insertedAlbums;
        public int insertedPlaylistTracks;
        public int fixedBytes;
        public int missingRequiredRows;
        public List<String> reasons = new ArrayList<>();

        @Override
        public String toString() {
            return "Stats{rows=" + totalRows +
                    ", procesadas=" + processedRows +
                    ", saltadas=" + skippedRows +
                    ", tracks=" + insertedTracks +
                    ", playlists=" + insertedPlaylists +
                    ", albums=" + insertedAlbums +
                    ", playlistTracks=" + insertedPlaylistTracks +
                    ", fixedBytes=" + fixedBytes +
                    ", missingRequired=" + missingRequiredRows + "}";
        }
    }
}

