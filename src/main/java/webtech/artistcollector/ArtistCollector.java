package webtech.artistcollector;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtistCollector implements RunnableFuture<List<CollectionAndArtist>>{
    URL startURL;
    boolean started = false;
    boolean done = false;
    boolean cancelled = false;
    List<CollectionAndArtist> results = new ArrayList<CollectionAndArtist>();

    public ArtistCollector() {
        try {
            startURL = new URL("http://de.wikipedia.org/wiki/Liste_der_Sammlungen_moderner_oder_zeitgen%C3%B6ssischer_Kunst");
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(-1);
        }
    }

    public void run() {
        started = true;

        List<URL> pages = new ArrayList<URL>();
        pages.addAll(collectPages(startURL));
    }

    private Collection<URL> collectPages(URL startURL) {
        String page = Util.downloadURL(startURL);
        List<URL> pages = new ArrayList<URL>();

        Pattern p = Pattern.compile("<li>[^<]*?<a href=\"([^\"]*?)\"[^>]+>([^<]*?)</a>", Pattern.DOTALL);
        Matcher m = p.matcher(page);

        while (m.find()) {
            String url = m.group(1);
            String title = m.group(2);

            if (url.startsWith("/")) url = "http://de.wikipedia.org" + url;
            if (url.startsWith("#") || url.contains("=")) {
                System.out.println("## DISCARDED '" + url + "'");
                continue;
            }

            System.out.println(title + " @ " + url);
            try {
                pages.add(new URL(url));
            } catch (MalformedURLException e) {
                System.out.println("Invalid URL '" + url + "'");
            }
        }

        if (pages.size() == 0) {
            System.out.println("No Pages found at '" + startURL.toString() + "'");
        } else {
            System.out.println(pages.size() + " Pages found at '" + startURL.toString() + "'");
        }

        return pages;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!started || done || cancelled) return false;

        if (mayInterruptIfRunning) {
            // interrupt
            cancelled = true;
            done = true;
        } else {
            // still running but cannot interrupt
            cancelled = false;
        }

        return cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDone() {
        return done;
    }

    public List<CollectionAndArtist> get() throws InterruptedException, ExecutionException {
        return results;
    }

    public List<CollectionAndArtist> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new NotImplementedException();
    }
}
