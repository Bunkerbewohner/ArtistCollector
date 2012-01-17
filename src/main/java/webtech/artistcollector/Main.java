package webtech.artistcollector;

import sun.security.jca.GetInstance;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Main Window
 */
public class Main extends JFrame {

    MainWindow mainForm;
    ArtistCollector collector;
    PageModel pageModel;

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
            // Should not happen
            e.printStackTrace();
            System.exit(-1);
        }
        pageModel = new PageModel(startURL);
        instance = this;

        collector = new ArtistCollector();

        // Create Form
        mainForm = new MainWindow();
        this.setContentPane(mainForm.getRootPanel());
        mainForm.redirectSystemStreams();
        mainForm.setTreeModel(pageModel);
    }

    public void startPageCollector() {
        new Thread(collector).start();
    }

    public PageModel getPageModel() {
        return pageModel;
    }

    public void reportStatus(float progressPercent, String statusMessage) {
        mainForm.reportStatus(progressPercent, statusMessage);
    }
}
