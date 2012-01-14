package webtech.artistcollector;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

public class Main extends JFrame {

    ArtistCollector collector;

    public static void main(String[] args) {
        new Main().setVisible(true);
    }

    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        setSize(800, 600);
        setTitle("Artist Collector | WebTech Projekt WS2011/2012 FU Berlin");
        setLocationRelativeTo(null);

        collector = new ArtistCollector();
        new Thread(collector).start();
    }
}
