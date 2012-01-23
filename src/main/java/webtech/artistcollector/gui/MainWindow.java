package webtech.artistcollector.gui;

import webtech.artistcollector.data.PageModel;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class MainWindow {
    private JTabbedPane tabbedPane1;
    private JPanel rootPanel;
    private JTree pageHierarchy;
    private JProgressBar progressBar1;
    private JButton startPageCollectorButton;
    private JTextPane logPane;

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

    public MainWindow() {

        startPageCollectorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Main.getInstance().startPageCollector();
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

    public void setTreeModel(PageModel pageModel) {
        pageHierarchy.setModel(pageModel);
        pageModel.addTreeModelListener(new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) {

            }

            public void treeNodesInserted(TreeModelEvent e) {

            }

            public void treeNodesRemoved(TreeModelEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void treeStructureChanged(TreeModelEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }
}
