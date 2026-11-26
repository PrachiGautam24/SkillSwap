package com.skillswap.view;

import com.skillswap.util.SwingUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** A scrollable form with a persistent heading and action bar. */
final class FormPage extends JPanel {
    private final JPanel fields = new JPanel(new GridBagLayout());
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private int row;

    FormPage(String title) {
        super(new BorderLayout(0, 16));
        setBackground(SwingUtils.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        add(SwingUtils.heading(title, null), BorderLayout.NORTH);
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createEmptyBorder(10, 16, 14, 16));

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(SwingUtils.CARD);
        body.add(fields, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(body);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        actions.setOpaque(false);
        add(actions, BorderLayout.SOUTH);
        registerKeyboardAction(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JDialog) window.dispose();
        }, KeyStroke.getKeyStroke("ESCAPE"), WHEN_IN_FOCUSED_WINDOW);
    }

    void section(String title) {
        JLabel label = SwingUtils.createLabel(title, SwingUtils.SUBTITLE_FONT, SwingUtils.PRIMARY);
        GridBagConstraints c = cell(0);
        c.gridwidth = 2;
        c.insets = new Insets(row == 0 ? 4 : 16, 0, 6, 0);
        fields.add(label, c);
        row++;
    }

    void field(String title, JComponent editor) {
        JLabel label = SwingUtils.createLabel(title, SwingUtils.BODY_FONT, SwingUtils.TEXT_PRIMARY);
        JComponent target = editor;
        if (editor instanceof JScrollPane) {
            JScrollPane pane = (JScrollPane) editor;
            target = (JComponent) pane.getViewport().getView();
            pane.setBorder(BorderFactory.createEmptyBorder());
        }
        label.setLabelFor(target);
        target.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { fields.scrollRectToVisible(editor.getBounds()); }
        });
        GridBagConstraints c = cell(0);
        c.insets = new Insets(7, 0, 7, 16);
        fields.add(label, c);
        c = cell(1);
        c.weightx = 1;
        fields.add(editor, c);
        row++;
    }

    private GridBagConstraints cell(int column) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = column;
        c.gridy = row;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        return c;
    }

    void detail(String title, String value) {
        JTextArea text = new JTextArea(value == null || value.trim().isEmpty() ? "Not provided" : value, 0, 20);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setOpaque(false);
        text.setFont(SwingUtils.BODY_FONT);
        text.setForeground(SwingUtils.TEXT_PRIMARY);
        text.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        field(title, text);
    }

    void actions(JButton... buttons) {
        for (JButton button : buttons) actions.add(button);
    }
}
