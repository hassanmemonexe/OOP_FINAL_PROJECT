package supermarket; // Stays in the root supermarket package

import javax.swing.*;
import supermarket.view.LoginFrame; // Import LoginFrame from the view package

/**
 * Main application class to launch the Supermarket Management System.
 * Updated for new package structure.
 */
public class SupermarketApp {

    public static void main(String[] args) {
        // Set a more modern Look and Feel if available (Nimbus is a good choice)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            // If Nimbus is not available, it will fall back to the default L&F.
            System.err.println("Nimbus L&F not found, using default: " + e.getMessage());
        }

        // Run the GUI on the Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}
