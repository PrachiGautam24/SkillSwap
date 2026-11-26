import com.skillswap.dao.BookingDAO;
import com.skillswap.dao.DBConnection;
import com.skillswap.dao.SessionDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.model.Booking;
import com.skillswap.model.User;
import com.skillswap.view.MySessionsFrame;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BookingFlowTest {
    private static void check(boolean passed, String message) {
        if (!passed) throw new AssertionError(message);
    }

    private static void checkBookingTab(User user, int expectedRows, String partner, String role) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MySessionsFrame frame = new MySessionsFrame(user);
            try {
                BorderLayout layout = (BorderLayout) frame.getContentPane().getLayout();
                JTabbedPane tabs = (JTabbedPane) layout.getLayoutComponent(BorderLayout.CENTER);
                JScrollPane currentTab = (JScrollPane) tabs.getSelectedComponent();
                JTable table = (JTable) currentTab.getViewport().getView();
                check(table.getRowCount() == expectedRows, "Default tab matches dashboard booking count");
                check("Linear Algebra Study Group".equals(table.getValueAt(0, 0)), "Booked session appears first");
                check(partner.equals(table.getValueAt(0, 1)) && role.equals(table.getValueAt(0, 2)), "Partner and role");
                check("PENDING".equals(table.getValueAt(0, 5)), "Pending bookings visible before confirmation");
            } finally {
                frame.dispose();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("skillswap.db.url", "jdbc:h2:mem:booking_flow;MODE=MySQL;DB_CLOSE_DELAY=-1");
        DBConnection.initializeTables();
        User alice = new UserDAO().login("alice@uni.edu", "pass123");
        check(alice != null, "Demo login");
        SessionDAO sessions = new SessionDAO();
        BookingDAO bookings = new BookingDAO();
        int host = 4, learner = 1, session = 4;
        int before = bookings.getBookingsByUser(learner).size();
        check(before == 2, "Existing bookings must include both roles");
        check(sessions.getBookableSessions(host, learner).size() == 1, "Offering available");
        check(!bookings.createBooking(session, host), "Host cannot book own session");
        check(!bookings.createBooking(3, learner), "Help requests are not bookable teaching sessions");
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("UPDATE sessions SET status = 'closed' WHERE id = 4");
            check(sessions.getBookableSessions(host, learner).isEmpty(), "Closed session hidden");
            check(!bookings.createBooking(session, learner), "Stale picker cannot book closed session");
            s.executeUpdate("UPDATE sessions SET status = 'open' WHERE id = 4");
        }

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            java.util.concurrent.Callable<Boolean> book = () -> { start.await(); return bookings.createBooking(session, learner); };
            Future<Boolean> first = pool.submit(book);
            Future<Boolean> second = pool.submit(book);
            start.countDown();
            check(first.get() != second.get(), "Exactly one concurrent request succeeds");
        } finally {
            pool.shutdownNow();
        }

        Booking b = bookings.getBookingsByUser(learner).get(0);
        check(bookings.getBookingsByUser(learner).size() == before + 1, "Learner count");
        check(b.getSessionId() == session && "pending".equals(b.getStatus()), "New request appears immediately");
        check("David Kim".equals(b.getPartnerName(learner)), "Learner sees host");
        check("Alice Johnson".equals(b.getPartnerName(host)), "Host sees learner");
        check(bookings.getBookingsByUser(host).get(0).getId() == b.getId(), "Both participants see same booking");
        check(bookings.getBookingsByUser(5).isEmpty(), "Other users cannot see booking");
        if (!GraphicsEnvironment.isHeadless()) {
            checkBookingTab(alice, before + 1, "David Kim", "Learner");
            checkBookingTab(new UserDAO().login("david@uni.edu", "pass123"), 1, "Alice Johnson", "Host");
        }
        check("2026-11-23 10:00".equals(b.getScheduledAt()) && !b.getScheduledAt().equals(b.getCreatedAt()), "Correct dates");
        check(sessions.getBookableSessions(host, learner).isEmpty(), "Existing request hidden from picker");
        check(!bookings.updateBookingStatus(b.getId(), "completed"), "Cannot skip confirmation");
        check(bookings.updateBookingStatus(b.getId(), "cancelled"), "Cancel pending request");
        check(bookings.createBooking(session, learner), "Can rebook after cancellation");
        int newId = bookings.getBookingsByUser(learner).get(0).getId();
        check(newId != b.getId(), "Cancellation history preserved");
        check(bookings.updateBookingStatus(newId, "confirmed"), "Confirm booking");
        check(bookings.updateBookingStatus(newId, "completed"), "Complete confirmed booking");
        check(!bookings.updateBookingStatus(newId, "cancelled"), "Completed booking is final");
        check("completed".equals(bookings.getBookingsByUser(learner).get(0).getStatus()), "Learner sees completion");
        check(!bookings.createBooking(session, learner), "Completed single session cannot be booked twice");
        System.out.println("PASS: existing bookings, participant mapping, schedule, availability, concurrency and lifecycle");
    }
}
