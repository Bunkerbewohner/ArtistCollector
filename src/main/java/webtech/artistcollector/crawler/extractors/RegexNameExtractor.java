package webtech.artistcollector.crawler.extractors;

import webtech.artistcollector.data.CollectionAndArtist;
import webtech.artistcollector.data.PageModel;
import webtech.artistcollector.interfaces.NameExtractor;
import webtech.artistcollector.interfaces.PageInfo;
import webtech.artistcollector.misc.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extraktor, der versucht durch einfache Anwendung von regulären Ausdrücken alle (relevanten)
 * Namen auf einer Seite zu extrahieren. Erzeugt noch jede Menge Müll, d.h. false positives.
 */
public class RegexNameExtractor implements NameExtractor {

    private boolean selected = true;

    public final String CRAWLER_TAG = "MC3K";

    /**
     * Liefert den Namen des Extraktors für die GUI
     *
     * @return Name
     */
    public String getExtractorName() {
        return "RegexNameExtractor";
    }

    public boolean isApplicable(PageInfo page) {
        // Ist nur für die Wikipedia-Seiten gedacht
        return page.getURL().toString().matches("wikipedia") || true;
    }

    /**
     * Gegeben eine bestimmte Seite mit einer Zuordnung zu einer Kunstsammlung, liefert diese Funktion
     * eine Menge von Künstlernamen und den zugehörigen Sammlungen.
     *
     * @param info Seite
     * @return Liste von Namen mit Sammlungszuordnung
     */
    public Collection<CollectionAndArtist> extractNames(PageInfo info) {
        // HTML herunterladen zur Regex-Untersuchung
        String pageHTML = info.getContent();
        if (pageHTML == null) {
            System.out.println("## ERROR could not download '" + info.getURL() + "'");
            return new LinkedList<CollectionAndArtist>();
        }

        // Reguläre Ausdrücke zum Finden der Namen
        Pattern[] patterns = new Pattern[]{

                // Wikipedia naiv
                Pattern.compile("<a href=\"([^\"]+)\"[^>]+>([^< ]+ [^< ]+)</a>"),
        };

        List<CollectionAndArtist> names = new ArrayList<CollectionAndArtist>();

        // Fügt jeden Treffer als Name zur aktuellen Seite hinzu
        for (Pattern p : patterns) {
            Matcher m = p.matcher(pageHTML);

            while (m.find()) {
                String url = m.group(1);
                String name = m.group(2);

                if (url.startsWith("/")) url = "http://de.wikipedia.org" + url;
                if (url.startsWith("#") || url.contains("=") || url.matches(".*:[^/]+.*")) {
                    //System.out.println("## DISCARDED NAME '" + url + "'");
                    continue;
                }

                CollectionAndArtist item = new CollectionAndArtist(info.getCollection(), name);
                item.crawler = CRAWLER_TAG;
                item.url = info.getURL().toString();
                item.comment = this.getClass().getName();

                if (name.contains(" ")) {
                    String[] parts = name.split(" ", 2);
                    if (parts.length > 1) {
                        item.fname = parts[0];
                        item.lname = parts[1];
                    }
                }

                names.add(item);
            }
        }

        return names;
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
