package com.skillswap.view;

import com.skillswap.dao.RatingDAO;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LeaderboardFrame extends JFrame {
    private final RatingDAO ratingDAO = new RatingDAO();
    private final JTable table = SwingUtils.createTable("No mentors to rank yet.");

    public LeaderboardFrame() {
        setTitle("SkillSwap - Top Mentors Leaderboard");
        setSize(820, 480);
        SwingUtils.centerFrame(this);
        setContentPane(SwingUtils.page());
        add(SwingUtils.heading("Top 10 peer mentors", null), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadLeaderboard();
    }

    void loadLeaderboard() {
        DefaultTableModel model = SwingUtils.readOnlyModel("Rank", "Mentor", "Skills", "Avg rating", "Sessions", "Reviews");

        List<Object[]> rows = ratingDAO.getLeaderboard(10);
        for (Object[] r : rows) {
            model.addRow(r);
        }
        table.setModel(model);
        int[] widths = {54, 148, 240, 98, 80, 80};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

    }
}
