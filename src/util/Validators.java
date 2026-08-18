package util;

import java.util.regex.Pattern;

import logic.ValidationException;

public class Validators {

	public static final double X_MIN = -180.0;
	public static final double X_MAX = 180.0;
	public static final double Y_MIN = -90.0;
	public static final double Y_MAX = 90.0;
	
	private static final Pattern AIRPORT_CODE = Pattern.compile("^[A-Z]{3}$");
	
	private Validators() { }
	
	/* ----------------------------- Aerodrom ----------------------------- */
	
	public static String validateAirportName(String raw) throws ValidationException {
		if(raw == null || raw.isBlank()) {
			throw new ValidationException("Naziv aerodroma je obavezan. Unesite naziv, npr. Nikola Tesla.");
		}
		return raw.trim();
	}
	
	public static String validateAirportCode(String raw) throws ValidationException {
		if(raw == null || raw.isBlank()) {
			throw new ValidationException("Kod aerodroma je obavezan. Unesite tačno 3 velika slova, npr. BEG");
		}
		String code = raw.trim();
		if(!AIRPORT_CODE.matcher(code).matches()) {
			String reason;
			if(code.length() != 3) {
				reason = "kod mora imati tačno 3 znaka (uneto: " + code.length() + ")";
			} else if (!code.equals(code.toUpperCase())) {
				reason = "koristite samo VELIKA slova (npr. BEG umesto beg)";
			} else {
				reason = "dozvoljena su samo slova engleske abecede (A-Z), bez cifara i razmaka";
			}
			throw new ValidationException("Neispravan kod aerodroma " + code + " : " + reason + ".");
		}
		return code;
	}
	
	public static double validateX(String raw) throws ValidationException {
		double x = parseCoordinate(raw, "x");
		if (x < X_MIN || x > X_MAX) {
            throw new ValidationException(
                    "x koordinata mora biti u opsegu [" + fmt(X_MIN) + ", " + fmt(X_MAX)
                            + "]. Uneto: " + fmt(x) + ".");
        }
        return x;
	}
	
	 public static double validateY(String raw) throws ValidationException {
	        double y = parseCoordinate(raw, "y");
	        if (y < Y_MIN || y > Y_MAX) {
	            throw new ValidationException(
	                    "y koordinata mora biti u opsegu [" + fmt(Y_MIN) + ", " + fmt(Y_MAX)
	                            + "]. Uneto: " + fmt(y) + ".");
	        }
	        return y;
	    }
	
	private static double parseCoordinate(String raw, String osa) throws ValidationException {
		if(raw == null || raw.isBlank()) {
			throw new ValidationException(osa + " koordinata je obavezna. Unesite broj (decimalni zapis sa tackom).");
		}
		String s = raw.trim().replace(',', '.');
		try {
			return Double.parseDouble(s);
		} catch(NumberFormatException e) {
			throw new ValidationException(raw.trim() + "nije isparavan broj za " + osa + " koordinatu. Primer ispravnog unosa: 44.82 ili -20.35");
		}
	}
	
	/* ----------------------------- Let ----------------------------- */
	 
	public static int[] validateDeparture(String raw) throws ValidationException {
		try {
			return TimeUtil.parseHhMm(raw);
		} catch(IllegalArgumentException e) {
			throw new ValidationException(e.getMessage());
		}
	}
	
	public static int validateDuration(String raw) throws ValidationException {
		int d = parseIntField(raw, "trajanje leta");
		if (d <= 0) {
			throw new ValidationException("Trajanje leta mora biti pozitivan broj minuta. Uneto: " + d + ".");
		}
		return d;
	}
	
	
	private static int parseIntField(String raw, String name) throws ValidationException {
		if(raw == null || raw.isBlank()) {
			throw new ValidationException("Polje " + name + " je obavezno. Unesite ceo broj.");
		}
		try {
			return Integer.parseInt(raw.trim());
		} catch(NumberFormatException e) {
			throw new ValidationException(raw.trim()+ " nije ispravan ceo broj za polje" + name + ".");
		}
	}
	
	private static String fmt(double v) {
	        if (v == Math.floor(v) && !Double.isInfinite(v)) {
	            return String.valueOf((long) v);
	        }
	        return String.valueOf(v);
	    }
}
