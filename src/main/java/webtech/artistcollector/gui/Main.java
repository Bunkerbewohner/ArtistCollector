package webtech.artistcollector.gui;

import webtech.artistcollector.crawler.Crawler;
import webtech.artistcollector.crawler.Extractors;
import webtech.artistcollector.crawler.extractors.BasePageExtractor;
import webtech.artistcollector.crawler.extractors.MunichNameExtractor;
import webtech.artistcollector.crawler.extractors.RegexNameExtractor;
import webtech.artistcollector.data.Database;
import webtech.artistcollector.data.PageModel;
import webtech.artistcollector.interfaces.Extractor;
import webtech.artistcollector.misc.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedList;

/**
 * Main Window
 */
public class Main extends JFrame {

    MainWindow mainForm;
    Crawler crawler;
    PageModel pageModel;
    Database db;

    public static void main(String[] args) {
        new Main().setVisible(true);
    }

    public static Main getInstance() {
        return instance;
    }

    private static Main instance;

    public Main() {

        // Nimbus Look & Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // w/e
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        setSize(800, 600);
        setTitle("Artist Collector | WebTech Projekt WS2011/2012 FU Berlin");
        setLocationRelativeTo(null);

        URL startURL = null;
        try {
            startURL = new URL("http://de.wikipedia.org/wiki/Liste_der_Sammlungen_moderner_oder_zeitgen%C3%B6ssischer_Kunst");
        } catch (MalformedURLException e) {
            assert false : "Ungültiger Start-URL";
        }
        pageModel = new PageModel(startURL);
        instance = this;

        initExtractors();

        crawler = new Crawler();

        // Create Form
        mainForm = new MainWindow();
        this.setContentPane(mainForm.getRootPanel());
        //mainForm.redirectSystemStreams();
        mainForm.setTreeModel(crawler.getRootPage());

        this.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {

            }

            public void keyPressed(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F2) {
                    crawler.editStartURL();
                    System.out.println("Edit start URL");
                }
            }
        });

        db = new Database();
        if (!db.isReady()) {
            System.out.println("Datenbankzugriff nicht möglich");
        }
    }

    public void saveToDB() {
        Main.getInstance().reportStatus(0, "Saving to Database...");
        db.InsertNames(Util.listNames(crawler.getRootPage()));
    }

    /**
     * Initialisiert die Extraktoren. Hier einfach für jede Art von Extraktor eine Instanz
     * hinzufügen per Extractors#getInstance.
     */
    void initExtractors() {
        Extractors e = Extractors.getInstance();

        e.registerExtractor(new BasePageExtractor());
        e.registerExtractor(new RegexNameExtractor());
        e.registerExtractor(new MunichNameExtractor());
    }

    public void startPageCollector() {
        new Thread(crawler).start();
    }

    public PageModel getPageModel() {
        return pageModel;
    }

    public void reportStatus(final float progressPercent, final String statusMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mainForm.reportStatus(progressPercent, statusMessage);
            }
        });
    }
}
