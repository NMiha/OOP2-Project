# Airports & Flights

A desktop application for entering, validating, visualizing, and persisting a small dataset of **airports** and the **flights** between them. Built in Java with Swing as an object-oriented university project (OOP 2).

The project is developed in two phases:

- **Phase A** — data entry through a form-based GUI, tabular display, validation, CSV/JSON persistence, and an inactivity timer.
- **Phase B** — a visual map that draws the airports by their coordinates, lets you select them, and provides a filterable side list.

---

## Features

### Phase A — Data entry & persistence
- Add **airports** with: name, a unique 3-letter uppercase code, and coordinates `x ∈ [-180, 180]`, `y ∈ [-90, 90]`.
- Add **flights** with: origin airport, destination airport, departure time (`HH:mm`), and duration in minutes.
- Both airports and flights are shown in **tables** that update as you add data.
- **Validation** with clear, user-friendly error messages (empty fields, wrong code format, duplicate codes, out-of-range coordinates, invalid time, flight referencing a non-existent airport, etc.).
- **Save / Load** the whole dataset in two formats:
  - **CSV** — a simple, human-readable text format with `# AIRPORTS` and `# FLIGHTS` sections.
  - **JSON** — structured format handled with the Gson library.
- **Inactivity timer**: if the user does nothing for 60 seconds, the application closes automatically. During the last 5 seconds a countdown dialog appears offering the option to continue working.

### Phase B — Map visualization
- The airports are drawn on a **map panel** as gray squares, positioned according to their `(x, y)` coordinates, each labeled with its 3-letter code.
- **Click** an airport to select it — the selected airport **blinks red**; click it again to deselect.
- While an airport is selected, the **inactivity timer is paused** (the user is considered active).
- A **side list** shows all airports with a checkbox filter, so the map view can be narrowed by name, code, or coordinates.
- All user interactions are wrapped in error handling so a single bad action never crashes the application.

---

## Architecture

The code is organized into layers, with dependencies flowing **downward** only. Upward communication happens through interfaces and callbacks. The `gui` layer is the "glue" that wires everything together.

```
model         →  immutable data types (records): Airport, Flight, Dataset
util          →  helpers: validation, time formatting, coordinate projection, inactivity timer
logic         →  DataManager: business rules, referential integrity
persistence   →  Storage interface + CsvStorage / JsonStorage (polymorphism)
gui           →  Swing windows, panels, dialogs
Main          →  application entry point
```

Key design choices:
- **Records** (`Airport`, `Flight`) make the data **immutable** — to "change" an airport you replace it with a new object in the list.
- **Custom checked exceptions** (`ValidationException`, `StorageException`) separate validation errors from I/O errors, and are translated between layers.
- **Two levels of validation**: the storage layer checks the structural correctness of a file, while `DataManager` enforces the business rules (coordinate ranges, unique codes, referential integrity).
- A **`Storage` interface** with `CsvStorage` and `JsonStorage` implementations lets the same code save/load in either format — polymorphism in action.
- The GUI runs on the **Event Dispatch Thread (EDT)**; background work (the inactivity timer) marshals back to the EDT with `EventQueue.invokeLater`.

---

## Project structure

```
src/
├── Main.java
├── model/
│   ├── Airport.java
│   ├── Flight.java
│   └── Dataset.java
├── logic/
│   └── DataManager.java
├── persistence/
│   ├── Storage.java
│   ├── CsvStorage.java
│   ├── JsonStorage.java
│   └── StorageException.java
├── util/
│   ├── Validators.java
│   ├── ValidationException.java
│   ├── TimeUtil.java
│   ├── MapProjection.java
│   └── InactivityTimer.java
└── gui/
    ├── MainFrame.java
    ├── MapFrame.java
    ├── MapPanel.java
    └── CountdownDialog.java
```

---

## Requirements

- **Java 17** or newer (uses records and modern language features).
- **Gson** library (for JSON persistence).

---

## Running the application

### From an IDE (Eclipse)
1. Import the project.
2. Make sure the Gson `.jar` is on the build path.
3. Run `Main.java`.

### From the command line
```bash
# compile (adjust the path to gson.jar)
javac -cp "lib/gson.jar" -d bin src/**/*.java src/Main.java

# run
java -cp "bin:lib/gson.jar" Main
```
> On Windows use `;` instead of `:` as the classpath separator.

---

## Data formats

### CSV
```
# AIRPORTS
CODE,NAME,X,Y
BEG,Beograd,20.3,44.8
...

# FLIGHTS
FROM,TO,DEPARTURE,DURATION
BEG,LON,08:30,150
...
```

### JSON
```json
{
  "airports": [
    { "code": "BEG", "name": "Beograd", "x": 20.3, "y": 44.8 }
  ],
  "flights": [
    { "from": "BEG", "to": "LON", "departure": "08:30", "duration": 150 }
  ]
}
```

---

## License

This project was created for educational purposes as part of an OOP course.
