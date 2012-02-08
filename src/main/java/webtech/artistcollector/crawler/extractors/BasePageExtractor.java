package webtech.artistcollector.crawler.extractors;

import webtech.artistcollector.data.Page;
import webtech.artistcollector.data.PageModel;
import webtech.artistcollector.gui.Main;
import webtech.artistcollector.interfaces.PageExtractor;
import webtech.artistcollector.interfaces.PageInfo;
import webtech.artistcollector.misc.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Angewendet auf die Ausgangsseite von Wikipedia
 * ("http://de.wikipedia.org/wiki/Liste_der_Sammlungen_moderner_oder_zeitgen%C3%B6ssischer_Kunst")
 * soll dieser Extraktor alle Sammlungsseiten extrahieren.
 */
public class BasePageExtractor implements PageExtractor {

    URL url;
    boolean selected = true;

    public BasePageExtractor() {
        try {
            url = new URL("http://de.wikipedia.org/wiki/Liste_der_Sammlungen_moderner_oder_zeitgen%C3%B6ssischer_Kunst");
        } catch (MalformedURLException e) {
            assert false : "Ungültiger Referenz-URL";
        }
    }

    public boolean isApplicable(PageInfo page) {
        // Dieser Extraktor bearbeitet nur die Liste der Sammlungen,
        // auch keine Unterseiten davon!
        return page.getURL().toString().contains("wikipedia");
    }

    /**
     * Liefert den Namen des Extraktors für die GUI
     *
     * @return Name
     */
    public String getExtractorName() {
        return "BasePageExtractor (Wikipedia)";
    }

    /**
     * Liefert eine Menge von Seiten, die auf der gegebenen Seite referenziert wurden.
     *
     * @param info Info über eine Seite, welche nach weiteren Seiten durchsucht werden soll
     * @return Liste von Seiten
     */
    public Collection<PageInfo> extractPages(PageInfo info) {
        // Dieser Extraktor soll nur auf die Wikipedia-Seite angewendet werden und liefert daher
        // für alle anderen Seiten eine leere Ergebnismenge zurück
        if (!info.getURL().equals(url)) {
            return new LinkedList<PageInfo>();
        }

        // HTML runterladen und analysieren
        String html = info.getContent();
        List<PageInfo> pages = new ArrayList<PageInfo>();

        // Seitenlinks per Regex rausholen
        Pattern[] patterns = new Pattern[]{
                //Pattern.compile("<a href=\"([^\"]+?)\"[^>]+>([^<]+?)</a>")
                Pattern.compile("<li>[^<]*?<a href=\"([^\"]*?)\"[^>]+>([^<]*?)</a>", Pattern.DOTALL),
                Pattern.compile("<tr>\\s+<td>.*?<a href=\"([^\"]*?)\"[^>]+>([^<]*?)</a>", Pattern.DOTALL)
        };

        // Aus jedem Treffer eine Seite erzeugen
        for (Pattern p : patterns) {
            Matcher m = p.matcher(html);

            while (m.find()) {
                String url = m.group(1);
                String title = m.group(2);

                if (url.startsWith("/")) url = "http://de.wikipedia.org" + url;
                if (url.startsWith("#") || url.contains("=") || url.matches(".*:[^/]+.*")) {
                    //System.out.println("## DISCARDED '" + url + "'");
                    continue;
                }

                if (!info.getURL().toString().equals(url)) {
                    try {
                        Page page = new Page(new URL(url), title, true, true);
                        page.setParent((Page)info);
                        pages.add(page);
                    } catch (MalformedURLException e) {
                        //System.out.println("Invalid URL '" + url + "'");
                    }
                }
            }
        }

        if (pages.size() == 0) {
            System.out.println("No Pages found at '" + info.getURL().toString() + "'");
        } else {
            System.out.println(pages.size() + " Pages found at '" + info.getURL().toString() + "'");
        }

        return pages;
    }

    /**
     * Gibt an, ob dieser Extraktor selektiert ist.
     *
     * @return True, wenn selektiert
     * @see webtech.artistcollector.interfaces.Extractor#setSelected(boolean)
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Setzt den Zustand auf selektiert oder nicht selektiert.
     * Nur selektierte Extraktoren werden vom Crawler benutzt.
     * Die Selektion wird vom Nutzer per GUI vorgenommen.
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
