package logic;

import java.util.List;

import model.Airport;
import model.Dataset;
import model.Flight;
import util.TimeUtil;
import util.Validators;

public class DataManager {

	private Dataset dataset = new Dataset();
	
	 /* ----------------------------- Pristup ------------------------------ */

    public List<Airport> getAirports() {
        return dataset.airportsView();
    }

    public List<Flight> getFlights() {
        return dataset.flightsView();
    }

    public Dataset getDataset() {
        return dataset;
    }

    /* ---------------------------- Aerodromi ----------------------------- */
    
    public Airport addAirport(String name, String code, String x, String y) throws ValidationException {
    	String vName = Validators.validateAirportName(name);
    	String vCode = Validators.validateAirportCode(code);
    	double vX = Validators.validateX(x);
    	double vY = Validators.validateY(y);
    	
    	if(findAirport(vCode) != null) {
    		throw new ValidationException("Aerodrom sa kodom " + vCode + " već postoji. "
    				+ "Kod aerodroma mora biti jedinstven — upotrebite drugi kod.");
    	}
    	
    	Airport airport = new Airport(vName, vCode, vX, vY);
    	dataset.getAirports().add(airport);
    	return airport;
    }
    
    public Airport findAirport(String code) {
    	if(code == null) {
    		return null;
    	}
    	for (Airport a : dataset.getAirports()) {
    		if(a.code().equals(code)) {
    			return a;
    		}
    	}
    	return null;
    }
    
    /* ------------------------------ Letovi ------------------------------ */
    
    public Flight addFlight(String originCode, String destinationCode,
            String departure, String duration) throws ValidationException {
	if (dataset.getAirports().isEmpty()) {
	throw new ValidationException(
	    "Nije moguce dodati let jer nema unetih aerodroma. Prvo unesite bar dva aerodroma.");
	}
	
	String vOrigin = Validators.validateAirportCode(originCode);
	String vDest = Validators.validateAirportCode(destinationCode);
	
	if (findAirport(vOrigin) == null) {
	throw new ValidationException(
	    "Polazni aerodrom sa kodom „" + vOrigin + "“ ne postoji. "
	            + "Izaberite postojeci aerodrom ili ga prvo unesite.");
	}
	if (findAirport(vDest) == null) {
	throw new ValidationException(
	    "Krajnji aerodrom sa kodom „" + vDest + "“ ne postoji. "
	            + "Izaberite postojeci aerodrom ili ga prvo unesite.");
	}
	if (vOrigin.equals(vDest)) {
	throw new ValidationException(
	    "Polazni i krajnji aerodrom ne smeju biti isti („" + vOrigin + "“).");
	}
	
	int[] hm = Validators.validateDeparture(departure);
	int vDuration = Validators.validateDuration(duration);
	
	Flight flight = new Flight(vOrigin, vDest, hm[0], hm[1], vDuration);
	dataset.getFlights().add(flight);
	return flight;
	}
    
    /* --------------------------- Ucitavanje ----------------------------- */
    
    public void replaceWith(Dataset loaded) throws ValidationException {
    	DataManager temp = new DataManager();
    	for (Airport a : loaded.getAirports()) {
    		temp.addAirport(a.name(), a.code(), String.valueOf(a.x()), String.valueOf(a.y()));
    	}
    	
    	for (Flight f : loaded.getFlights()) {
    		temp.addFlight(f.originCode(), f.destinationCode(), 
    				TimeUtil.formatHm(f.departureHour(), f.departureMinute()), 
    				String.valueOf(f.durationMinutes()));
    	}
    	this.dataset = temp.dataset;
    }
    
    public void clear() {
    	this.dataset = new Dataset();
    }
}
