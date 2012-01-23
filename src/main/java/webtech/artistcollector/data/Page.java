package webtech.artistcollector.data;

import webtech.artistcollector.interfaces.PageInfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Allgemeine Informationen über eine Website, in Bezug
 * auf das Problem (Finden von Künstlernamen zu Sammlungen).
 */
public class Page implements PageInfo {

    protected URL url;
    protected String collection;
    protected boolean gotNames;
    protected boolean gotPages;
    protected List<CollectionAndArtist> names;
    protected List<PageInfo> subPages;

    public Page(URL url, String collection, boolean gotNames, boolean gotPages)
    {
        this.url = url;
        this.collection = collection;
        this.gotNames = gotNames;
        this.gotPages = gotPages;
        names = new ArrayList<CollectionAndArtist>();
        subPages = new ArrayList<PageInfo>();
    }

    /**
     * Liefert den URL der Seite zurück, welcher zum Herunterladen benutzt wird
     *
     * @return URL
     */
    public URL getURL() {
        return url;
    }

    /**
     * Liefert den Namen der zugeordneten Sammlung. Kann null sein, wenn diese Seite
     * keiner Sammlung zugeordnet werden kann, sondern beispielsweise selbst eine
     * Reihe von Verweisen auf Sammlungen enthält (wie die Ausgangsseite von Wikipedia).
     *
     * @return Name der Sammlung
     */
    public String getCollection() {
        return collection;
    }

    public void setCollection(String name) {
        this.collection = name;
    }

    /**
     * Gibt an, ob diese Seite Namen von Künstlern enthält, die extrahiert werden müssen.
     * Extrahierte Namen werden der entsprechenden Sammlung zugeordnet (getCollection).
     *
     * @return True wenn Namen extrahiert werden sollen
     */
    public boolean containsNames() {
        return gotNames;
    }

    /**
     * Gibt an, ob diese Seite Verweise auf weitere Seiten enthält, die extrahiert werden
     * müssen.
     *
     * @return True, wenn Verweise auf Seiten extrahiert werden sollen
     */
    public boolean containsPages() {
        return gotPages;
    }

    /**
     * Liefert die Liste der etwaigen Unterseiten
     *
     * @return Liste von Seiten
     */
    public List<PageInfo> getSubPages() {
        return subPages;
    }

    /**
     * Liefert die Liste der auf dieser Seite gefundenen Namen
     * mit zugeordneten Kunstsammlungen.
     *
     * @return Liste von Namen und Sammlungen
     */
    public List<CollectionAndArtist> getNames() {
        return names;
    }

    public void addName(CollectionAndArtist name) {
        names.add(name);
    }

    public void addNames(Collection<CollectionAndArtist> names) {
        for (CollectionAndArtist n : names) {
            addName(n);
        }
    }

    public void addSubPage(PageInfo page) {
        subPages.add(page);
    }

    public void addSubPages(Collection<PageInfo> pages) {
        for (PageInfo p : pages) {
            subPages.add(p);
        }
    }
}
