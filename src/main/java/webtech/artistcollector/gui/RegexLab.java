package webtech.artistcollector.gui;

import webtech.artistcollector.data.Page;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexLab extends JFrame implements ActionListener, KeyListener {

    Page page;
    JEditorPane editor;
    JTextField input;
    JButton evalButton;
    JButton nextMatchButton;
    JButton prevMatchButton;
    JLabel statusLabel;

    int currentMatch = -1;
    int numMatches = 0;
    Object selectionHighlight = null;

    public RegexLab(Page page) {
        this.page = page;
        setupLayout();
    }

    private void setupLayout() {
        setTitle("Regex Lab for '" + page.getURL().toString() + "'");
        JPanel content = new JPanel(new BorderLayout());

        editor = new JEditorPane("text/plain", page.getContent());
        editor.setEditable(false);
        editor.requestFocusInWindow();

        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());

        evalButton = new JButton("evaluate regex");
        evalButton.addActionListener(this);

        input = new JTextField();
        input.addKeyListener(this);
        this.addKeyListener(this);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        statusLabel = new JLabel("^^ Regex eingeben");
        statusBar.add(statusLabel);

        nextMatchButton = new JButton("next");
        nextMatchButton.addActionListener(this);
        prevMatchButton = new JButton("prev");
        prevMatchButton.addActionListener(this);

        statusBar.add(prevMatchButton);
        statusBar.add(nextMatchButton);

        statusBar.setPreferredSize(new Dimension(800, 40));

        inputPanel.add(input, BorderLayout.CENTER);
        inputPanel.add(evalButton, BorderLayout.EAST);
        inputPanel.add(statusBar, BorderLayout.SOUTH);
        content.add(inputPanel, BorderLayout.NORTH);

        setSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setContentPane(content);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == evalButton) {
            evaluateRegex();
        } else if (e.getSource() == nextMatchButton) {
            jumpToNextMatch();
        } else if (e.getSource() == prevMatchButton) {
            jumpToPrevMatch();
        }
    }

    private void evaluateRegex() {
        String regex = input.getText();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(editor.getText());
        clearHighlights();
        numMatches = 0;
        currentMatch = 0;

        while (matcher.find()) {
            highlight(matcher.start(), matcher.end(), Color.YELLOW);
            numMatches++;
        }

        statusLabel.setText(numMatches + " Matches");
        if (numMatches > 0)
            jumpToMatch(0);
    }

    void jumpToMatch(int match) {
        if (numMatches == 0) return;
        String regex = input.getText();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(editor.getText());
        int i = 0;

        while (matcher.find() && i != match) {
            i++;
        }

        editor.setCaretPosition(matcher.start());
        if (selectionHighlight != null) editor.getHighlighter().removeHighlight(selectionHighlight);
        removeHighlight(matcher.start(), matcher.end());
        selectionHighlight = highlight(matcher.start(), matcher.end(), Color.ORANGE);
    }

    private void removeHighlight(int start, int end) {
        Highlighter.Highlight remove = null;
        for (Highlighter.Highlight h : editor.getHighlighter().getHighlights()) {
            if (h.getStartOffset() == start && h.getEndOffset() == end) {
                remove = h;
                break;
            }
        }

        if (remove != null)
            editor.getHighlighter().removeHighlight(remove);
    }

    private Object highlight(int start, int end, Color color) {
        DefaultHighlighter.DefaultHighlightPainter highlightPainter =
                new DefaultHighlighter.DefaultHighlightPainter(color);
        try {
            return editor.getHighlighter().addHighlight(start, end, highlightPainter);
        } catch (BadLocationException e) {
            return null;
        }
    }

    private void clearHighlights() {
        editor.getHighlighter().removeAllHighlights();
    }

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key typed event.
     */
    public void keyTyped(KeyEvent e) {

    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key pressed event.
     */
    public void keyPressed(KeyEvent e) {

    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key released event.
     */
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            evaluateRegex();
        } else if (e.getKeyCode() == KeyEvent.VK_F3) {
            if (!e.isShiftDown()) jumpToNextMatch();
            else jumpToPrevMatch();
        }
    }

    void jumpToNextMatch() {
        if (numMatches == 0) return;
        currentMatch = (currentMatch + 1) % numMatches;
        jumpToMatch(currentMatch);
    }

    void jumpToPrevMatch() {
        if (numMatches == 0) return;
        currentMatch = (currentMatch - 1) % numMatches;
        jumpToMatch(currentMatch);
    }
}
