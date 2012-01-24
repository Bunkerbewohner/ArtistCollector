package webtech.artistcollector.gui;

import webtech.artistcollector.data.PageModel;
import webtech.artistcollector.interfaces.PageInfo;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainWindow {
    private JTabbedPane tabbedPane1;
    private JPanel rootPanel;
    private JTree pageHierarchy;
    private JProgressBar progressBar1;
    private JButton startPageCollectorButton;
    private JTextPane logPane;
    private JButton cancelButton;
    private JButton saveToDBButton;

    public void reportStatus(float progressPercent, String statusMessage) {
        progressBar1.setMinimum(0);
        progressBar1.setMaximum(100);
        progressBar1.setValue((int)Math.floor(progressPercent));
        progressBar1.setStringPainted(true);
        progressBar1.setString(statusMessage);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JTextPane getLogPane() {
        return logPane;
    }

    final void eventCollectorStarted() {
        Main.getInstance().startPageCollector();
        startPageCollectorButton.setEnabled(false);
        cancelButton.setEnabled(true);
        saveToDBButton.setEnabled(false);
    }

    final void eventCollectorCancelled() {
        Main.getInstance().crawler.cancel(true);
        cancelButton.setEnabled(false);
        startPageCollectorButton.setEnabled(true);
        saveToDBButton.setEnabled(true);
    }

    final void eventCollectorFinished() {
        cancelButton.setEnabled(false);
        startPageCollectorButton.setEnabled(true);
        saveToDBButton.setEnabled(true);
    }

    final void eventSavingStarted() {
        saveToDBButton.setEnabled(false);
        startPageCollectorButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    public MainWindow() {

        startPageCollectorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eventCollectorStarted();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eventCollectorCancelled();
            }
        });

        saveToDBButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    public void run() {
                        Main.getInstance().saveToDB();
                    }
                }).start();
                eventSavingStarted();
            }
        });

        Main.getInstance().crawler.addEventListener(new CrawlerEventListener() {
            public void crawlerFinished(PageInfo results) {
                eventCollectorFinished();
            }
        });
    }

    private void updateTextPane(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Document doc = getLogPane().getDocument();
                try {
                    doc.insertString(doc.getLength(), text, null);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
                getLogPane().setCaretPosition(doc.getLength() - 1);
            }
        });
    }

    public void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                updateTextPane(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextPane(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    public void setTreeModel(TreeModel pageModel) {
        pageHierarchy.setModel(pageModel);
        pageModel.addTreeModelListener(new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        pageHierarchy.invalidate();
                    }
                });
            }

            public void treeNodesInserted(final TreeModelEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        pageHierarchy.invalidate();

                        TreePath firstChild = new TreePath(e.getPath()[0]);
                        if (!pageHierarchy.isExpanded(firstChild)) {
                            pageHierarchy.scrollPathToVisible(e.getTreePath().getParentPath());
                        }
                    }
                });
            }

            public void treeNodesRemoved(TreeModelEvent e) {

            }

            public void treeStructureChanged(TreeModelEvent e) {

            }
        });

        pageHierarchy.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {

            }

            public void keyPressed(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {
                if (pageHierarchy.getSelectionPath() != null && e.getKeyCode() == KeyEvent.VK_DELETE) {
                    Main.getInstance().crawler.remove(pageHierarchy.getSelectionPath());
                }
            }
        });
    }
}
