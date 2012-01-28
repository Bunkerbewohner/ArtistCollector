package webtech.artistcollector.crawler;

import org.apache.commons.configuration.PropertiesConfiguration;
import webtech.artistcollector.crawler.extractors.GeneralRegexNameExtractor;
import webtech.artistcollector.interfaces.Extractor;
import webtech.artistcollector.interfaces.NameExtractor;
import webtech.artistcollector.interfaces.PageExtractor;

import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
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

        // List all *.ini files either from jar or class folder
        boolean isJar = src.getLocation().toString().toLowerCase().endsWith(".jar");
        if (src != null && isJar) {
            // Executed from packed jar archive
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            ZipEntry ze = null;

            while ((ze = zip.getNextEntry()) != null) {
                String entryName = ze.getName();
                if (entryName.contains("crawler.extractors") && entryName.endsWith(".ini")) {
                    list.add(entryName);
                }
            }
        } else if (src != null) {
            // Executed from class folder
            File dir = new File("extractors");
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".ini");
                }
            };
            for (String filename : dir.list(filter)) {
                list.add("extractors/" + filename);
            }
        }

        // Try loading the extractors from the ini files
        for (String filename : list) {
            try {
                URL configURL = ClassLoader.getSystemResource(filename);
                File configFile = new File(filename);
                GeneralRegexNameExtractor.Params params = null;

                if (isJar) params = new GeneralRegexNameExtractor.Params(configURL);
                else params = new GeneralRegexNameExtractor.Params(configFile);

                NameExtractor ex = new GeneralRegexNameExtractor(params);
                registerExtractor(ex);

                System.out.println("Successfully loaded extractor from '" + filename + "'");
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
