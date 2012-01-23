package webtech.artistcollector.interfaces;

import java.util.Collection;

/**
 * Interface für Crawler, die Verweise auf Seiten von einer HTML-Seite extrahieren.
 * Beachtet werden muss natürlich, dass nur relevante Seiten extrahiert werden.
 */
public interface PageExtractor extends Extractor {

    /**
     * Liefert den Namen des Extraktors für die GUI
     * @return Name
     */
    public String getExtractorName();

    /**
     * Liefert eine Menge von Seiten, die auf der gegebenen Seite referenziert wurden.
     * @param info Info über eine Seite, welche nach weiteren Seiten durchsucht werden soll
     * @return Liste von Seiten
     */
    Collection<PageInfo> extractPages(PageInfo info);

}
