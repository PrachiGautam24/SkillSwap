package com.skillswap.view;

import com.skillswap.dao.*;
import com.skillswap.model.*;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Consumer;

public class DashboardFrame extends JFrame {
    private User currentUser;
    private JLabel welcomeLabel, sessionsCountLbl, bookingsCountLbl, matchesCountLbl;
    private JTable recentBookings;
    private List<Announcement> announcements;
    private int announcementIndex;
    private final JLabel announcementTitle = SwingUtils.createLabel("", SwingUtils.BOLD_FONT, SwingUtils.TEXT_PRIMARY);
    private final JTextArea announcementBody = new JTextArea();
    private final JLabel announcementMeta = SwingUtils.createLabel("", SwingUtils.BODY_FONT, SwingUtils.TEXT_SECONDARY);
    private final JButton previous = SwingUtils.createStyledButton("Previous", SwingUtils.CARD);
    private final JButton next = SwingUtils.createStyledButton("Next", SwingUtils.CARD);
    private final JLabel activityTitle = SwingUtils.createLabel("Recent bookings", SwingUtils.SUBTITLE_FONT, SwingUtils.TEXT_PRIMARY);
    private final Map<Class<? extends JFrame>, JFrame> windows = new HashMap<>();

    private final SessionDAO sessionDAO = new SessionDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final ExchangeRequestDAO exchangeRequestDAO = new ExchangeRequestDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    
    private JButton sessionsNavBtn, messagesNavBtn, requestsNavBtn;

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("SkillSwap - Dashboard");
        setSize(1020, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtils.centerFrame(this);
        setLayout(new BorderLayout());

        setupMenuBar();
        setupSidebar();
        setupCenterPanel();
        refreshDashboardData();
    }

    private <T extends JFrame> T openWindow(Class<T> type, Supplier<T> create, Consumer<T> refresh) {
        T frame = type.cast(windows.get(type));
        if (frame == null || !frame.isDisplayable()) {
            frame = create.get();
            frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            windows.put(type, frame);
        } else {
            refresh.accept(frame);
        }
        frame.setExtendedState(frame.getExtendedState() & ~Frame.ICONIFIED);
        frame.setVisible(true);
        frame.toFront();
        return frame;
    }

    private MySessionsFrame openSessions() {
        return openWindow(MySessionsFrame.class, () -> new MySessionsFrame(currentUser, this::sessionsChanged),
                MySessionsFrame::loadSessions);
    }

    private void sessionsChanged() {
        refreshDashboardData();
        MySessionsFrame sessions = (MySessionsFrame) windows.get(MySessionsFrame.class);
        if (sessions != null && sessions.isDisplayable()) sessions.loadSessions();
        MyExchangesFrame exchanges = (MyExchangesFrame) windows.get(MyExchangesFrame.class);
        if (exchanges != null && exchanges.isDisplayable()) exchanges.loadExchanges();
    }

    private void logout() {
        for (JFrame frame : windows.values()) frame.dispose();
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh Dashboard");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");

        refreshItem.addActionListener(e -> refreshDashboardData());
        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(refreshItem);
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu profileMenu = new JMenu("Profile");
        JMenuItem editProfileItem = new JMenuItem("Edit Profile");
        editProfileItem.addActionListener(e -> {
            new EditProfileDialog(this, currentUser, updated -> {
                this.currentUser = updated;
                refreshDashboardData();
            }).setVisible(true);
        });
        profileMenu.add(editProfileItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About SkillSwap");
        aboutItem.addActionListener(e -> SwingUtils.showSuccess(this,
                "SkillSwap Desktop v1.0\nPeer-to-Peer Student Learning & Skill Exchange Platform."));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(profileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void setupSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SwingUtils.CARD);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, SwingUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));

        JLabel brand = SwingUtils.wordmark();
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 0));
        sidebar.add(brand);

        addNavButton(sidebar, "Dashboard", e -> refreshDashboardData());
        addNavButton(sidebar, "Find Matches", e -> openWindow(FindMatchesFrame.class,
                () -> new FindMatchesFrame(currentUser, this::sessionsChanged), FindMatchesFrame::loadMatches));
        messagesNavBtn = addNavButton(sidebar, "Messages", e -> openWindow(MessagesFrame.class,
                () -> new MessagesFrame(currentUser), MessagesFrame::loadConversations));
        requestsNavBtn = addNavButton(sidebar, "My Requests", e -> openWindow(MyRequestsFrame.class,
                () -> new MyRequestsFrame(currentUser), MyRequestsFrame::loadTables));
        addNavButton(sidebar, "My Exchanges", e -> openWindow(MyExchangesFrame.class,
                () -> new MyExchangesFrame(currentUser), MyExchangesFrame::loadExchanges));
        sessionsNavBtn = addNavButton(sidebar, "My Sessions", e -> openSessions());
        addNavButton(sidebar, "Leaderboard", e -> openWindow(LeaderboardFrame.class,
                LeaderboardFrame::new, LeaderboardFrame::loadLeaderboard));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = SwingUtils.createStyledButton("Logout", SwingUtils.DANGER);
        logoutBtn.setMaximumSize(new Dimension(220, 38));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> logout());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        add(sidebar, BorderLayout.WEST);
    }

    private JButton addNavButton(JPanel sidebar, String text, java.awt.event.ActionListener action) {
        JButton btn = SwingUtils.createStyledButton(text, "Dashboard".equals(text) ? SwingUtils.PRIMARY_DARK : SwingUtils.CARD);
        btn.setFont(SwingUtils.BOLD_FONT);
        btn.setForeground(SwingUtils.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(5));
        return btn;
    }
    
    private void updateNotificationBadges() {
        // Update My Sessions badge
        int pendingBookings = bookingDAO.getPendingHostBookings(currentUser.getId());
        updateButtonText(sessionsNavBtn, "My Sessions", pendingBookings);
        
        // Update Messages badge
        int unreadMessages = messageDAO.getUnreadCount(currentUser.getId());
        updateButtonText(messagesNavBtn, "Messages", unreadMessages);
        
        // Update My Requests badge
        int pendingRequests = exchangeRequestDAO.getPendingIncomingCount(currentUser.getId());
        updateButtonText(requestsNavBtn, "My Requests", pendingRequests);
    }
    
    private void updateButtonText(JButton button, String baseName, int count) {
        if (count > 0) {
            button.setText(baseName + " [" + count + "]");
        } else {
            button.setText(baseName);
        }
    }

    private void setupCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setBackground(SwingUtils.BACKGROUND);
        center.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        welcomeLabel = SwingUtils.createLabel("Welcome back, " + currentUser.getName() + "!", SwingUtils.TITLE_FONT, SwingUtils.TEXT_PRIMARY);
        JButton createSessionBtn = SwingUtils.createStyledButton("+ Create New Session", SwingUtils.PRIMARY_DARK);
        createSessionBtn.addActionListener(e -> {
            new CreateSessionDialog(this, currentUser, this::sessionsChanged).setVisible(true);
        });
        JPanel greeting = new JPanel(new BorderLayout(0, 3));
        greeting.setOpaque(false);
        greeting.add(SwingUtils.createLabel("Dashboard", SwingUtils.TITLE_FONT, SwingUtils.TEXT_PRIMARY), BorderLayout.NORTH);
        welcomeLabel.setFont(SwingUtils.BODY_FONT);
        welcomeLabel.setForeground(SwingUtils.TEXT_SECONDARY);
        greeting.add(welcomeLabel, BorderLayout.CENTER);
        topBar.add(greeting, BorderLayout.CENTER);
        topBar.add(createSessionBtn, BorderLayout.EAST);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);

        sessionsCountLbl = SwingUtils.createLabel("0", SwingUtils.TITLE_FONT, SwingUtils.PRIMARY);
        bookingsCountLbl = SwingUtils.createLabel("0", SwingUtils.TITLE_FONT, SwingUtils.MINT);
        matchesCountLbl = SwingUtils.createLabel("0", SwingUtils.TITLE_FONT, SwingUtils.AMBER);

        statsPanel.add(createStatCard("Sessions posted", sessionsCountLbl));
        statsPanel.add(createStatCard("Total bookings", bookingsCountLbl));
        statsPanel.add(createStatCard("Compatible Peers", matchesCountLbl));

        JPanel northWrapper = new JPanel(new BorderLayout(0, 20));
        northWrapper.setOpaque(false);
        northWrapper.add(topBar, BorderLayout.NORTH);
        northWrapper.add(statsPanel, BorderLayout.CENTER);
        center.add(northWrapper, BorderLayout.NORTH);

        JPanel annPanel = new JPanel(new BorderLayout(0, 10));
        annPanel.setBackground(SwingUtils.CARD);
        annPanel.setBorder(SwingUtils.CARD_BORDER);

        JLabel annTitle = SwingUtils.createLabel("Campus Announcements", SwingUtils.SUBTITLE_FONT, SwingUtils.PRIMARY);
        JPanel annHeading = new JPanel(new BorderLayout());
        annHeading.setOpaque(false);
        annHeading.add(annTitle, BorderLayout.WEST);
        JPanel paging = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        paging.setOpaque(false);
        previous.addActionListener(e -> { announcementIndex--; showAnnouncement(); });
        next.addActionListener(e -> { announcementIndex++; showAnnouncement(); });
        paging.add(previous);
        paging.add(next);
        annHeading.add(paging, BorderLayout.EAST);
        annPanel.add(annHeading, BorderLayout.NORTH);
        JPanel notice = new JPanel(new BorderLayout(0, 5));
        notice.setOpaque(false);
        notice.add(announcementTitle, BorderLayout.NORTH);
        announcementBody.setEditable(false);
        announcementBody.setFont(SwingUtils.BODY_FONT);
        announcementBody.setForeground(SwingUtils.TEXT_SECONDARY);
        announcementBody.setBackground(SwingUtils.CARD);
        announcementBody.setLineWrap(true);
        announcementBody.setWrapStyleWord(true);
        announcementBody.setRows(2);
        JScrollPane announcementScroll = new JScrollPane(announcementBody);
        announcementScroll.setBorder(BorderFactory.createEmptyBorder());
        notice.add(announcementScroll, BorderLayout.CENTER);
        notice.add(announcementMeta, BorderLayout.SOUTH);
        annPanel.add(notice, BorderLayout.CENTER);

        JPanel activity = new JPanel(new BorderLayout(0, 8));
        activity.setBackground(SwingUtils.CARD);
        activity.setBorder(SwingUtils.CARD_BORDER);
        JPanel activityHeading = new JPanel(new BorderLayout());
        activityHeading.setOpaque(false);
        activityHeading.add(activityTitle, BorderLayout.WEST);
        JButton viewBookings = SwingUtils.createStyledButton("View all bookings", SwingUtils.PRIMARY_DARK);
        viewBookings.addActionListener(e -> openSessions().showAllBookings());
        activityHeading.add(viewBookings, BorderLayout.EAST);
        activity.add(activityHeading, BorderLayout.NORTH);
        recentBookings = SwingUtils.createTable("Book a session in Find Matches to see it here.");
        activity.add(new JScrollPane(recentBookings), BorderLayout.CENTER);
        JPanel feed = new JPanel(new BorderLayout(0, 12));
        feed.setOpaque(false);
        feed.add(activity, BorderLayout.CENTER);
        feed.add(annPanel, BorderLayout.SOUTH);
        center.add(feed, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(5, 10));
        card.setBackground(SwingUtils.CARD);
        card.setBorder(SwingUtils.CARD_BORDER);
        card.add(SwingUtils.createLabel(title, SwingUtils.BODY_FONT, SwingUtils.TEXT_SECONDARY), BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshDashboardData() {
        welcomeLabel.setText("Welcome back, " + currentUser.getName() + "!");

        int offeringCount = sessionDAO.getSessionsByUser(currentUser.getId(), "offering").size();
        int requestingCount = sessionDAO.getSessionsByUser(currentUser.getId(), "requesting").size();
        sessionsCountLbl.setText(String.valueOf(offeringCount + requestingCount));

        List<Booking> bookings = bookingDAO.getBookingsByUser(currentUser.getId());
        bookingsCountLbl.setText(String.valueOf(bookings.size()));
        DefaultTableModel activity = SwingUtils.readOnlyModel("Session", "Partner", "Scheduled for", "Status");
        for (Booking b : bookings.subList(0, Math.min(4, bookings.size()))) {
            activity.addRow(new Object[]{b.getSessionTitle(), b.getPartnerName(currentUser.getId()),
                    b.getScheduledAt(), b.getStatus().toUpperCase(java.util.Locale.ROOT)});
        }
        recentBookings.setModel(activity);
        recentBookings.getColumnModel().getColumn(0).setPreferredWidth(210);

        long matchCount = userDAO.getAllUsers().stream()
                .filter(u -> u.getId() != currentUser.getId())
                .filter(u -> SwingUtils.calculateMatchScore(currentUser, u) >= 40)
                .count();
        matchesCountLbl.setText(String.valueOf(matchCount));

        announcements = announcementDAO.getRecentAnnouncements(10);
        announcementIndex = Math.min(announcementIndex, Math.max(0, announcements.size() - 1));
        showAnnouncement();
        
        // Update notification badges
        updateNotificationBadges();
    }

    private void showAnnouncement() {
        boolean empty = announcements.isEmpty();
        announcementTitle.setText(empty ? "No announcements yet" : announcements.get(announcementIndex).getTitle());
        announcementBody.setText(empty ? "" : announcements.get(announcementIndex).getContent());
        announcementBody.setCaretPosition(0);
        if (empty) announcementMeta.setText("");
        else {
            String posted = announcements.get(announcementIndex).getCreatedAt();
            announcementMeta.setText((announcementIndex + 1) + " of " + announcements.size() +
                    (posted == null ? "" : "   |   Posted " + posted.split(" ")[0]));
        }
        previous.setEnabled(announcementIndex > 0);
        next.setEnabled(announcementIndex + 1 < announcements.size());
    }
}
