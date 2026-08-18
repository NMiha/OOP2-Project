package model;

public record Airport(String name, String code, double x, double y) {

	public Airport {
		if(name == null || name.isBlank()) {
			throw new IllegalArgumentException("Naziv aerodroma ne sme biti prazan.");
		}
		if(code == null || code.isBlank()) {
			throw new IllegalArgumentException("Kod aerodroma ne sme biti prazan.");
		}
		name = name.trim();
		code = code.trim();
	}
}
