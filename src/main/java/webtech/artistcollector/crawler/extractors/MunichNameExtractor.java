package webtech.artistcollector.crawler.extractors;

import webtech.artistcollector.data.CollectionAndArtist;
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
 * Ein Extraktor extra für die graphische Sammlung München
 */
public class MunichNameExtractor implements NameExtractor {

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
        // True heißt, dass der Extraktor für alle Seiten aufgerufen wird,
        // jedoch wird später beim Extrahieren nochmal geprüft, ob es sich
        // wirklich um die Müncher-Sammlung handelt. Dies kann nicht anhand
        // des URLs geprüft werden, sondern nur anhand des Inhalts.
        return true;
    }

    /**
     * Gegeben eine bestimmte Seite mit einer Zuordnung zu einer Kunstsammlung, liefert diese Funktion
     * eine Menge von Künstlernamen und den zugehörigen Sammlungen. Wenn aus jeglichem Grund keine
     * Künstername gefunden werden, muss eine leere Liste zurückgeliefert werden.
     *
     * @param info Seite
     * @return Liste von Namen mit Sammlungszuordnung
     */
    public Collection<CollectionAndArtist> extractNames(PageInfo info) {
        String pageHTML = info.getContent();
        if (pageHTML == null) {
            System.out.println("## ERROR could not download '" + info.getURL() + "'");
            return new LinkedList<CollectionAndArtist>();
        }

        // Nur bei Zieldokument anwenden, ansonsten hier aussteigen
        if (!pageHTML.contains("STAATLICHE GRAPHISCHE SAMMLUNG")) {
            return new LinkedList<CollectionAndArtist>();
        }

        // Reguläre Ausdrücke zum Finden der Namen
        Pattern[] patterns = new Pattern[]{

                /*
                // Monogramm A
                Pattern.compile("<SPAN[^>]+?>([^,]+?)-[^<>]*?</SPAN",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

                // Monogramm B
                Pattern.compile("<SPAN[^>]+?>([^,]+?)siehe[^<>]*?</SPAN",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                */

                // Normaler Name
                Pattern.compile("<SPAN[^>]+?>([^,<>]+),([^<>,]+?)-[^<>]*?</SPAN",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
        };

        List<CollectionAndArtist> names = new ArrayList<CollectionAndArtist>();

        // Fügt jeden Treffer als Name zur aktuellen Seite hinzu
        for (Pattern p : patterns) {
            Matcher m = p.matcher(pageHTML);

            while (m.find()) {

                String name = null;

                if (m.groupCount() == 1) {
                    name = m.group(1).trim();
                } else if (m.groupCount() == 2) {
                    name = m.group(2).trim() + " " + m.group(1).trim();
                }

                CollectionAndArtist item = new CollectionAndArtist(info.getCollection(), name);
                item.crawler = CRAWLER_TAG;
                item.url = info.getURL().toString();
                item.collection = "STAATLICHE GRAPHISCHE SAMMLUNG";

                if (m.groupCount() == 2) {
                    item.fname = m.group(2);
                    item.lname = m.group(1);
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