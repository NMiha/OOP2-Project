package persistence;

import model.Airport;
import model.Dataset;
import model.Flight;
import util.TimeUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CsvStorage implements Storage {

    private static final String AIRPORTS_MARKER = "# AIRPORTS";
    private static final String FLIGHTS_MARKER  = "# FLIGHTS";
    private static final String AIRPORT_HEADER  = "CODE,NAME,X,Y";
    private static final String FLIGHT_HEADER   = "FROM,TO,DEPARTURE,DURATION";

    @Override
    public String formatName() { 
    	return "CSV"; 
    }

    @Override
    public String extension() {
    	return "csv";
    }

    /* ------------------------------ Cuvanje ----------------------------- */

    @Override
    public void save(Dataset data, File file) throws StorageException {
        try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            bw.write(AIRPORTS_MARKER); 
            bw.newLine();
            bw.write(AIRPORT_HEADER);  
            bw.newLine();
            for (Airport a : data.getAirports()) {
                bw.write(a.code() + "," + a.name() + "," + num(a.x()) + "," + num(a.y()));
                bw.newLine();
            }

            bw.newLine(); // prazan red izmedju sekcija

            bw.write(FLIGHTS_MARKER); 
            bw.newLine();
            bw.write(FLIGHT_HEADER);  
            bw.newLine();
            for (Flight f : data.getFlights()) {
                String departure = TimeUtil.formatHm(f.departureHour(), f.departureMinute());
                bw.write(f.originCode() + "," + f.destinationCode() + "," + departure + "," + f.durationMinutes());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new StorageException("Nije moguce sacuvati fajl „" + file.getName()
                    + "“: " + e.getMessage() + ". Proverite putanju i dozvole za pisanje.", e);
        }
    }

    /* ---------------------------- Ucitavanje ---------------------------- */

    @Override
    public Dataset load(File file) throws StorageException {
        if (!file.exists()) {
            throw new StorageException("Fajl „" + file.getName()
                    + "“ ne postoji. Proverite putanju ili izaberite drugi fajl.");
        }
        if (!file.canRead()) {
            throw new StorageException("Fajl „" + file.getName()
                    + "“ ne moze da se procita. Proverite dozvole pristupa.");
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new StorageException("Greska pri citanju fajla „" + file.getName()
                    + "“: " + e.getMessage() + ". Fajl je mozda ostecen.", e);
        }

        return parse(lines);
    }

    private Dataset parse(List<String> lines) throws StorageException {
        List<Airport> airports = new ArrayList<>();
        List<Flight> flights = new ArrayList<>();
        int n = lines.size();

        // --- Sekcija AIRPORTS ---
        int i = skipEmpty(lines, 0);
        if (i >= n || !lines.get(i).trim().equalsIgnoreCase(AIRPORTS_MARKER)) {
            throw new StorageException("Ocekivan je pocetak sekcije „" + AIRPORTS_MARKER
                    + "“. Proverite format ili unesite novi fajl.");
        }
        i++;
        i = skipEmpty(lines, i);
        if (i >= n || !lines.get(i).trim().equalsIgnoreCase(AIRPORT_HEADER)) {
            throw new StorageException("Fajl ne sadrzi ocekivane kolone (" + AIRPORT_HEADER
                    + "). Proverite format ili unesite novi fajl.");
        }
        i++;

        while (i < n && !lines.get(i).trim().equalsIgnoreCase(FLIGHTS_MARKER)) {
            String line = lines.get(i);
            if (!line.isBlank()) {
                String[] p = line.split(",");
                if (p.length != 4) {
                    throw new StorageException("Neispravan broj kolona u redu " + (i + 1)
                            + " (aerodrom): ocekovano 4, pronadjeno " + p.length + ".");
                }
                String code = p[0].trim();
                String name = p[1].trim();
                double x = parseDouble(p[2], i + 1, "X");
                double y = parseDouble(p[3], i + 1, "Y");
                try {
                    airports.add(new Airport(name, code, x, y));
                } catch (IllegalArgumentException e) {
                    throw new StorageException("Neispravan red " + (i + 1) + ": " + e.getMessage());
                }
            }
            i++;
        }

        // --- Sekcija FLIGHTS (opciona) ---
        i = skipEmpty(lines, i);
        if (i >= n) {
            return new Dataset(airports, flights);
        }
        i++; // preskoci FLIGHTS marker
        i = skipEmpty(lines, i);
        if (i >= n || !lines.get(i).trim().equalsIgnoreCase(FLIGHT_HEADER)) {
            throw new StorageException("Fajl ne sadrzi ocekivane kolone (" + FLIGHT_HEADER
                    + "). Proverite format ili unesite novi fajl.");
        }
        i++;

        while (i < n) {
            String line = lines.get(i);
            if (!line.isBlank()) {
                String[] p = line.split(",");
                if (p.length != 4) {
                    throw new StorageException("Neispravan broj kolona u redu " + (i + 1)
                            + " (let): ocekovano 4, pronadjeno " + p.length + ".");
                }
                String from = p[0].trim();
                String to = p[1].trim();
                int[] hm = parseDeparture(p[2], i + 1);
                int duration = parseInt(p[3], i + 1, "DURATION");
                try {
                    flights.add(new Flight(from, to, hm[0], hm[1], duration));
                } catch (IllegalArgumentException e) {
                    throw new StorageException("Neispravan red " + (i + 1) + ": " + e.getMessage());
                }
            }
            i++;
        }

        return new Dataset(airports, flights);
    }

    /* ------------------------------ Pomocno ----------------------------- */

    private static int skipEmpty(List<String> lines, int i) {
        while (i < lines.size() && lines.get(i).isBlank()) i++;
        return i;
    }

    private static double parseDouble(String v, int line, String col) throws StorageException {
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            throw new StorageException("Red " + line + ": „" + v.trim()
                    + "“ nije ispravan broj za kolonu " + col + ".");
        }
    }

    private static int parseInt(String v, int line, String col) throws StorageException {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new StorageException("Red " + line + ": „" + v.trim()
                    + "“ nije ispravan ceo broj za kolonu " + col + ".");
        }
    }

    private static int[] parseDeparture(String v, int line) throws StorageException {
        try {
            return TimeUtil.parseHhMm(v);
        } catch (IllegalArgumentException e) {
            throw new StorageException("Red " + line + " (kolona DEPARTURE): " + e.getMessage());
        }
    }

    private static String num(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) return String.valueOf((long) v);
        return String.valueOf(v);
    }
}