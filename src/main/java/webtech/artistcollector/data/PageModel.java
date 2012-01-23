package webtech.artistcollector.data;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PageModel implements TreeModel {

    Page root;
    List<TreeModelListener> modelListeners;
    HashMap<URL, Page> pageDir;

    public static class Name {
        public Page page;
        public String name;

        public Name(String name) {
            this.name = name;
        }

        public Object[] getPath() {
            List<Object> path = new ArrayList<Object>();
            path.add(page);

            Page p = page.parent;
            while (p != null)
            {
                path.add(p);
                p = p.parent;
            }

            Collections.reverse(path);
            return path.toArray();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public class Page {
        protected PageModel model;
        public Page parent;
        public URL url;
        public List<Page> children;
        public List<Name> names;
        public String collection;

        protected Page(URL url) {
            this.url = url;
            children = new ArrayList<Page>();
            names = new ArrayList<Name>();
        }

        public void addName(Name name) {
            names.add(name);
            name.page = this;
            model.notifyListeners(name, null);
        }

        public Object[] getPath() {
            List<Object> path = new ArrayList<Object>();
            path.add(this);

            Page p = this.parent;
            while (p != null)
            {
                path.add(p);
                p = p.parent;
            }

            Collections.reverse(path);
            return path.toArray();
        }

        @Override
        public String toString() {
            String str = null;
            try {
                str = URLDecoder.decode(url.toString(), "utf8");
            } catch (UnsupportedEncodingException e) {
                str = url.toString();
            }
            return str + " (" + (children.size() + names.size()) + ")";
        }
    }

    public Page createPage(URL url, Page parent) {
        Page page = new Page(url);
        page.model = this;
        page.parent = parent;
        if (parent != null) {
            parent.children.add(page);
        }
        notifyListeners(page, null);

        pageDir.put(url, page);

        return page;
    }

    public Page findPage(URL url) {
        return pageDir.get(url);
    }

    void notifyListeners(Object created, Page modified) {
        for (TreeModelListener l : modelListeners) {
            if (created != null) {
                TreeModelEvent e = null;
                if (created instanceof Name) {
                    e = new TreeModelEvent(this, ((Name)created).getPath());
                } else if (created instanceof Page) {
                    e = new TreeModelEvent(this, ((Page)created).getPath());
                } else {
                    throw new NotImplementedException();
                }

                l.treeNodesInserted(e);
                //l.treeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
            }
        }
    }

    public Page getRootPage() {
        return root;
    }

    public PageModel(URL startURL) {
        root = new Page(startURL);
        modelListeners = new ArrayList<TreeModelListener>();
        pageDir = new HashMap<URL, Page>();
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof Page) {
            Page p = (Page)parent;
            if (index >= p.children.size()) {
                int j = index - p.children.size();
                return p.names.get(j);
            } else {
                return p.children.get(index);
            }
        } else {
            return null;
        }
    }

    public int getChildCount(Object parent) {
        if (parent instanceof Name) {
            return 0;
        }
        Page p = (Page)parent;
        return p.children.size() + p.names.size();
    }

    public boolean isLeaf(Object node) {
        if (node instanceof Page) {
            Page p = (Page)node;
            return (p.children.size() + p.names.size()) == 0;
        } else if (node instanceof Name) {
            return true;
        } else {
            throw new NotImplementedException();
        }
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // todo: implement valueForPathChanged in PageModel
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof Page) {
            Page p = (Page)parent;

            if (child instanceof Page) {
                return p.children.indexOf(child);
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
}
