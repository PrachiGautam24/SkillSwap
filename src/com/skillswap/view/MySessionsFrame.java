package com.skillswap.view;

import com.skillswap.dao.BookingDAO;
import com.skillswap.dao.SessionDAO;
import com.skillswap.model.Booking;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class MySessionsFrame extends JFrame {
    private final User currentUser;
    private final Runnable onChange;
    private final SessionDAO sessionDAO = new SessionDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JTable bookingTable = SwingUtils.createTable("No bookings match this view.");
    private final JTable offeringTable = SwingUtils.createTable("Your teaching offers will appear here.");
    private final JTable requestingTable = SwingUtils.createTable("Your requests for help will appear here.");
    private final JComboBox<String> roleFilter = new JComboBox<>(new String[]{"All bookings", "As learner", "As host"});
    private final JLabel summary = SwingUtils.createLabel("", SwingUtils.BODY_FONT, SwingUtils.TEXT_SECONDARY);
    private final JPanel hostActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JButton manage = SwingUtils.createStyledButton("View bookings", SwingUtils.PRIMARY_DARK);
    private final JButton edit = SwingUtils.createStyledButton("Edit", SwingUtils.CARD);
    private final JButton delete = SwingUtils.createStyledButton("Delete", SwingUtils.DANGER);
    private List<Session> offerings = Collections.emptyList(), requests = Collections.emptyList();
    private List<Booking> bookings = Collections.emptyList();

    public MySessionsFrame(User user) { this(user, null); }

    MySessionsFrame(User user, Runnable onChange) {
        currentUser = user;
        this.onChange = onChange;
        setTitle("SkillSwap - My Sessions");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 540);
        SwingUtils.centerFrame(this);

        JPanel page = SwingUtils.page();
        setContentPane(page);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tools.setOpaque(false);
        JButton refresh = SwingUtils.createStyledButton("Refresh", SwingUtils.CARD);
        JButton create = SwingUtils.createStyledButton("+ Create session", SwingUtils.PRIMARY_DARK);
        refresh.addActionListener(e -> loadSessions());
        create.addActionListener(e -> openEditor(null));
        tools.add(refresh);
        tools.add(create);
        page.add(SwingUtils.heading("My Sessions", tools), BorderLayout.NORTH);

        tabs.setFont(SwingUtils.BOLD_FONT);
        tabs.setBackground(SwingUtils.CARD);
        tabs.setForeground(SwingUtils.TEXT_PRIMARY);
        tabs.addTab("Bookings", new JScrollPane(bookingTable));
        tabs.addTab("My offers", new JScrollPane(offeringTable));
        tabs.addTab("My help requests", new JScrollPane(requestingTable));
        page.add(tabs, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        footer.add(summary, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        roleFilter.getAccessibleContext().setAccessibleName("Filter bookings by your role");
        roleFilter.addActionListener(e -> showBookings());
        actions.add(roleFilter);
        hostActions.setOpaque(false);
        manage.addActionListener(e -> {
            Session s = selectedPost();
            if (s != null) new SessionBookingsDialog(this, s, this::sessionChanged).setVisible(true);
        });
        edit.addActionListener(e -> {
            Session s = selectedPost();
            if (s != null) openEditor(s);
        });
        delete.addActionListener(e -> deletePost());
        hostActions.add(manage);
        hostActions.add(edit);
        hostActions.add(delete);
        actions.add(hostActions);
        footer.add(actions, BorderLayout.EAST);
        page.add(footer, BorderLayout.SOUTH);

        tabs.addChangeListener(e -> updateActions());
        offeringTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        requestingTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        loadSessions();
    }

    void loadSessions() {
        int id = currentUser.getId();
        bookings = bookingDAO.getBookingsByUser(id);
        offerings = sessionDAO.getSessionsByUser(id, "offering");
        requests = sessionDAO.getSessionsByUser(id, "requesting");
        offeringTable.setModel(postModel(offerings));
        requestingTable.setModel(postModel(requests));
        tabs.setTitleAt(0, "Bookings (" + bookings.size() + ")");
        tabs.setTitleAt(1, "My offers (" + offerings.size() + ")");
        tabs.setTitleAt(2, "My help requests (" + requests.size() + ")");
        showBookings();
    }

    void showAllBookings() {
        tabs.setSelectedIndex(0);
        roleFilter.setSelectedIndex(0);
    }

    private void showBookings() {
        DefaultTableModel model = SwingUtils.readOnlyModel("Session", "Partner", "Your role", "Skill", "Scheduled for", "Status");
        for (Booking b : bookings) {
            boolean learner = b.getLearnerId() == currentUser.getId();
            if ((roleFilter.getSelectedIndex() == 1 && !learner) || (roleFilter.getSelectedIndex() == 2 && learner)) continue;
            model.addRow(new Object[]{b.getSessionTitle(), b.getPartnerName(currentUser.getId()), learner ? "Learner" : "Host",
                    b.getSkill(), b.getScheduledAt(), b.getStatus().toUpperCase(java.util.Locale.ROOT)});
        }
        bookingTable.setModel(model);
        int[] widths = {240, 145, 85, 120, 155, 110};
        for (int i = 0; i < widths.length; i++) bookingTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        updateActions();
    }

    private DefaultTableModel postModel(List<Session> sessions) {
        DefaultTableModel model = SwingUtils.readOnlyModel("Session", "Skill", "Scheduled for", "Status", "Bookings");
        for (Session s : sessions) {
            model.addRow(new Object[]{s.getTitle(), s.getSkill(), s.getScheduledAt(), s.getStatus().toUpperCase(java.util.Locale.ROOT), s.getBookingCount()});
        }
        return model;
    }

    private Session selectedPost() {
        int tab = tabs.getSelectedIndex();
        if (tab == 0) return null;
        JTable table = tab == 1 ? offeringTable : requestingTable;
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (tab == 1 ? offerings : requests).get(table.convertRowIndexToModel(row));
    }

    private void updateActions() {
        int tab = tabs.getSelectedIndex();
        roleFilter.setVisible(tab == 0);
        hostActions.setVisible(tab != 0);
        boolean selected = tab == 1 ? offeringTable.getSelectedRow() >= 0 : requestingTable.getSelectedRow() >= 0;
        manage.setEnabled(selected);
        edit.setEnabled(selected);
        delete.setEnabled(selected);
        if (tab == 0) {
            int count = bookingTable.getRowCount();
            summary.setText(count == 0 ? "No bookings in this view." : count + (count == 1 ? " booking" : " bookings"));
        } else {
            boolean empty = (tab == 1 ? offerings : requests).isEmpty();
            summary.setText(empty ? "You haven't posted any " + (tab == 1 ? "offers" : "help requests") + " yet."
                    : "Select a session to manage it.");
        }
    }

    private void sessionChanged() {
        if (onChange == null) loadSessions();
        else onChange.run();
    }

    private void openEditor(Session session) {
        CreateSessionDialog dialog = new CreateSessionDialog(this, currentUser, session, this::sessionChanged);
        dialog.setVisible(true);
        Session saved = dialog.getSavedSession();
        if (saved != null) tabs.setSelectedIndex("offering".equals(saved.getSessionType()) ? 1 : 2);
    }

    private void deletePost() {
        Session s = selectedPost();
        if (s == null || !SwingUtils.confirm(this, "Delete '" + s.getTitle() + "' and its bookings?")) return;
        if (sessionDAO.deleteSession(s.getId())) sessionChanged();
        else SwingUtils.showError(this, "Could not delete this session. Refresh and try again.");
    }
}
