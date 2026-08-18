package util;

public final class TimeUtil {

	private TimeUtil() { }
	
	public static String formatHm(int hour, int minute) {
		return String.format("%02d:%02d", hour, minute);
	}
	
	public static int[] parseHhMm(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Vreme poletanja je obavezno u formatu HH:mm, npr. 08:30.");
        }
        String s = raw.trim();
        String[] parts = s.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Vreme poletanja mora biti u formatu HH:mm (npr. 08:30). Uneto: „" + s + "“.");
        }
        int hour;
        int minute;
        try {
            hour = Integer.parseInt(parts[0].trim());
            minute = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Vreme poletanja mora sadrzati brojeve u formatu HH:mm (npr. 08:30). Uneto: „" + s + "“.");
        }
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Sat poletanja mora biti u opsegu 0–23. Uneto: " + hour + ".");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minut poletanja mora biti u opsegu 0–59. Uneto: " + minute + ".");
        }
        return new int[]{hour, minute};
    }
	
	public static int toMinutesOfDay(int hour, int minute) {
		return hour * 60 + minute;
	}
	
	public static String arrivalHm(int departureHour, int departureMinute, int durationMinutes) {
		int total = (toMinutesOfDay(departureHour, departureMinute) + durationMinutes) % (24*60);
		if(total < 0) {
			total += 24 * 60;
		}
		return formatHm(total/60, total%60);
	}
}
