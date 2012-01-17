package webtech.artistcollector;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
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
        startURL = Main.getInstance().getPageModel().getRootPage().url;
    }

    public void run() {
        started = true;

        Main.getInstance().reportStatus(0, "Collecting Pages...");

        List<URL> pages = new ArrayList<URL>();
        pages.addAll(collectPages(startURL));

        Main.getInstance().reportStatus(50, "Extracting Names...");

        for (URL url : pages) {
            PageModel.Page page = Main.getInstance().getPageModel().findPage(url);
            extractNames(page);
        }

        Main.getInstance().reportStatus(100, "Done.");
    }

    private Collection<String> extractNames(PageModel.Page page) {

        String pageHTML = Util.downloadURL(page.url);
        if (pageHTML == null) {
            System.out.println("## ERROR could not download '" + page.url + "'");
            return null;
        }

        // Assume names to be two words with a link
        Pattern[] patterns = new Pattern[] {
            Pattern.compile("<a href=\"([^\"]+)\"[^>]+>([^< ]+ [^< ]+)</a>")
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(pageHTML);

            while (m.find()) {
                String url = m.group(1);
                String name = m.group(2);

                if (url.startsWith("/")) url = "http://de.wikipedia.org" + url;
                if (url.startsWith("#") || url.contains("=") || url.matches(".*:[^/]+.*")) {
                    System.out.println("## DISCARDED NAME '" + url + "'");
                    continue;
                }

                System.out.println(name);
                page.addName(new PageModel.Name(name));
            }
        }

        return null;
    }

    private Collection<URL> collectPages(URL startURL) {
        String page = Util.downloadURL(startURL);
        List<URL> pages = new ArrayList<URL>();

        Pattern[] patterns = new Pattern[] {
            Pattern.compile("<a href=\"([^\"]+?)\"[^>]+>([^<]+?)</a>")
            //Pattern.compile("<li>[^<]*?<a href=\"([^\"]*?)\"[^>]+>([^<]*?)</a>", Pattern.DOTALL),
            //Pattern.compile("<tr>\\s+<td>.*?<a href=\"([^\"]*?)\"[^>]+>([^<]*?)</a>", Pattern.DOTALL)
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(page);

            while (m.find()) {
                String url = m.group(1);
                String title = m.group(2);

                if (url.startsWith("/")) url = "http://de.wikipedia.org" + url;
                if (url.startsWith("#") || url.contains("=") || url.matches(".*:[^/]+.*")) {
                    System.out.println("## DISCARDED '" + url + "'");
                    continue;
                }

                System.out.println(title + " @ " + url);
                try {
                    pages.add(new URL(url));
                    PageModel pageModel = Main.getInstance().getPageModel();
                    pageModel.createPage(new URL(url), pageModel.getRootPage());
                } catch (MalformedURLException e) {
                    System.out.println("Invalid URL '" + url + "'");
                }
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
