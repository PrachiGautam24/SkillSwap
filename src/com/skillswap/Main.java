package com.skillswap;

import com.skillswap.dao.DBConnection;
import com.skillswap.view.LoginFrame;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            SwingUtils.installTheme();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Keep the embedded database open between screen queries until the app exits.
            Connection database = DBConnection.getConnection();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { database.close(); } catch (SQLException e) { e.printStackTrace(); }
            }));
            DBConnection.initializeTables();
        } catch (SQLException | IllegalStateException | LinkageError e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not open the SkillSwap database. " +
                    "Check the files in lib and access to your home folder.", "SkillSwap", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
