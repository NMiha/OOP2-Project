# OOP2-Project — Airports & Flights

A desktop application for managing **airports** and **flights**, built in **Java (Swing)**.
Users can enter airports and flights through a graphical interface, view them in tables,
and save/load the data in **CSV** and **JSON** formats. The app auto-closes after 60 seconds
of inactivity, with a warning dialog in the final 5 seconds.

> Note: the user interface and messages are in **Serbian**.

## Features

- Enter **airports** (name, unique 3-letter uppercase code, x/y coordinates) and **flights**
  (origin, destination, departure time `HH:mm`, duration in minutes).
- **Validation** with clear, specific error messages (coordinate ranges, code format,
  duplicate codes, referential integrity of flights, time format).
- **Tabular display** of all airports and flights, with computed arrival time.
- **Persistence** in CSV and JSON, with robust error handling (missing file, wrong
  format, unreadable file) surfaced as user-friendly dialogs.
- **Inactivity timer**: the program closes after 60s of inactivity; in the last 5s a
  countdown dialog offers to continue.

## Requirements

- **Java 17+** (uses `record` classes)
- **Gson** (for JSON) — `gson-2.10.jar`

## Running the app

### From Eclipse
1. Import the project (*File → Import → Existing Projects into Workspace*).
2. Add `gson-2.10.jar` to the Build Path (*Build Path → Add to Build Path*).
3. Run `Main` (*Run As → Java Application*).

### As a runnable JAR
```bash
java -jar airports-flights.jar
```
(Export from Eclipse via *File → Export → Runnable JAR file*, choosing
"Extract required libraries into generated JAR" to bundle Gson.)

## Project structure (layered architecture)

| Package        | Responsibility                                                        |
|----------------|-----------------------------------------------------------------------|
| `model`        | Data model: `Airport`, `Flight`, `Dataset`                            |
| `logic`        | Business logic: `DataManager`, `ValidationException`                  |
| `persistence`  | File I/O: `Storage`, `CsvStorage`, `JsonStorage`, `StorageException`  |
| `util`         | Helpers: `Validators`, `TimeUtil`, `InactivityTimer`                  |
| `gui`          | Swing UI: `MainFrame`, `CountdownDialog`                              |

The model, logic, and persistence layers have no knowledge of the GUI, keeping the
architecture cleanly separated.

## File formats

### CSV
```
# AIRPORTS
CODE,NAME,X,Y
LHR,London Heathrow,0,51

# FLIGHTS
FROM,TO,DEPARTURE,DURATION
LHR,JFK,08:30,420
```

### JSON
```json
{
  "airports": [
    { "code": "LHR", "name": "London Heathrow", "x": 0, "y": 51 }
  ],
  "flights": [
    { "from": "LHR", "to": "JFK", "departure": "08:30", "duration": 420 }
  ]
}
```

## Roadmap

- **Phase B** — visual map of airports (drawn by coordinates, click-to-select with
  blinking highlight, checkbox filtering).
