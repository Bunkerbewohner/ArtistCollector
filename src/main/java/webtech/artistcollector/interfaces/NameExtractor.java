package webtech.artistcollector.interfaces;

import webtech.artistcollector.data.CollectionAndArtist;

import java.util.Collection;

/**
 * Schnittstelle für Crawler, die Künstlernamen aus einer Seite extrahieren.
 * Jedem Namen muss eine Sammlung zugeordnet werden - im Zweifel die der Seite zugeordnete.
 */
public interface NameExtractor extends Extractor {

    /**
     * Liefert den Namen des Extraktors für die GUI
     * @return Name
     */
    String getExtractorName();

    /**
     * Gegeben eine bestimmte Seite mit einer Zuordnung zu einer Kunstsammlung, liefert diese Funktion
     * eine Menge von Künstlernamen und den zugehörigen Sammlungen.
     * @param info Seite
     * @return Liste von Namen mit Sammlungszuordnung
     */
    Collection<CollectionAndArtist> extractNames(PageInfo info);

}
