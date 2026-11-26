package com.skillswap.view;

import com.skillswap.dao.*;
import com.skillswap.model.*;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FindMatchesFrame extends JFrame {
    private final User currentUser;
    private final Runnable onBooked;
    private TableRowSorter<DefaultTableModel> sorter;
    private final JLabel results = SwingUtils.createLabel("", SwingUtils.BODY_FONT, SwingUtils.TEXT_SECONDARY);
    private JTextField searchField;
    private JComboBox<String> modeFilter;
    private JTable matchTable;
    private List<User> displayedUsers = new ArrayList<>();

    private final UserDAO userDAO = new UserDAO();
    private final RatingDAO ratingDAO = new RatingDAO();
    private final SessionDAO sessionDAO = new SessionDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    public FindMatchesFrame(User currentUser) {
        this(currentUser, () -> {});
    }

    FindMatchesFrame(User currentUser, Runnable onBooked) {
        this.currentUser = currentUser;
        this.onBooked = onBooked;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("SkillSwap - Find Learning Partners");
        setSize(980, 560);
        SwingUtils.centerFrame(this);
        setContentPane(SwingUtils.page());

        JPanel filters = new JPanel(new BorderLayout(10, 0));
        filters.setBackground(SwingUtils.CARD);
        filters.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        searchField = SwingUtils.createStyledTextField(18);
        modeFilter = new JComboBox<>(new String[]{"All Modes", "Online", "In-Person", "Hybrid"});

        JLabel searchLabel = SwingUtils.createLabel("Name or skill", SwingUtils.BOLD_FONT, SwingUtils.TEXT_PRIMARY);
        searchLabel.setLabelFor(searchField);
        filters.add(searchLabel, BorderLayout.WEST);
        filters.add(searchField, BorderLayout.CENTER);
        JPanel mode = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        mode.setOpaque(false);
        JLabel modeLabel = SwingUtils.createLabel("Learning mode", SwingUtils.BODY_FONT, SwingUtils.TEXT_SECONDARY);
        modeLabel.setLabelFor(modeFilter);
        mode.add(modeLabel);
        mode.add(modeFilter);
        filters.add(mode, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout(0, 14));
        header.setOpaque(false);
        header.add(SwingUtils.heading("Find matches", results), BorderLayout.NORTH);
        header.add(filters, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        matchTable = SwingUtils.createTable("No peers match your search and learning mode.");
        add(new JScrollPane(matchTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomPanel.setOpaque(false);
        JButton viewProfileBtn = SwingUtils.createStyledButton("View profile", SwingUtils.CARD);
        JButton bookSessionBtn = SwingUtils.createStyledButton("Book Open Session", SwingUtils.SUCCESS);
        JButton sendReqBtn = SwingUtils.createStyledButton("Send Exchange Request", SwingUtils.PRIMARY_DARK);

        viewProfileBtn.addActionListener(e -> viewSelectedProfile());
        bookSessionBtn.addActionListener(e -> bookSelectedUserSession());
        sendReqBtn.addActionListener(e -> openSendRequestDialog());

        bottomPanel.add(viewProfileBtn);
        bottomPanel.add(bookSessionBtn);
        bottomPanel.add(sendReqBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadMatches();
        for (JButton action : new JButton[]{viewProfileBtn, bookSessionBtn, sendReqBtn}) {
            action.setEnabled(false);
            matchTable.getSelectionModel().addListSelectionListener(e -> action.setEnabled(matchTable.getSelectedRow() >= 0));
        }
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterMatches(); }
            public void removeUpdate(DocumentEvent e) { filterMatches(); }
            public void changedUpdate(DocumentEvent e) { filterMatches(); }
        });
        modeFilter.addActionListener(e -> filterMatches());
    }

    void loadMatches() {
        DefaultTableModel model = SwingUtils.readOnlyModel("Name", "Skills", "Skills to Learn", "Learning Mode", "Match Score", "Department", "Rating");
        displayedUsers.clear();
        List<User> all = userDAO.getAllUsers();
        for (User u : all) {
            if (u.getId() == currentUser.getId()) continue;

            displayedUsers.add(u);
            int score = SwingUtils.calculateMatchScore(currentUser, u);
            double rating = ratingDAO.getAverageRating(u.getId());
            model.addRow(new Object[]{u.getName(), u.getSkills(), u.getSkillsToLearn(), u.getLearningMode(),
                    score + "%", u.getDepartment(), rating > 0 ? String.format("%.1f / 5", rating) : "New"});
        }
        matchTable.setModel(model);
        sorter = new TableRowSorter<>(model);
        sorter.setComparator(4, (a, b) -> Integer.compare(Integer.parseInt(a.toString().replace("%", "")),
                Integer.parseInt(b.toString().replace("%", ""))));
        sorter.setSortKeys(java.util.Collections.singletonList(new RowSorter.SortKey(4, SortOrder.DESCENDING)));
        matchTable.setRowSorter(sorter);
        filterMatches();
    }

    private void filterMatches() {
        String query = searchField.getText().trim();
        String mode = (String) modeFilter.getSelectedItem();
        RowFilter<DefaultTableModel, Integer> search = RowFilter.regexFilter("(?iu)" + Pattern.quote(query), 0, 1, 2);
        if ("All Modes".equals(mode)) sorter.setRowFilter(search);
        else {
            RowFilter<DefaultTableModel, Integer> byMode = RowFilter.regexFilter("(?i)^" + Pattern.quote(mode) + "$", 3);
            sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(search, byMode)));
        }
        int count = matchTable.getRowCount();
        results.setText(count + (count == 1 ? " peer" : " peers"));
    }

    private User getSelectedUser() {
        int row = matchTable.getSelectedRow();
        if (row < 0) {
            SwingUtils.showError(this, "Please select a student from the table first.");
            return null;
        }
        return displayedUsers.get(matchTable.convertRowIndexToModel(row));
    }

    private void viewSelectedProfile() {
        User target = getSelectedUser();
        if (target == null) return;
        JDialog dialog = new JDialog(this, target.getName() + "'s Profile", true);
        FormPage page = new FormPage(target.getName());
        page.section("Skill exchange");
        page.detail("Can teach", target.getSkills());
        page.detail("Wants to learn", target.getSkillsToLearn());
        page.detail("Learning mode", target.getLearningMode());
        page.detail("Preferred time", target.getPreferredTime());
        page.section("Student details");
        page.detail("Department", target.getDepartment());
        page.detail("Semester", target.getSemester());
        page.detail("Student ID", target.getStudentId());
        page.detail("Bio", target.getBio());
        JButton close = SwingUtils.createStyledButton("Close", SwingUtils.CARD);
        close.addActionListener(e -> dialog.dispose());
        page.actions(close);
        dialog.setContentPane(page);
        dialog.getRootPane().setDefaultButton(close);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(550, 560);
        SwingUtils.centerFrame(dialog);
        dialog.setVisible(true);
    }

    private void bookSelectedUserSession() {
        User target = getSelectedUser();
        if (target == null) return;
        List<Session> openSessions;
        try {
            openSessions = sessionDAO.getBookableSessions(target.getId(), currentUser.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            SwingUtils.showError(this, "Could not load available sessions. Please try again.");
            return;
        }
        if (openSessions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sessions available for you to book with " + target.getName() +
                    ". Existing bookings appear in My Sessions.", "Available sessions", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JComboBox<String> choices = new JComboBox<>();
        for (Session s : openSessions) choices.addItem(s.getTitle() + "  |  " + s.getScheduledAt());
        JDialog picker = new JDialog(this, "Book a session with " + target.getName(), true);
        FormPage page = new FormPage("Book a session");
        page.detail("Host", target.getName());
        page.field("Session", choices);
        JButton cancel = SwingUtils.createStyledButton("Cancel", SwingUtils.CARD);
        JButton book = SwingUtils.createStyledButton("Book session", SwingUtils.PRIMARY_DARK);
        cancel.addActionListener(e -> picker.dispose());
        book.addActionListener(e -> {
            Session chosen = openSessions.get(choices.getSelectedIndex());
            picker.dispose();
            requestBooking(chosen);
        });
        page.actions(cancel, book);
        picker.setContentPane(page);
        picker.getRootPane().setDefaultButton(book);
        picker.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        picker.setSize(580, 295);
        SwingUtils.centerFrame(picker);
        picker.setVisible(true);
    }

    private void requestBooking(Session chosen) {
        if (bookingDAO.createBooking(chosen.getId(), currentUser.getId())) {
            onBooked.run();
            SwingUtils.showSuccess(this, chosen.getTitle() + " is pending confirmation. Track it in My Sessions.");
        } else {
            SwingUtils.showError(this, "Booking could not be saved. The session may be full, in the past, or you've already booked it. Check My Sessions.");
        }
    }

    private void openSendRequestDialog() {
        User target = getSelectedUser();
        if (target == null) return;
        new SendRequestDialog(this, currentUser, target).setVisible(true);
    }
}
