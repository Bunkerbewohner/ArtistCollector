package webtech.artistcollector.crawler;

import org.apache.commons.configuration.PropertiesConfiguration;
import webtech.artistcollector.crawler.extractors.GeneralRegexNameExtractor;
import webtech.artistcollector.interfaces.Extractor;
import webtech.artistcollector.interfaces.NameExtractor;
import webtech.artistcollector.interfaces.PageExtractor;

import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

        try {
            loadGeneralRegexNameExtractors();
        } catch (IOException e) {
            System.out.println("Could not load any general extractors");
            e.printStackTrace();
        }
    }

    void loadGeneralRegexNameExtractors() throws IOException {
        CodeSource src = Extractors.class.getProtectionDomain().getCodeSource();
        List<String> list = new ArrayList<String>();

        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            ZipEntry ze = null;

            while ((ze = zip.getNextEntry()) != null) {
                String entryName = ze.getName();
                if (entryName.contains("crawler.extractors") && entryName.endsWith(".ini")) {
                    list.add(entryName);
                    System.out.println("Loading extractor '" + entryName + "'");
                }
            }

        }

        for (String filename : list) {
            URL configURL = this.getClass().getResource(filename);
            try {
                GeneralRegexNameExtractor.Params params = new GeneralRegexNameExtractor.Params(configURL);
                NameExtractor ex = new GeneralRegexNameExtractor(params);
                registerExtractor(ex);
            } catch (Exception e) {
                System.out.println("Extractor Config could not be loaded");
                e.printStackTrace();
            }
        }
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
