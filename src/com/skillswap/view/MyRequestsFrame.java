package com.skillswap.view;

import com.skillswap.dao.ExchangeRequestDAO;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyRequestsFrame extends JFrame {
    private final User currentUser;
    private final JTabbedPane tabs = new JTabbedPane();
    private JTable incomingTable, sentTable;
    private List<ExchangeRequest> incomingList, sentList;
    private final ExchangeRequestDAO requestDAO = new ExchangeRequestDAO();

    public MyRequestsFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("SkillSwap - My Exchange Requests");
        setSize(880, 500);
        SwingUtils.centerFrame(this);

        setContentPane(SwingUtils.page());
        add(SwingUtils.heading("Exchange requests", null), BorderLayout.NORTH);
        tabs.setFont(SwingUtils.BOLD_FONT);

        JPanel incomingPanel = new JPanel(new BorderLayout(10, 10));
        incomingPanel.setBackground(SwingUtils.BACKGROUND);
        incomingTable = SwingUtils.createTable("No incoming exchange requests.");
        incomingPanel.add(new JScrollPane(incomingTable), BorderLayout.CENTER);

        JPanel inBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        inBtns.setBackground(SwingUtils.CARD);
        JButton acceptBtn = SwingUtils.createStyledButton("Accept", SwingUtils.SUCCESS);
        JButton rejectBtn = SwingUtils.createStyledButton("Reject", SwingUtils.DANGER);

        acceptBtn.addActionListener(e -> updateSelectedStatus("accepted"));
        rejectBtn.addActionListener(e -> updateSelectedStatus("rejected"));
        acceptBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        incomingTable.getSelectionModel().addListSelectionListener(e -> {
            int selected = incomingTable.getSelectedRow();
            boolean pending = selected >= 0 && "pending".equals(incomingList.get(selected).getStatus());
            acceptBtn.setEnabled(pending);
            rejectBtn.setEnabled(pending);
        });
        inBtns.add(acceptBtn);
        inBtns.add(rejectBtn);
        incomingPanel.add(inBtns, BorderLayout.SOUTH);

        JPanel sentPanel = new JPanel(new BorderLayout());
        sentPanel.setBackground(SwingUtils.BACKGROUND);
        sentTable = SwingUtils.createTable("Requests you send from Find Matches will appear here.");
        sentPanel.add(new JScrollPane(sentTable), BorderLayout.CENTER);

        tabs.addTab("Incoming Requests", incomingPanel);
        tabs.addTab("Sent Requests", sentPanel);
        add(tabs);

        loadTables();
    }

    void loadTables() {
        incomingList = requestDAO.getIncomingRequests(currentUser.getId());
        DefaultTableModel inModel = new DefaultTableModel(
                new String[]{"From Student", "They Offer", "They Request", "Message", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ExchangeRequest r : incomingList) {
            inModel.addRow(new Object[]{r.getFromUserName(), r.getSkillOffering(), r.getSkillRequesting(), r.getMessage(), r.getStatus().toUpperCase()});
        }
        incomingTable.setModel(inModel);

        sentList = requestDAO.getSentRequests(currentUser.getId());
        DefaultTableModel sentModel = new DefaultTableModel(
                new String[]{"To Student", "You Offered", "You Requested", "Message", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ExchangeRequest r : sentList) {
            sentModel.addRow(new Object[]{r.getToUserName(), r.getSkillOffering(), r.getSkillRequesting(), r.getMessage(), r.getStatus().toUpperCase()});
        }
        sentTable.setModel(sentModel);
        tabs.setTitleAt(0, "Incoming (" + incomingList.size() + ")");
        tabs.setTitleAt(1, "Sent (" + sentList.size() + ")");
    }

    private void updateSelectedStatus(String status) {
        int row = incomingTable.getSelectedRow();
        if (row < 0) {
            SwingUtils.showError(this, "Select an incoming request first.");
            return;
        }
        ExchangeRequest req = incomingList.get(row);
        if (requestDAO.updateRequestStatus(req.getId(), status)) {
            SwingUtils.showSuccess(this, "Request marked as " + status + ".");
            loadTables();
        } else {
            loadTables();
            SwingUtils.showError(this, "Could not change this request. Check its current status.");
        }
    }
}
