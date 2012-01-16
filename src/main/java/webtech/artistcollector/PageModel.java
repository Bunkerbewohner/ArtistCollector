package webtech.artistcollector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class PageModel implements TreeModel {

    Page root;
    List<TreeModelListener> modelListeners;

    public class Page {
        protected PageModel model;
        public Page parent;
        public URL url;
        public List<Page> children;

        protected Page(URL url) {
            this.url = url;
            children = new ArrayList<Page>();
        }

        @Override
        public String toString() {
            String str = null;
            try {
                str = URLDecoder.decode(url.toString(), "utf8");
            } catch (UnsupportedEncodingException e) {
                str = url.toString();
            }
            return str + " (" + children.size() + ")";
        }
    }

    public Page createPage(URL url, Page parent) {
        Page page = new Page(url);
        page.model = this;
        page.parent = parent;
        if (parent != null) {
            parent.children.add(page);
        }
        notifyListeners(page);

        return page;
    }

    void notifyListeners(Page created) {
        for (TreeModelListener l : modelListeners) {
            if (created != null) {
                l.treeNodesInserted(new TreeModelEvent(this,
                        new TreePath(created)));
            }
        }
    }

    public Page getRootPage() {
        return root;
    }

    public PageModel(URL startURL) {
        root = new Page(startURL);
        modelListeners = new ArrayList<TreeModelListener>();
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        return ((Page)parent).children.get(index);
    }

    public int getChildCount(Object parent) {
        return ((Page)parent).children.size();
    }

    public boolean isLeaf(Object node) {
        return ((Page)node).children.size() == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // todo: implement valueForPathChanged in PageModel
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((Page)parent).children.indexOf(child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        modelListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        modelListeners.remove(l);
    }
}
