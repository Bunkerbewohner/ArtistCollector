package webtech.artistcollector.data;

import com.sun.org.apache.xpath.internal.operations.Bool;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import webtech.artistcollector.interfaces.PageInfo;
import webtech.artistcollector.misc.Util;

import javax.naming.ldap.PagedResultsControl;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * Allgemeine Informationen über eine Website, in Bezug
 * auf das Problem (Finden von Künstlernamen zu Sammlungen).
 */
public class Page implements PageInfo, TreeModel {

    protected Page parent = null;
    protected URL url;
    protected String collection;
    protected boolean gotNames;
    protected boolean gotPages;
    protected List<CollectionAndArtist> names;
    protected List<PageInfo> subPages;
    protected List<TreeModelListener> modelListeners;
    protected String content = null;

    public Page(URL url, String collection, boolean gotNames, boolean gotPages) {
        modelListeners = new ArrayList<TreeModelListener>();
        this.url = url;
        this.collection = collection;
        this.gotNames = gotNames;
        this.gotPages = gotPages;
        names = new ArrayList<CollectionAndArtist>();
        subPages = new ArrayList<PageInfo>();
    }

    public String getContent() {
        if (content == null) {
            content = Util.downloadURL(getURL());
        }

        return content;
    }

    public void releaseContent() {
        content = null;
    }

    public void setParent(Page parent) {
        this.parent = parent;
    }

    /**
     * Liefert den URL der Seite zurück, welcher zum Herunterladen benutzt wird
     *
     * @return URL
     */
    public URL getURL() {
        return url;
    }

    public void setURL(URL url) {
        this.url = url;
        notifyListeners(null, this, null);
    }

    public void setContainsPages(boolean value) {
        this.gotPages = value;
    }

    public void setContainsNames(boolean value) {
        this.gotNames = value;
    }

    /**
     * Liefert den Namen der zugeordneten Sammlung. Kann null sein, wenn diese Seite
     * keiner Sammlung zugeordnet werden kann, sondern beispielsweise selbst eine
     * Reihe von Verweisen auf Sammlungen enthält (wie die Ausgangsseite von Wikipedia).
     *
     * @return Name der Sammlung
     */
    public String getCollection() {
        return collection;
    }

    public void setCollection(String name) {
        this.collection = name;
    }

    /**
     * Gibt an, ob diese Seite Namen von Künstlern enthält, die extrahiert werden müssen.
     * Extrahierte Namen werden der entsprechenden Sammlung zugeordnet (getCollection).
     *
     * @return True wenn Namen extrahiert werden sollen
     */
    public boolean containsNames() {
        return gotNames;
    }

    /**
     * Gibt an, ob diese Seite Verweise auf weitere Seiten enthält, die extrahiert werden
     * müssen.
     *
     * @return True, wenn Verweise auf Seiten extrahiert werden sollen
     */
    public boolean containsPages() {
        return gotPages;
    }

    /**
     * Liefert die Liste der etwaigen Unterseiten
     *
     * @return Liste von Seiten
     */
    public List<PageInfo> getSubPages() {
        return subPages;
    }

    /**
     * Liefert die Liste der auf dieser Seite gefundenen Namen
     * mit zugeordneten Kunstsammlungen.
     *
     * @return Liste von Namen und Sammlungen
     */
    public List<CollectionAndArtist> getNames() {
        return names;
    }

    public void addName(CollectionAndArtist name) {
        name.page = this;
        names.add(name);
        notifyListeners(name, this, null);
    }

    public void addNames(Collection<CollectionAndArtist> names) {
        for (CollectionAndArtist n : names) {
            addName(n);
        }

        notifyListeners(null, findRoot(this), null);
    }

    public void addSubPage(PageInfo page) {
        ((Page) page).parent = this;
        subPages.add(page);
        notifyListeners(page, null, null);
    }

    public void addSubPages(Collection<PageInfo> pages) {
        for (PageInfo p : pages) {
            subPages.add(p);
        }
    }

    public void removePage(TreePath path) {
        Page parent = getPage(path).parent;
        if (parent == null) return;

        if (parent.subPages.remove(path.getLastPathComponent())) {
            notifyListeners(null, null, path.getLastPathComponent());
        }
    }

    @Override
    public String toString() {
        try {
            return URLDecoder.decode(url.toString(), "utf8") + " (" + countNames() + ")";
        } catch (UnsupportedEncodingException e) {
            return url.toString() + " (" + countNames() + ")";
        }
    }

    public int countNames() {
        return countNames(this);
    }

    int countNames(Page page) {
        return names.size() + countNames(subPages);
    }

    int countNames(Collection<PageInfo> pages) {
        int sum = 0;
        for (PageInfo p : pages) {
            sum += p.countNames();
        }
        return sum;
    }

    /* TreeModel-Implementierung: Anfang */

    public Object[] getPath(Object obj) {
        if (obj instanceof CollectionAndArtist)
            return getPath((CollectionAndArtist)obj);
        else
            return getPath((Page)obj);
    }

    public Object[] getPath(CollectionAndArtist name) {
        List<Object> path = new ArrayList<Object>();
        if (name.page != null)
            path.addAll(Arrays.asList(getPath(name.page)));
        path.add(name);
        return path.toArray();
    }

    public Object[] getPath(Page page) {
        List<Object> path = new ArrayList<Object>();
        path.add(page);

        Page p = page.parent;
        while (p != null) {
            path.add(p);
            p = p.parent;
        }

        Collections.reverse(path);
        return path.toArray();
    }

    void notifyListeners(Object created, Page modified, Object deleted) {
        Page root = null;
        if (created != null) root = findRoot(created);
        else if (modified != null) root = findRoot(modified);
        else if (deleted != null) root = findRoot(deleted);
        else throw new RuntimeException("Root was not found");

        List<TreeModelListener> listeners = root.modelListeners;
        for (TreeModelListener l : listeners) {
            if (created != null) {
                TreeModelEvent e = new TreeModelEvent(this, getPath(created));
                l.treeNodesInserted(e);
            }

            if (modified != null) {
                TreeModelEvent e = new TreeModelEvent(this, getPath(modified));
                l.treeNodesChanged(e);
            }

            if (deleted != null) {
                TreeModelEvent e = new TreeModelEvent(this, getPath(deleted));
                l.treeNodesRemoved(e);
                l.treeStructureChanged(new TreeModelEvent(this, getPath(root)));
            }
        }
    }

    public Page getPage(TreePath path) {
        Object[] way = path.getPath();
        PageInfo page = (PageInfo)way[0];
        for (int i = 1; i < path.getPathCount(); i++) {
            boolean found = false;
            for (PageInfo p : page.getSubPages()) {
                if (way[i] == p) {
                    page = p;
                    found = true;
                    break;
                }
            }

            if (!found) {
                page = null;
                break;
            }
        }

        return (Page)page;
    }

    public Object getRoot() {
        return findRoot(this);
    }

    protected Page findRoot(Page page) {
        if (page.parent != null)
            return findRoot(page.parent);
        else
            return page;
    }

    protected Page findRoot(Object obj) {
        if (obj instanceof CollectionAndArtist)
            return findRoot(((CollectionAndArtist)obj).page);
        else if (obj instanceof Page)
            return findRoot((Page)obj);
        else {
            System.out.println("findRoot(null)");
            return null;
        }
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof Page) {
            Page p = (Page) parent;
            if (index >= p.subPages.size()) {
                int j = index - p.subPages.size();
                return p.names.get(j);
            } else {
                return p.subPages.get(index);
            }
        } else {
            return null;
        }
    }

    public int getChildCount(Object parent) {
        if (parent instanceof CollectionAndArtist) {
            return 0;
        }
        Page p = (Page) parent;
        return p.subPages.size() + p.names.size();
    }

    public boolean isLeaf(Object node) {
        if (node instanceof Page) {
            Page p = (Page) node;
            return (p.subPages.size() + p.names.size()) == 0;
        } else if (node instanceof CollectionAndArtist) {
            return true;
        } else {
            throw new NotImplementedException();
        }
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // todo: implement valueForPathChanged in Page
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof Page) {
            Page p = (Page) parent;

            if (child instanceof Page) {
                return p.subPages.indexOf(child);
            } else {
                return p.names.indexOf(child);
            }
        }

        throw new NotImplementedException();
    }

    public void addTreeModelListener(TreeModelListener l) {
        modelListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        modelListeners.remove(l);
    }

    /* TreeModel-Implementierung: Ende */
}
