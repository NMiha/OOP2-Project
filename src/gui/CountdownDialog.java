package gui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CountdownDialog extends JDialog {

    private final JLabel countdownLabel;

    public CountdownDialog(Frame owner, Runnable onContinue) {
        super(owner, "Neaktivnost", false);   // false = NIJE modalan
        setAlwaysOnTop(true);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Program ce se uskoro zatvoriti");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(new Color(150, 20, 20));
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(title);
        add(north, BorderLayout.NORTH);

        countdownLabel = new JLabel();
        JLabel hint = new JLabel("Kliknite „Nastavi rad“ da ostanete u programu.");
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 4));
        center.add(countdownLabel);
        center.add(hint);
        add(center, BorderLayout.CENTER);

        JButton continueBtn = new JButton("Nastavi rad");
        continueBtn.addActionListener(e -> onContinue.run());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.add(continueBtn);
        add(south, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onContinue.run();   // zatvaranje "X" = zelim da nastavim
            }
        });

        setSize(430, 170);
    }

    public void setSecondsLeft(int seconds) {
        countdownLabel.setText("Zbog neaktivnosti, program se zatvara za " + seconds + " sekundi.");
    }

    public void showCentered(Frame owner) {
        setLocationRelativeTo(owner);
        setVisible(true);
        toFront();
    }
}