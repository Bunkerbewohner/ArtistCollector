package webtech.artistcollector.interfaces;

/**
 * Gemeinsame Schnittstelle für Extraktoren
 */
public interface Extractor {

    /**
     * Gibt an, ob dieser Extraktor selektiert ist.
     * @return True, wenn selektiert
     * @see Extractor#setSelected(boolean)
     */
    boolean isSelected();

    /**
     * Setzt den Zustand auf selektiert oder nicht selektiert.
     * Nur selektierte Extraktoren werden vom Crawler benutzt.
     * Die Selektion wird vom Nutzer per GUI vorgenommen.
     * @param selected
     */
    void setSelected(boolean selected);

}
