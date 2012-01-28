package webtech.artistcollector.crawler.extractors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import webtech.artistcollector.data.CollectionAndArtist;
import webtech.artistcollector.interfaces.NameExtractor;
import webtech.artistcollector.interfaces.PageInfo;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Eine Klasse, die es erlaubt einen Extraktor nur durch Definition
 * von Regex zu definieren, ohne eine neue Klasse dafür schreiben
 * zu müssen.
 */
public class GeneralRegexNameExtractor implements NameExtractor {

    /**
     * Daten zur Beschreibung eines Extraktors
     */
    public static class Params {
        /**
         * Name dieses Extraktors
         */
        public String name;

        /**
         * Liste von Patterns, die jeweils ein oder zwei Gruppen matchen müssen.
         * Alle Pattern werden auf eine Seite angewendet und die Treffer zur
         * Ergebnismenge hinzugefügt. Eine match-Gruppe wird als Monogramm
         * interpretiert, zwei Gruppen als Vor- bzw. Nachname.
         */
        public Pattern[] patterns;

        /**
         * Regex, der auf URLs matchen muss, die von diesem Extraktor behandelt
         * werden. Wenn der Wert null ist, werden alle URLs akzeptiert.
         */
        public Pattern acceptableURLs;

        /**
         * Regex, der auf den Inhalt passen muss, der von diesem Extraktor behandelt wird.
         * Der Wert kann null sein, wenn der Inhalt nicht überprüft werden soll.
         */
        public Pattern acceptableContent;

        /**
         * Kann gesetzt werden, um den Namen der erkannten Sammmlung explizit zu setzen.
         * Ansonsten wird die angenommene Sammlung der Seite verwendet.
         */
        public String collection = null;

        public Params(String name, Pattern acceptableURLs, Pattern acceptableContent,
                      String collection, Pattern...patterns) {
            this.name = name;
            this.acceptableURLs = acceptableURLs;
            this.acceptableContent = acceptableContent;
            this.collection = collection;
            this.patterns = patterns;
        }

        protected void load(PropertiesConfiguration config) throws IOException, ConfigurationException {
            this.name = config.getString("name");
            this.collection = config.getString("collection");
            if (collection != null) collection = collection.trim();

            String urlPattern = config.getString("acceptableURLs");
            if (urlPattern != null) this.acceptableURLs = Pattern.compile(urlPattern.trim());

            String contentPattern = config.getString("acceptableContent");
            if (contentPattern != null) this.acceptableContent = Pattern.compile(contentPattern.trim());

            List<Pattern> patterns = new ArrayList<Pattern>();
            for (String p : config.getStringArray("patterns")) {
                patterns.add(Pattern.compile(p.trim(),
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
            }

            this.patterns = patterns.toArray(new Pattern[0]);
        }

        public Params(File configFile) throws IOException, ConfigurationException {
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.setDelimiterParsingDisabled(true);
            config.load(configFile);

            load(config);
        }

        public Params(URL configURL) throws IOException, ConfigurationException {
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.setDelimiterParsingDisabled(true);
            config.load(configURL);

            load(config);
        }

        public Params(InputStream configStream) throws IOException, ConfigurationException {
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.setDelimiterParsingDisabled(true);
            config.load(configStream);

            load(config);
        }
    }

    protected Params params;
    protected boolean selected = true;

    public GeneralRegexNameExtractor(Params params) throws Exception {
        this.params = params;

        if (params.name == null)
            throw new Exception("Name muss definiert werden");

        if (params.patterns == null || params.patterns.length == 0)
            throw new Exception("Es müssen Regex zum Extrahieren von Namen definiert werden");
    }

    /**
     * Liefert den Namen des Extraktors für die GUI
     *
     * @return Name
     */
    public String getExtractorName() {
        return params.name;
    }

    String getCrawlerTag() {
        return "MC3K: " + params.name;
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
        List<CollectionAndArtist> names = new ArrayList<CollectionAndArtist>();

        // Fügt jeden Treffer als Name zur aktuellen Seite hinzu
        for (Pattern p : params.patterns) {
            Matcher m = p.matcher(pageHTML);

            while (m.find()) {

                if (m.groupCount() != 1 && m.groupCount() != 2) {
                    throw new RuntimeException("## ERROR name regex has to match either 1 or 2 groups: " +
                        m.pattern().pattern());
                }

                String name = null;

                if (m.groupCount() == 1) {
                    name = m.group(1).trim();
                } else if (m.groupCount() == 2) {
                    name = m.group(2).trim() + " " + m.group(1).trim();
                }

                CollectionAndArtist item = new CollectionAndArtist(info.getCollection(), name);
                item.crawler = getCrawlerTag();
                item.url = info.getURL().toString();
                item.collection = params.collection != null ? params.collection : info.getCollection();

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

    /**
     * Gibt an, ob der Extraktor für eine bestimmte Seite geeignet ist.
     * Es wird der URL und der Inhalt der Seite mit den entsprechenden Regex
     * abgeglichen (acceptableURLs, acceptableContent).
     *
     * @param page Seiteninformationen
     * @return True, wenn die Seite vom Extraktor bearbeitet werden kann
     */
    public boolean isApplicable(PageInfo page) {
        boolean acceptable = true;

        acceptable &= params.acceptableURLs == null ||
                      params.acceptableURLs.matcher(page.getURL().toString()).find();

        if (page.getContent() != null) {
            acceptable &= params.acceptableContent == null ||
                          params.acceptableContent.matcher(page.getContent()).find();
        }

        return acceptable;
    }
}
