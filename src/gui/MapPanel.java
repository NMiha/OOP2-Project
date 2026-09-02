package gui;

import logic.DataManager;
import model.Airport;
import util.InactivityTimer;
import util.MapProjection;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class MapPanel extends JPanel {

    private static final int SQUARE = 14;
    private static final int PADDING = 40;
    private static final Color GREY = new Color(140, 140, 140);

    private final java.awt.Frame owner;
    private final DataManager manager;
    private final Map<String, Boolean> visibility;
    private final InactivityTimer timer;

    private String selectedCode;
    private boolean blinkOn = true;
    private final Timer blinker;

    public MapPanel(java.awt.Frame owner, DataManager manager, Map<String, Boolean> visibility, InactivityTimer timer) {
        this.owner = owner;
        this.manager = manager;
        this.visibility = visibility;
        this.timer = timer;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(720, 480));
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { handleClick(e); }
        });
        // Swing Timer kuca na EDT-u, pa ne treba invokeLater.
        blinker = new Timer(500, e -> { blinkOn = !blinkOn; repaint(); });
    }

    /* --------- Selekcija --------- */

    private void handleClick(MouseEvent e) {
        try {
            MapProjection proj = new MapProjection(getWidth(), getHeight(), PADDING);
            String hit = null;
            for (Airport a : manager.getAirports()) {
                if (!isVisible(a.code())) continue;
                int cx = proj.screenX(a.x());
                int cy = proj.screenY(a.y());
                Rectangle r = new Rectangle(cx - SQUARE / 2, cy - SQUARE / 2, SQUARE, SQUARE);
                if (r.contains(e.getPoint())) { hit = a.code(); break; }
            }
            if (hit == null) return;                       // klik na prazno ne menja
            setSelected(hit.equals(selectedCode) ? null : hit);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Greska pri obradi klika: " + ex.getMessage(),
                    "Greska", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setSelected(String code) {
        this.selectedCode = code;
        if (code != null) { timer.pause(); blinkOn = true; blinker.start(); }
        else { blinker.stop(); blinkOn = true; timer.resume(); }
        repaint();
    }

    public void deselectIfSelected(String code) {
        if (code != null && code.equals(selectedCode)) setSelected(null);
    }
    public void ensureSelectionValid() {
        if (selectedCode != null && (!isVisible(selectedCode) || manager.findAirport(selectedCode) == null))
            setSelected(null);
    }
    public void clearSelectionIfAny() { if (selectedCode != null) setSelected(null); }
    public void shutdown() { blinker.stop(); }

    private boolean isVisible(String code) { Boolean v = visibility.get(code); return v == null || v; }

    /* --------- Crtanje --------- */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   // Swing sam brise pozadinu + dvostruko baferovanje
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        drawGrid(g2, w, h);
        MapProjection proj = new MapProjection(w, h, PADDING);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        for (Airport a : manager.getAirports()) {
            if (!isVisible(a.code())) continue;
            int cx = proj.screenX(a.x());
            int cy = proj.screenY(a.y());
            boolean sel = a.code().equals(selectedCode);
            g2.setColor(sel && blinkOn ? Color.RED : GREY);
            g2.fillRect(cx - SQUARE / 2, cy - SQUARE / 2, SQUARE, SQUARE);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(cx - SQUARE / 2, cy - SQUARE / 2, SQUARE, SQUARE);
            g2.setColor(sel ? new Color(170, 0, 0) : Color.BLACK);
            g2.drawString(a.code(), cx + SQUARE / 2 + 3, cy + 4);
        }
    }

    private void drawGrid(Graphics2D g, int w, int h) {
        MapProjection proj = new MapProjection(w, h, PADDING);
        int x0 = proj.screenX(0), y0 = proj.screenY(0);
        g.setColor(new Color(225, 228, 232));
        g.drawLine(PADDING, y0, w - PADDING, y0);
        g.drawLine(x0, PADDING, x0, h - PADDING);
        g.setColor(new Color(190, 195, 200));
        g.drawRect(PADDING, PADDING, w - 2 * PADDING, h - 2 * PADDING);
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("x=-180", PADDING - 6, h - PADDING + 16);
        g.drawString("x=180", w - PADDING - 24, h - PADDING + 16);
        g.drawString("y=90", 6, PADDING + 4);
        g.drawString("y=-90", 6, h - PADDING + 4);
    }
}