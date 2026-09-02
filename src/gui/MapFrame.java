package gui;

import logic.DataManager;
import model.Airport;
import util.InactivityTimer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapFrame extends JFrame {

    private final DataManager manager;
    private final Map<String, Boolean> visibility = new HashMap<>();
    private final MapPanel mapPanel;
    private final JPanel filterList;

    public MapFrame(DataManager manager, InactivityTimer timer) {
        super("Mapa aerodroma — Faza B");
        this.manager = manager;
        setLayout(new BorderLayout(8, 8));

        mapPanel = new MapPanel(this, manager, visibility, timer);
        add(mapPanel, BorderLayout.CENTER);

        filterList = new JPanel();
        filterList.setLayout(new BoxLayout(filterList, BoxLayout.Y_AXIS));
        JScrollPane sp = new JScrollPane(filterList);
        sp.setPreferredSize(new Dimension(250, 480));
        JPanel east = new JPanel(new BorderLayout());
        east.setBorder(BorderFactory.createTitledBorder("Prikaz na mapi (filter)"));
        east.add(sp, BorderLayout.CENTER);
        add(east, BorderLayout.EAST);

        setSize(1024, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { closeMap(); }
        });

        refreshData();
    }

    public void refreshData() {
        Set<String> codes = new HashSet<>();
        for (Airport a : manager.getAirports()) { codes.add(a.code()); visibility.putIfAbsent(a.code(), true); }
        visibility.keySet().retainAll(codes);
        rebuildFilter();
        mapPanel.ensureSelectionValid();
        mapPanel.repaint();
    }

    private void rebuildFilter() {
        filterList.removeAll();
        if (manager.getAirports().isEmpty()) filterList.add(new JLabel("Nema unetih aerodroma."));
        for (Airport a : manager.getAirports()) {
            final String code = a.code();
            JCheckBox cb = new JCheckBox(code + " — " + a.name() + " (" + num(a.x()) + ", " + num(a.y()) + ")",
                    isVisible(code));
            cb.addActionListener(e -> {
                visibility.put(code, cb.isSelected());
                if (!cb.isSelected()) mapPanel.deselectIfSelected(code);
                mapPanel.repaint();
            });
            filterList.add(cb);
        }
        filterList.revalidate();
        filterList.repaint();
    }

    public void closeMap() {
        mapPanel.clearSelectionIfAny();
        mapPanel.shutdown();
        dispose();
    }

    private boolean isVisible(String c) { Boolean v = visibility.get(c); return v == null || v; }
    private static String num(double v) { if (v == Math.floor(v) && !Double.isInfinite(v)) return String.valueOf((long) v); return String.valueOf(v); }
}