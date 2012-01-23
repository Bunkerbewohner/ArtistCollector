package webtech.artistcollector.misc;


import webtech.artistcollector.data.CollectionAndArtist;
import webtech.artistcollector.data.PageModel;
import webtech.artistcollector.interfaces.PageInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Util {

    /**
     * Listet alle Namen (mit zugeordneten Sammlungen) auf, welche auf gegebener Seite,
     * und deren Unterseiten, enthalten sind.
     * @param page Seite
     * @return Liste von Namen
     */
    public static List<CollectionAndArtist> listNames(PageInfo page) {
        List<CollectionAndArtist> names = new ArrayList<CollectionAndArtist>();
        names.addAll(page.getNames());

        for (PageInfo p : page.getSubPages()) {
            names.addAll(listNames(page));
        }

        return names;
    }

    /**
     * Download the contents of given URL as a string.
     * @param url URL to HTML page or other string-based file
     * @return Contents of the page
     */
    public static String downloadURL(URL url) {
        InputStream in = null;

        try {
            in = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead = 1;

            while ((bytesRead = reader.read(buffer)) > 0) {
                reader.read(buffer);
                sb.append(buffer);
            }

            return sb.toString();
        } catch (IOException ex) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
