package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dataset {

	private final List<Airport> airports;
	private final List<Flight> flights;
	
	public Dataset() {
		this.airports = new ArrayList<Airport>();
		this.flights = new ArrayList<Flight>();
	}
	
	public Dataset(List<Airport> airports, List<Flight> flights) {
		this.airports = new ArrayList<>(airports != null ? airports : List.of());
		this.flights = new ArrayList<>(flights != null ? flights : List.of());
	}
	
	public List<Airport> getAirports() {
		return airports;
	}
	
	public List<Flight> getFlights() {
		return flights;
	}
	
	/** za GUI prikaz  */
	public List<Airport> airportsView() {
		return Collections.unmodifiableList(airports);
	}
	
	public List<Flight> flightsView() {
		return Collections.unmodifiableList(flights);
	}
	
}
