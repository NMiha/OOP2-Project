package test;

import model.Airport;
import model.Dataset;
import model.Flight;
import persistence.CsvStorage;
import persistence.StorageException;
import util.TimeUtil;

import java.io.File;

public class UcitajCsv {

    public static void main(String[] args) {
        File csv = new File("test.csv");   // fajl u korenu projekta

        try {
            Dataset d = new CsvStorage().load(csv);

            System.out.println("Ucitano: " + d.getAirports().size() + " aerodroma, "
                    + d.getFlights().size() + " letova\n");

            for (Airport a : d.getAirports()) {
                System.out.println("Aerodrom: " + a.code() + " – " + a.name()
                        + " (" + a.x() + ", " + a.y() + ")");
            }
            for (Flight f : d.getFlights()) {
                System.out.println("Let: " + f.originCode() + " -> " + f.destinationCode()
                        + " u " + TimeUtil.formatHm(f.departureHour(), f.departureMinute())
                        + ", trajanje " + f.durationMinutes() + " min");
            }
        } catch (StorageException e) {
            System.out.println("GRESKA: " + e.getMessage());   // lepa poruka umesto pada programa
        }
    }
}