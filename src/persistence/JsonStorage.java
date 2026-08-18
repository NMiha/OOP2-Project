package persistence;

import model.Airport;
import model.Dataset;
import model.Flight;
import util.TimeUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class JsonStorage implements Storage {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // DTO klase — tacno odgovaraju obliku JSON fajla
    private static class DatasetDto {
        List<AirportDto> airports = new ArrayList<>();
        List<FlightDto> flights = new ArrayList<>();
    }
    private static class AirportDto {
        String code; String name; double x; double y;
    }
    private static class FlightDto {
        String from; String to; String departure; int duration;
    }

    @Override
    public String formatName() { 
    	return "JSON"; 
    }

    @Override
    public String extension() {
    	return "json"; 
    }

    /* ------------------------------ Cuvanje ----------------------------- */

    @Override
    public void save(Dataset data, File file) throws StorageException {
        DatasetDto dto = new DatasetDto();

        for (Airport a : data.getAirports()) {
            AirportDto ad = new AirportDto();
            ad.code = a.code();
            ad.name = a.name();
            ad.x = a.x();
            ad.y = a.y();
            dto.airports.add(ad);
        }
        for (Flight f : data.getFlights()) {
            FlightDto fd = new FlightDto();
            fd.from = f.originCode();
            fd.to = f.destinationCode();
            fd.departure = TimeUtil.formatHm(f.departureHour(), f.departureMinute());
            fd.duration = f.durationMinutes();
            dto.flights.add(fd);
        }

        try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            bw.write(gson.toJson(dto));
        } catch (IOException e) {
            throw new StorageException("Nije moguce sacuvati JSON fajl „" + file.getName()
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

        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Greska pri citanju fajla „" + file.getName()
                    + "“: " + e.getMessage() + ". Fajl je mozda ostecen.", e);
        }

        DatasetDto dto;
        try {
            dto = gson.fromJson(content, DatasetDto.class);
        } catch (JsonSyntaxException e) {
            throw new StorageException("Fajl „" + file.getName()
                    + "“ nije ispravan JSON. Proverite sintaksu ili unesite novi fajl.", e);
        }
        if (dto == null) {
            throw new StorageException("Fajl je prazan ili ne sadrzi JSON objekat.");
        }

        List<Airport> airports = new ArrayList<>();
        if (dto.airports != null) {
            int idx = 1;
            for (AirportDto ad : dto.airports) {
                try {
                    airports.add(new Airport(ad.name, ad.code, ad.x, ad.y));
                } catch (IllegalArgumentException e) {
                    throw new StorageException("Aerodrom #" + idx + " u JSON-u: " + e.getMessage());
                }
                idx++;
            }
        }

        List<Flight> flights = new ArrayList<>();
        if (dto.flights != null) {
            int idx = 1;
            for (FlightDto fd : dto.flights) {
                int[] hm;
                try {
                    hm = TimeUtil.parseHhMm(fd.departure);
                } catch (IllegalArgumentException e) {
                    throw new StorageException("Let #" + idx + " (polje departure): " + e.getMessage());
                }
                try {
                    flights.add(new Flight(fd.from, fd.to, hm[0], hm[1], fd.duration));
                } catch (IllegalArgumentException e) {
                    throw new StorageException("Let #" + idx + " u JSON-u: " + e.getMessage());
                }
                idx++;
            }
        }

        return new Dataset(airports, flights);
    }
}