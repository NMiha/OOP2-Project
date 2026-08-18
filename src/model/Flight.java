package model;

public record Flight(
		String originCode, 
		String destinationCode,
		int departureHour,
		int departureMinute,
		int durationMinutes) {
	
	public Flight {
		if(originCode == null || originCode.isBlank()) {
			throw new IllegalArgumentException("Polazni aerodrom ne sme biti prazan.");
		}
		if(destinationCode == null || destinationCode.isBlank()) {
			throw new IllegalArgumentException("Krajnji aerodrom ne sme biti prazan.");
		}
		originCode = originCode.trim();
		destinationCode = destinationCode.trim();
	}

}
