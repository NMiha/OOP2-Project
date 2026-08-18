package test;

import logic.DataManager;
import model.Airport;
import model.Dataset;
import model.Flight;
import persistence.CsvStorage;
import persistence.JsonStorage;
import util.TimeUtil;

import java.io.File;
import java.nio.file.Files;

public class TestPersistencije {

    public static void main(String[] args) throws Exception {
        // 1. Napravi podatke kroz DataManager (prolazi validaciju kao pravi unos)
        DataManager manager = new DataManager();
        manager.addAirport("London Heathrow", "LHR", "0", "51");
        manager.addAirport("Nikola Tesla", "BEG", "20.32", "44.82");
        manager.addFlight("LHR", "BEG", "08:30", "165");
        manager.addFlight("BEG", "LHR", "14:05", "150");

        System.out.println("Uneto: " + manager.getAirports().size() + " aerodroma, "
                + manager.getFlights().size() + " letova\n");

        // 2. CSV: sacuvaj pa ucitaj
        File csv = new File("test.csv");
        new CsvStorage().save(manager.getDataset(), csv);
        System.out.println("===== sadrzaj test.csv =====");
        Files.readAllLines(csv.toPath()).forEach(System.out::println);
        System.out.println("\nUcitano iz CSV:");
        ispisi(new CsvStorage().load(csv));

        // 3. JSON: sacuvaj pa ucitaj
        File json = new File("test.json");
        new JsonStorage().save(manager.getDataset(), json);
        System.out.println("\n===== sadrzaj test.json =====");
        Files.readAllLines(json.toPath()).forEach(System.out::println);
        System.out.println("\nUcitano iz JSON:");
        ispisi(new JsonStorage().load(json));

        // 4. Provera da ucitani podaci prolaze i logiku (replaceWith)
        DataManager m2 = new DataManager();
        m2.replaceWith(new JsonStorage().load(json));
        System.out.println("\nreplaceWith OK: " + m2.getAirports().size() + " aerodroma u novom menadzeru");
    }

    // Pomocni ispis jednog skupa
    private static void ispisi(Dataset d) {
        for (Airport a : d.getAirports()) {
            System.out.println("  Aerodrom: " + a.code() + " – " + a.name()
                    + " (" + a.x() + ", " + a.y() + ")");
        }
        for (Flight f : d.getFlights()) {
            System.out.println("  Let: " + f.originCode() + " -> " + f.destinationCode()
                    + " u " + TimeUtil.formatHm(f.departureHour(), f.departureMinute())
                    + ", trajanje " + f.durationMinutes() + " min");
        }
    }
}