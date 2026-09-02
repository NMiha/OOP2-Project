package gui;

import logic.DataManager;
import logic.ValidationException;
import model.Airport;
import model.Dataset;
import model.Flight;
import persistence.CsvStorage;
import persistence.JsonStorage;
import persistence.Storage;
import persistence.StorageException;
import util.InactivityTimer;
import util.TimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainFrame extends JFrame implements InactivityTimer.Listener {

    private final DataManager manager = new DataManager();

    private final JTextField airportName = new JTextField(14);
    private final JTextField airportCode = new JTextField(6);
    private final JTextField airportX = new JTextField(6);
    private final JTextField airportY = new JTextField(6);

    private final JComboBox<String> flightOrigin = new JComboBox<>();
    private final JComboBox<String> flightDestination = new JComboBox<>();
    private final JTextField flightDeparture = new JTextField(6);
    private final JTextField flightDuration = new JTextField(6);

    private final DefaultTableModel airportsModel =
            new DefaultTableModel(new String[]{"Naziv", "Kod", "x", "y"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
    private final DefaultTableModel flightsModel =
            new DefaultTableModel(new String[]{"Polazni", "Krajnji", "Poletanje", "Trajanje (min)", "Sletanje"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

    private InactivityTimer timer;
    private CountdownDialog countdownDialog;
    private MapFrame mapFrame;

    public MainFrame() {
        super("Aerodromi i letovi");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        setJMenuBar(buildMenuBar());

        add(buildInputArea(), BorderLayout.NORTH);
        add(buildTablesArea(), BorderLayout.CENTER);
        add(buildButtonArea(), BorderLayout.SOUTH);

        setSize(900, 640);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { shutdown(); }
        });

        refreshAll();
        setupTimer();
    }

    /* --------- Izgradnja UI --------- */

    private JPanel buildInputArea() {
        JPanel area = new JPanel(new GridLayout(1, 2, 12, 0));
        area.add(buildAirportForm());
        area.add(buildFlightForm());
        return area;
    }

    private JPanel buildAirportForm() {
        JPanel form = new JPanel(new BorderLayout(6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Unos aerodroma"));
        JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
        f.add(new JLabel("Naziv:"));                f.add(airportName);
        f.add(new JLabel("Kod (3 velika slova):")); f.add(airportCode);
        f.add(new JLabel("x  [-180, 180]:"));       f.add(airportX);
        f.add(new JLabel("y  [-90, 90]:"));         f.add(airportY);
        form.add(f, BorderLayout.CENTER);
        JButton add = new JButton("Dodaj aerodrom");
        add.addActionListener(e -> onAddAirport());
        JPanel s = new JPanel(new FlowLayout(FlowLayout.RIGHT)); s.add(add);
        form.add(s, BorderLayout.SOUTH);
        return form;
    }

    private JPanel buildFlightForm() {
        JPanel form = new JPanel(new BorderLayout(6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Unos leta"));
        JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
        f.add(new JLabel("Polazni aerodrom:"));        f.add(flightOrigin);
        f.add(new JLabel("Krajnji aerodrom:"));        f.add(flightDestination);
        f.add(new JLabel("Vreme poletanja (HH:mm):")); f.add(flightDeparture);
        f.add(new JLabel("Trajanje (min):"));          f.add(flightDuration);
        form.add(f, BorderLayout.CENTER);
        JButton add = new JButton("Dodaj let");
        add.addActionListener(e -> onAddFlight());
        JPanel s = new JPanel(new FlowLayout(FlowLayout.RIGHT)); s.add(add);
        form.add(s, BorderLayout.SOUTH);
        return form;
    }

    private JPanel buildTablesArea() {
        JPanel area = new JPanel(new GridLayout(2, 1, 0, 12));
        JPanel a = new JPanel(new BorderLayout());
        a.setBorder(BorderFactory.createTitledBorder("Aerodromi"));
        a.add(new JScrollPane(new JTable(airportsModel)), BorderLayout.CENTER);
        JPanel f = new JPanel(new BorderLayout());
        f.setBorder(BorderFactory.createTitledBorder("Letovi"));
        f.add(new JScrollPane(new JTable(flightsModel)), BorderLayout.CENTER);
        area.add(a); area.add(f);
        return area;
    }

    /*private JPanel buildButtonArea() {
        JPanel area = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton saveCsv = new JButton("Sacuvaj u CSV");
        JButton loadCsv = new JButton("Ucitaj iz CSV");
        JButton saveJson = new JButton("Sacuvaj u JSON");
        JButton loadJson = new JButton("Ucitaj iz JSON");
        JButton clear = new JButton("Obrisi sve");
        saveCsv.addActionListener(e -> onSave(new CsvStorage()));
        loadCsv.addActionListener(e -> onLoad(new CsvStorage()));
        saveJson.addActionListener(e -> onSave(new JsonStorage()));
        loadJson.addActionListener(e -> onLoad(new JsonStorage()));
        clear.addActionListener(e -> { manager.clear(); refreshAll(); });
        area.add(saveCsv); area.add(loadCsv); area.add(saveJson); area.add(loadJson); area.add(clear);
        return area;
    }*/
    
    private JPanel buildButtonArea() {
        JPanel area = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton clear = new JButton("Obrisi sve");
        JButton showMap = new JButton("Prikazi mapu");
        showMap.addActionListener(e -> onShowMap());
        clear.addActionListener(e -> { manager.clear(); refreshAll(); });
        area.add(showMap);
        area.add(clear);
        // Faza B: ovde ce doci i dugme "Prikazi mapu"
        return area;
    }
    
    private void onShowMap() {
        if (mapFrame == null || !mapFrame.isShowing()) {
            mapFrame = new MapFrame(manager, timer);
            mapFrame.setVisible(true);
        } else {
            mapFrame.toFront();
        }
    }
    
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("Datoteka");

        JMenuItem loadCsv = new JMenuItem("Ucitaj iz CSV");
        JMenuItem saveCsv = new JMenuItem("Sacuvaj u CSV");
        JMenuItem loadJson = new JMenuItem("Ucitaj iz JSON");
        JMenuItem saveJson = new JMenuItem("Sacuvaj u JSON");

        loadCsv.addActionListener(e -> onLoad(new CsvStorage()));
        saveCsv.addActionListener(e -> onSave(new CsvStorage()));
        loadJson.addActionListener(e -> onLoad(new JsonStorage()));
        saveJson.addActionListener(e -> onSave(new JsonStorage()));

        file.add(loadCsv);
        file.add(saveCsv);
        file.addSeparator();          // linija koja razdvaja CSV od JSON
        file.add(loadJson);
        file.add(saveJson);

        bar.add(file);
        return bar;
    }

    /* --------- Akcije --------- */

    private void onAddAirport() {
        try {
            manager.addAirport(airportName.getText(), airportCode.getText(), airportX.getText(), airportY.getText());
            airportName.setText(""); airportCode.setText(""); airportX.setText(""); airportY.setText("");
            refreshAll();
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Greska", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddFlight() {
        try {
            String o = (String) flightOrigin.getSelectedItem();
            String d = (String) flightDestination.getSelectedItem();
            manager.addFlight(o, d, flightDeparture.getText(), flightDuration.getText());
            flightDeparture.setText(""); flightDuration.setText("");
            refreshAll();
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Greska", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave(Storage storage) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("podaci." + storage.extension()));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            storage.save(manager.getDataset(), fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Sacuvano: " + fc.getSelectedFile().getName());
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Greska", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLoad(Storage storage) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Dataset loaded = storage.load(fc.getSelectedFile());
            manager.replaceWith(loaded);
            refreshAll();
            JOptionPane.showMessageDialog(this, "Ucitano: " + fc.getSelectedFile().getName());
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Greska", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, "Ucitani podaci nisu ispravni: " + e.getMessage(), "Greska", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* --------- Osvezavanje --------- */

    private void refreshAll() {
        airportsModel.setRowCount(0);
        for (Airport a : manager.getAirports())
            airportsModel.addRow(new Object[]{a.name(), a.code(), num(a.x()), num(a.y())});
        flightsModel.setRowCount(0);
        for (Flight f : manager.getFlights())
            flightsModel.addRow(new Object[]{f.originCode(), f.destinationCode(),
                    TimeUtil.formatHm(f.departureHour(), f.departureMinute()),
                    f.durationMinutes(),
                    TimeUtil.arrivalHm(f.departureHour(), f.departureMinute(), f.durationMinutes())});
        refreshChoices();
        if (mapFrame != null && mapFrame.isShowing()) mapFrame.refreshData();
    }

    private void refreshChoices() {
        String po = (String) flightOrigin.getSelectedItem();
        String pd = (String) flightDestination.getSelectedItem();
        flightOrigin.removeAllItems();
        flightDestination.removeAllItems();
        for (Airport a : manager.getAirports()) {
            flightOrigin.addItem(a.code());
            flightDestination.addItem(a.code());
        }
        if (po != null) flightOrigin.setSelectedItem(po);
        if (pd != null) flightDestination.setSelectedItem(pd);
    }

    /* --------- Tajmer --------- */

    private void setupTimer() {
        timer = new InactivityTimer(60, 5, this);
        countdownDialog = new CountdownDialog(() -> timer.continueWorking());
        timer.start();
    }
    
    public void onCountdown(int secondsLeft) {
        countdownDialog.setSecondsLeft(secondsLeft);
        if (!countdownDialog.isVisible()) {
            Frame anchor;
            if (mapFrame != null && mapFrame.isShowing()) {
                anchor = mapFrame;      // mapa otvorena -> centriraj nad mapom
            } else {
                anchor = this;          // inace -> nad glavnim prozorom
            }
            countdownDialog.showCentered(anchor);
        }
    }
    
    public void onExpire() { shutdown(); }
    public void onContinue() { countdownDialog.setVisible(false); }

    private void shutdown() {
        if (timer != null) timer.stop();
        if (mapFrame != null) mapFrame.closeMap();
        dispose();
        System.exit(0);
    }

    private static String num(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) return String.valueOf((long) v);
        return String.valueOf(v);
    }
}