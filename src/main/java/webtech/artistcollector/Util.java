package webtech.artistcollector;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class Util {

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
