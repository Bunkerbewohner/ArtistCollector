package webtech.artistcollector.crawler;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import webtech.artistcollector.data.Database;
import webtech.artistcollector.data.Page;
import webtech.artistcollector.gui.CrawlerEventListener;
import webtech.artistcollector.interfaces.NameExtractor;
import webtech.artistcollector.interfaces.PageExtractor;
import webtech.artistcollector.interfaces.PageInfo;
import webtech.artistcollector.misc.Util;
import webtech.artistcollector.data.CollectionAndArtist;
import webtech.artistcollector.data.PageModel;
import webtech.artistcollector.gui.Main;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Der Crawler nutzt vorhandene Extraktoren, um ausgehend von der Wikipedia-Seite
 * rekursiv Seiten und die darin enthaltenen Namen zu extrahieren.
 */
public class Crawler implements RunnableFuture<List<CollectionAndArtist>>{
    URL startURL;
    boolean started = false;
    boolean done = false;
    boolean cancelled = false;
    List<CollectionAndArtist> results = new ArrayList<CollectionAndArtist>();
    ArrayList<CrawlerEventListener> listeners = new ArrayList<CrawlerEventListener>();

    Page rootPage;

    public Page getRootPage() {
        return rootPage;
    }

    public Crawler() {
        startURL = Main.getInstance().getPageModel().getRootPage().url;
        rootPage = new Page(startURL, null, false, true);
    }

    public void editStartURL() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);

        int result = fc.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return;

        URL url = null;
        try {
            url = fc.getSelectedFile().toURI().toURL();
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return;
        }

        rootPage.setURL(url);
        rootPage.setContainsNames(true);
        rootPage.setContainsPages(false);
    }

    public void run() {
        started = true;

        Main.getInstance().reportStatus(0, "Collecting Pages...");

        extractPages(rootPage);

        Main.getInstance().reportStatus(100, "Done.");

        for (CrawlerEventListener l : listeners) {
            l.crawlerFinished(rootPage);
        }
    }

    /**
     * Extrahiert alle Seiten aus gegebener Seite unter Nutzung aller bekannten
     * Extraktoren. Dazu wird die Vereinigung aller Ergebnisse gebildet.
     * @param page Seite, die durchsucht werden soll
     * @return Menge von Unterseiten
     */
    void extractPages(Page page) {
        if (cancelled) return;

        // Unterseiten hinzufügen (sofern welche vorhanden sein könnten)
        if (page.containsPages()) {
            for (PageExtractor pe : Extractors.getInstance().getPageExtractors()) {
                if (!pe.isSelected() || !pe.isApplicable(page)) continue;
                page.addSubPages(pe.extractPages(page));
            }
        }

        // Namen von dieser Seite extrahieren (sofern welche vorhanden sein könnten)
        if (page.containsNames()) {
            page.addNames(extractNames(page));
        }

        // Rekursiv alle Unterseiten bearbeiten
        for (PageInfo p : page.getSubPages()) {
            extractPages((Page)p);
        }
    }

    /**
     * Liefert die durch die zur Verfügung stehenden Namensextraktoren gefundenen Namen
     * auf der gegebenen Seite zurück.
     * @param page Eine Seite mit Namen drauf
     * @return Liste von Namen
     */
    Collection<CollectionAndArtist> extractNames(PageInfo page) {
        Set<CollectionAndArtist> names = new TreeSet<CollectionAndArtist>();

        // Vereinigung aller gefundenen Namen bilden
        for (NameExtractor ne : Extractors.getInstance().getNameExtractors()) {
            if (!ne.isSelected() || !ne.isApplicable(page)) continue;
            names.addAll(ne.extractNames(page));
        }

        return names;
    }

    /**
     * Unterbricht die Ausführung des Crawlers bei nächster Gelegenheit.
     * @param mayInterruptIfRunning
     * @return
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!started || done || cancelled) return false;

        if (mayInterruptIfRunning) {
            // interrupt
            cancelled = true;
            done = true;
        } else {
            // still running but cannot interrupt
            cancelled = false;
        }

        return cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDone() {
        return done;
    }

    public List<CollectionAndArtist> get() throws InterruptedException, ExecutionException {
        return Util.listNames(rootPage);
    }

    public List<CollectionAndArtist> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new NotImplementedException();
    }

    public void addEventListener(CrawlerEventListener l) {
        listeners.add(l);
    }

    public void remove(TreePath selectionPath) {
        Page page = rootPage.getPage(selectionPath);
        if (page != null) {
            rootPage.removePage(selectionPath);
        }
    }
}
