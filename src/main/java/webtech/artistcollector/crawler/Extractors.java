package webtech.artistcollector.crawler;

import webtech.artistcollector.interfaces.NameExtractor;
import webtech.artistcollector.interfaces.PageExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Registrierstelle für Extraktoren. Extraktoren, die vom User per GUI ausgewählt werden
 * können sollen, müssen per Extractors#registerExtractor registriert werden.
 */
public class Extractors {

    private static Extractors instance = new Extractors();

    protected List<PageExtractor> pageExtractors;
    protected List<NameExtractor> nameExtractors;

    public static Extractors getInstance() {
        return instance;
    }

    public Extractors() {
        pageExtractors = new ArrayList<PageExtractor>();
        nameExtractors = new ArrayList<NameExtractor>();
    }

    public void registerExtractor(PageExtractor extractor) {
        pageExtractors.add(extractor);
    }

    public void registerExtractor(NameExtractor extractor) {
        nameExtractors.add(extractor);
    }

    public List<PageExtractor> getPageExtractors() {
        return pageExtractors;
    }

    public List<NameExtractor> getNameExtractors() {
        return nameExtractors;
    }
}
