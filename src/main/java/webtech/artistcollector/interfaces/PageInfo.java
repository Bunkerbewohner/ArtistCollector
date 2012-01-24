package webtech.artistcollector.interfaces;

import webtech.artistcollector.data.CollectionAndArtist;

import java.net.URL;
import java.util.List;

/**
 * Interface für Seiteninfos. Eine Seite benötigt mindestens einen URL, über den sie abgerufen werden kann.
 * Außerdem muss eine Sammlung zugeordnet werden können, sonst ist die Seite wertlos. Außnahme bilden
 * Seiten, die Sammlungen auflisten.
 */
public interface PageInfo {
    /**
     * Liefert den URL der Seite zurück, welcher zum Herunterladen benutzt wird
     * @return URL
     */
    URL getURL();

    /**
     * Liefert den Namen der zugeordneten Sammlung. Kann null sein, wenn diese Seite
     * keiner Sammlung zugeordnet werden kann, sondern beispielsweise selbst eine
     * Reihe von Verweisen auf Sammlungen enthält (wie die Ausgangsseite von Wikipedia).
     * @return Name der Sammlung
     */
    String getCollection();

    /**
     * Setzt den Namen der zugeordneten Sammlung.
     * @param collection Name der Sammlung
     * @see webtech.artistcollector.interfaces.PageInfo#getCollection()
     */
    void setCollection(String collection);

    /**
     * Gibt an, ob diese Seite Namen von Künstlern enthält, die extrahiert werden müssen.
     * Extrahierte Namen werden der entsprechenden Sammlung zugeordnet (getCollection).
     * @return True wenn Namen extrahiert werden sollen
     */
    boolean containsNames();

    /**
     * Gibt an, ob diese Seite Verweise auf weitere Seiten enthält, die extrahiert werden
     * müssen.
     * @return True, wenn Verweise auf Seiten extrahiert werden sollen
     */
    boolean containsPages();

    /**
     * Liefert die Liste der etwaigen Unterseiten
     * @return Liste von Seiten
     */
    List<PageInfo> getSubPages();

    /**
     * Liefert die Liste der auf dieser Seite gefundenen Namen
     * mit zugeordneten Kunstsammlungen.
     * @return Liste von Namen und Sammlungen
     */
    List<CollectionAndArtist> getNames();

    /**
     * Fügt einen Namen und die zugehörige Sammlung zur Seiteninformation hinzu.
     * @param name Name und Sammlung
     */
    void addName(CollectionAndArtist name);

    /**
     * Zählt die Anzahl der Namen, die auf dieser Seite und ihren Unterseiten gefunden wurden.
     * @return Anzahl Namen
     */
    int countNames();
}
