package com.skillswap.util;

import com.skillswap.model.User;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SwingUtils {
    public static final Color PRIMARY = new Color(139, 222, 207);
    public static final Color PRIMARY_DARK = new Color(28, 104, 98);
    public static final Color BACKGROUND = new Color(17, 26, 38);
    public static final Color CARD = new Color(29, 42, 57);
    public static final Color BORDER_COLOR = new Color(62, 81, 100);
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    public static final Color TEXT_SECONDARY = new Color(178, 194, 206);
    public static final Color SUCCESS = new Color(24, 112, 88);
    public static final Color DANGER = new Color(161, 56, 72);
    public static final Color MINT = new Color(113, 224, 187);
    public static final Color AMBER = new Color(248, 202, 115);

    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    public static final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 14);

    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
    );

    public static final Border INPUT_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
    );

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int res = JOptionPane.showConfirmDialog(parent, message, "Confirm Action", JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.YES_OPTION;
    }

    public static void centerFrame(Window window) {
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        window.setSize(Math.min(window.getWidth(), bounds.width), Math.min(window.getHeight(), bounds.height));
        window.setLocationRelativeTo(window.getOwner());
    }

    public static void installTheme() throws UnsupportedLookAndFeelException {
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme() {
            protected ColorUIResource getPrimary1() { return new ColorUIResource(PRIMARY); }
            protected ColorUIResource getPrimary2() { return new ColorUIResource(PRIMARY_DARK); }
            protected ColorUIResource getPrimary3() { return new ColorUIResource(PRIMARY_DARK); }
            protected ColorUIResource getSecondary1() { return new ColorUIResource(BORDER_COLOR); }
            protected ColorUIResource getSecondary2() { return new ColorUIResource(BORDER_COLOR); }
            protected ColorUIResource getSecondary3() { return new ColorUIResource(CARD); }
            protected ColorUIResource getBlack() { return new ColorUIResource(TEXT_PRIMARY); }
            protected ColorUIResource getWhite() { return new ColorUIResource(BACKGROUND); }
            public FontUIResource getControlTextFont() { return new FontUIResource(BODY_FONT); }
            public FontUIResource getSystemTextFont() { return new FontUIResource(BODY_FONT); }
            public FontUIResource getUserTextFont() { return new FontUIResource(BODY_FONT); }
            public ColorUIResource getHighlightedTextColor() { return new ColorUIResource(Color.WHITE); }
        });
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_DARK);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Menu.selectionForeground", Color.WHITE);
        UIManager.put("MenuItem.selectionForeground", Color.WHITE);
        UIManager.put("Menu.selectionBackground", PRIMARY_DARK);
        UIManager.put("MenuItem.selectionBackground", PRIMARY_DARK);
        UIManager.put("ToolTip.background", CARD);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", INPUT_BORDER);
        UIManager.put("ComboBox.padding", new Insets(4, 8, 4, 8));
        UIManager.put("TabbedPane.selected", PRIMARY_DARK);
        UIManager.put("TabbedPane.contentAreaColor", BACKGROUND);
        UIManager.put("TabbedPane.tabAreaBackground", BACKGROUND);
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 12, 8, 12));
        UIManager.put("TabbedPane.focus", PRIMARY);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDER_COLOR));
        UIManager.put("ScrollPane.viewportBorder", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBarUI", "javax.swing.plaf.basic.BasicScrollBarUI");
        UIManager.put("ScrollBar.width", 14);
        UIManager.put("ScrollBar.track", BACKGROUND);
        UIManager.put("ScrollBar.thumb", BORDER_COLOR);
        UIManager.put("ScrollBar.thumbHighlight", BORDER_COLOR);
        UIManager.put("ScrollBar.thumbShadow", BORDER_COLOR);
        UIManager.put("ScrollBar.thumbDarkShadow", BORDER_COLOR);
        UIManager.put("Button.disabledText", TEXT_SECONDARY);
    }

    public static JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D pen = (Graphics2D) g.create();
                pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel state = getModel();
                Color fill = getBackground();
                if (!isEnabled()) fill = BACKGROUND;
                else if (state.isPressed()) fill = fill.darker();
                else if (state.isRollover()) fill = fill.brighter();
                pen.setColor(fill);
                pen.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (isEnabled() && CARD.equals(getBackground())) {
                    pen.setColor(BORDER_COLOR);
                    pen.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                if (isFocusOwner()) {
                    pen.setColor(PRIMARY);
                    pen.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                }
                pen.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(BOLD_FONT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setRolloverEnabled(true);
        btn.setOpaque(false);
        return btn;
    }

    public static JTextField createStyledTextField(int cols) {
        JTextField tf = new JTextField(cols);
        styleInput(tf);
        return tf;
    }

    public static JPasswordField createStyledPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        styleInput(pf);
        return pf;
    }

    public static JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        styleInput(ta);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    private static void styleInput(JTextComponent field) {
        field.setFont(BODY_FONT);
        field.setBackground(BACKGROUND);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setSelectionColor(PRIMARY_DARK);
        field.setSelectedTextColor(Color.WHITE);
        field.setBorder(INPUT_BORDER);
        Border focus = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PRIMARY),
                BorderFactory.createEmptyBorder(6, 8, 6, 8));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { field.setBorder(focus); }
            public void focusLost(FocusEvent e) { field.setBorder(INPUT_BORDER); }
        });
    }

    public static JPanel page() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        return page;
    }

    public static JPanel heading(String title, JComponent action) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(createLabel(title, TITLE_FONT, TEXT_PRIMARY), BorderLayout.CENTER);
        if (action != null) header.add(action, BorderLayout.EAST);
        return header;
    }

    public static JLabel wordmark() {
        JLabel brand = createLabel("SkillSwap", TITLE_FONT, TEXT_PRIMARY);
        brand.setIcon(new Icon() {
            public int getIconWidth() { return 32; }
            public int getIconHeight() { return 32; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D pen = (Graphics2D) g.create();
                pen.translate(x, y);
                pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                pen.setColor(PRIMARY);
                pen.fillRoundRect(0, 0, 32, 32, 10, 10);
                pen.setColor(BACKGROUND);
                pen.setStroke(new BasicStroke(2.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                pen.drawPolyline(new int[]{8, 23, 19}, new int[]{12, 12, 8}, 3);
                pen.drawPolyline(new int[]{24, 9, 13}, new int[]{20, 20, 24}, 3);
                pen.dispose();
            }
        });
        brand.setIconTextGap(10);
        return brand;
    }

    public static JTable createTable(String emptyMessage) {
        JTable table = new JTable() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() != 0) return;
                Rectangle visible = getVisibleRect();
                g.setColor(TEXT_SECONDARY);
                g.setFont(BODY_FONT);
                FontMetrics font = g.getFontMetrics();
                int x = visible.x + Math.max(8, (visible.width - font.stringWidth(emptyMessage)) / 2);
                g.drawString(emptyMessage, x, visible.y + Math.min(48, Math.max(font.getAscent(), visible.height / 2)));
            }
        };
        styleTable(table);
        return table;
    }

    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    public static void styleTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(BODY_FONT);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(PRIMARY_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BACKGROUND);
        header.setForeground(PRIMARY);
        header.setFont(BOLD_FONT);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        javax.swing.table.TableCellRenderer base = header.getDefaultRenderer();
        header.setDefaultRenderer((t, value, selected, focused, row, column) -> {
            Component cell = base.getTableCellRendererComponent(t, value, selected, focused, row, column);
            if (cell instanceof JLabel) {
                JLabel label = (JLabel) cell;
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
            }
            return cell;
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 6));
                setBackground(selected ? t.getSelectionBackground() : (row % 2 == 0 ? CARD : BACKGROUND));
                setForeground(TEXT_PRIMARY);
                setFont(BODY_FONT);
                if ("Status".equals(t.getColumnName(column))) {
                    String status = String.valueOf(value).toLowerCase(java.util.Locale.ROOT);
                    setFont(BOLD_FONT);
                    if (!selected) {
                        switch (status) {
                            case "pending": setForeground(AMBER); break;
                            case "confirmed": case "completed": case "open": case "accepted":
                                setForeground(MINT); break;
                            case "cancelled": case "rejected": setForeground(new Color(252, 157, 166)); break;
                            default: break;
                        }
                    }
                }
                setToolTipText(value == null ? null : value.toString());
                return this;
            }
        });
    }

    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public static int calculateMatchScore(User current, User other) {
        int score = 0;
        Set<String> myNeeds = parseSkills(current.getSkillsToLearn());
        Set<String> mySkills = parseSkills(current.getSkills());
        Set<String> theirSkills = parseSkills(other.getSkills());
        Set<String> theirNeeds = parseSkills(other.getSkillsToLearn());

        for (String need : myNeeds) {
            if (theirSkills.contains(need)) {
                score += 40;
                break;
            }
        }
        for (String skill : mySkills) {
            if (theirNeeds.contains(skill)) {
                score += 40;
                break;
            }
        }
        if (current.getLearningMode() != null &&
            current.getLearningMode().equalsIgnoreCase(other.getLearningMode())) {
            score += 20;
        }
        return score;
    }

    private static Set<String> parseSkills(String raw) {
        Set<String> set = new HashSet<>();
        if (raw == null || raw.trim().isEmpty()) return set;
        Arrays.stream(raw.split(","))
              .map(s -> s.trim().toLowerCase())
              .filter(s -> !s.isEmpty())
              .forEach(set::add);
        return set;
    }
}
