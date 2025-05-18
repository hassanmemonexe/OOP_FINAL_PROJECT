package supermarket.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
// No need to import ListSelectionEvent and ListSelectionListener explicitly if using lambda or anonymous class
import java.awt.*;
// No need to import ActionEvent and ActionListener explicitly if using lambda or anonymous class
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator; // For safe removal from list while iterating if needed

import supermarket.functionality.Item;
import supermarket.functionality.User;
// LoginFrame is in the same 'view' package

/**
 * Admin panel for managing items and users.
 * Simplified User Management: Admin can add/delete sellers. Passwords not displayed.
 * Simplified Item ID Generation: Timestamp-based.
 * Currency is PKR.
 */
public class AdminFrame extends JFrame {
    private User adminUser;

    // Item Management Components
    private JTextField itemNameField, itemPriceField;
    private JButton addItemButton;
    private JButton deleteItemButton;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private List<Item> itemList;

    // User Management Components (Simplified)
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private List<User> userListForManagement;
    private JTextField manageSellerUsernameField; // Renamed for clarity
    private JPasswordField manageSellerPasswordField; // Renamed for clarity
    private JButton manageSellerAddButton; // Renamed
    private JButton manageSellerDeleteButton; // Renamed

    private static final String ITEMS_FILE_PATH = "items.txt";
    private static final String USERS_FILE_PATH = "users.txt";

    public AdminFrame(User loggedInAdminUser) {
        this.adminUser = loggedInAdminUser;
        this.itemList = new ArrayList<>();
        this.userListForManagement = new ArrayList<>();

        setTitle("Admin Panel - Welcome " + adminUser.getUsername());
        setSize(900, 650); // Adjusted size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Items", createManageItemsPanel());
        tabbedPane.addTab("Manage Sellers", createManageSellersPanel()); // Renamed tab

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        bottomPanel.add(logoutButton);

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadItemsFromFile();
        loadUsersForManagement();
    }

    private JPanel createManageItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputFormPanel = new JPanel(new GridBagLayout());
        inputFormPanel.setBorder(BorderFactory.createTitledBorder("Add New Item"));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);
        gbcForm.fill = GridBagConstraints.HORIZONTAL;

        gbcForm.gridx = 0; gbcForm.gridy = 0; inputFormPanel.add(new JLabel("Item Name:"), gbcForm);
        itemNameField = new JTextField(20);
        gbcForm.gridx = 1; gbcForm.gridy = 0; inputFormPanel.add(itemNameField, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 1; inputFormPanel.add(new JLabel("Item Price (PKR):"), gbcForm);
        itemPriceField = new JTextField(10);
        gbcForm.gridx = 1; gbcForm.gridy = 1; inputFormPanel.add(itemPriceField, gbcForm);

        addItemButton = new JButton("Add Item");
        gbcForm.gridx = 0; gbcForm.gridy = 2; gbcForm.gridwidth = 2;
        gbcForm.anchor = GridBagConstraints.CENTER;
        inputFormPanel.add(addItemButton, gbcForm);

        panel.add(inputFormPanel, BorderLayout.NORTH);

        String[] columnNames = {"Item ID", "Name", "Price (PKR)"};
        itemsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        itemsTable = new JTable(itemsTableModel);
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel itemActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteItemButton = new JButton("Delete Selected Item");
        itemActionsPanel.add(deleteItemButton);
        panel.add(itemActionsPanel, BorderLayout.SOUTH);

        addItemButton.addActionListener(e -> handleAddItem());
        deleteItemButton.addActionListener(e -> handleDeleteItem());
        return panel;
    }

    /**
     * Creates the panel for simplified seller management (Add/Delete Seller).
     */
    private JPanel createManageSellersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Seller Table (Top part) ---
        String[] userColumnNames = {"Username", "Role"}; // Password column removed
        usersTableModel = new DefaultTableModel(userColumnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        usersTable = new JTable(usersTableModel);
        JScrollPane userTableScrollPane = new JScrollPane(usersTable);
        panel.add(userTableScrollPane, BorderLayout.CENTER);

        // --- Seller Input and Action Form (Bottom part) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Seller"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Seller Username:"), gbc);
        manageSellerUsernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1; formPanel.add(manageSellerUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Seller Password:"), gbc);
        manageSellerPasswordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; formPanel.add(manageSellerPasswordField, gbc);

        // Role is fixed to "seller", so no ComboBox needed here for adding.

        // Buttons Panel
        JPanel buttonsSubPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        manageSellerAddButton = new JButton("Add Seller");
        manageSellerDeleteButton = new JButton("Delete Selected Seller"); // Changed text

        buttonsSubPanel.add(manageSellerAddButton);
        buttonsSubPanel.add(manageSellerDeleteButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; // Span across 2 columns
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonsSubPanel, gbc);

        panel.add(formPanel, BorderLayout.SOUTH);

        // Action Listeners for buttons
        manageSellerAddButton.addActionListener(e -> handleAddSeller());
        manageSellerDeleteButton.addActionListener(e -> handleDeleteSeller());

        return panel;
    }

    // --- Item Management Logic ---
    private void loadItemsFromFile() {
        this.itemList.clear();
        itemsTableModel.setRowCount(0);
        File itemsFile = new File(ITEMS_FILE_PATH);
        if (!itemsFile.exists()) {
            try {
                if (itemsFile.createNewFile()) { System.out.println(ITEMS_FILE_PATH + " created."); }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error creating items file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(itemsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Item item = Item.fromCSVString(line);
                if (item != null) {
                    this.itemList.add(item);
                    itemsTableModel.addRow(new Object[]{item.getId(), item.getName(), String.format("%.2f", item.getPrice())});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean saveAllItemsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ITEMS_FILE_PATH, false))) {
            for (Item item : itemList) {
                writer.println(item.toCSVString());
            }
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving items to file: " + e.getMessage(), "File Save Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Generates a unique item ID using timestamp and a random number.
     * @return A unique item ID as a String.
     */
    private String generateNewItemId() {
        // Timestamp ensures high likelihood of uniqueness, random part reduces collision for rapid adds.
        return System.currentTimeMillis() + "-" + (int)(Math.random()*1000);
    }

    private void handleAddItem() {
        String name = itemNameField.getText().trim();
        String priceStr = itemPriceField.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name and Price cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = generateNewItemId(); // Use simplified ID generation
        Item newItem = new Item(id, name, price);

        for (Item existingItem : this.itemList) {
            if (existingItem.getName().equalsIgnoreCase(name)) { // Check for duplicate name
                JOptionPane.showMessageDialog(this, "Item with this Name already exists.", "Duplicate Item Name", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        this.itemList.add(newItem);

        if (saveAllItemsToFile()) {
            itemsTableModel.addRow(new Object[]{newItem.getId(), newItem.getName(), String.format("%.2f", newItem.getPrice())});
            JOptionPane.showMessageDialog(this, "Item '" + name + "' (ID: " + id + ") added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            itemNameField.setText("");
            itemPriceField.setText("");
        } else {
            this.itemList.remove(newItem);
            JOptionPane.showMessageDialog(this, "Failed to save item. Item not added.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemIdToDelete = (String) itemsTableModel.getValueAt(selectedRow, 0);
        String itemName = (String) itemsTableModel.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete item '" + itemName + "' (ID: " + itemIdToDelete + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            Item itemToRemove = null;
            for(Item item : itemList) {
                if(item.getId().equals(itemIdToDelete)) {
                    itemToRemove = item;
                    break;
                }
            }

            if (itemToRemove != null) {
                itemList.remove(itemToRemove);
                if (saveAllItemsToFile()) {
                    JOptionPane.showMessageDialog(this, "Item '" + itemName + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    itemsTableModel.removeRow(selectedRow);
                } else {
                    itemList.add(itemToRemove);
                    JOptionPane.showMessageDialog(this, "Failed to save changes after deletion. Item not deleted from file.", "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Could not find the selected item. Please refresh.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Simplified User (Seller) Management Logic ---
    private void loadUsersForManagement() {
        userListForManagement.clear();
        usersTableModel.setRowCount(0);
        File usersFile = new File(USERS_FILE_PATH);
        if (!usersFile.exists()) {
            JOptionPane.showMessageDialog(this, "Users file ('users.txt') not found.", "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromCSVString(line);
                if (user != null) {
                    userListForManagement.add(user);
                    // Only display username and role, not password
                    usersTableModel.addRow(new Object[]{user.getUsername(), user.getRole()});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean saveAllUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE_PATH, false))) {
            for (User user : userListForManagement) {
                writer.println(user.toCSVString());
            }
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving users to file: " + e.getMessage(), "File Save Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void handleAddSeller() { // Renamed from handleAddUser
        String username = manageSellerUsernameField.getText().trim();
        String password = new String(manageSellerPasswordField.getPassword()).trim();
        String role = "seller"; // Role is fixed to "seller"

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seller Username and Password cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Check if username is already taken by any user
        for (User existingUser : userListForManagement) {
            if (existingUser.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username '" + username + "' is already taken.", "Username Exists", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        User newSeller = new User(username, password, role);
        userListForManagement.add(newSeller);

        if (saveAllUsersToFile()) {
            usersTableModel.addRow(new Object[]{newSeller.getUsername(), newSeller.getRole()}); // Add to table without password
            JOptionPane.showMessageDialog(this, "Seller '" + username + "' added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            manageSellerUsernameField.setText(""); // Clear form fields
            manageSellerPasswordField.setText("");
        } else {
            userListForManagement.remove(newSeller); // Rollback if save failed
            JOptionPane.showMessageDialog(this, "Failed to save new seller.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteSeller() { // Renamed from handleDeleteUser
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a seller from the table to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String usernameToDelete = (String) usersTableModel.getValueAt(selectedRow, 0);
        String roleOfUserToDelete = (String) usersTableModel.getValueAt(selectedRow, 1);

        // Prevent deleting the currently logged-in admin (though this panel focuses on sellers)
        if (adminUser.getUsername().equals(usernameToDelete)) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account from this interface.", "Deletion Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prevent deleting other admin accounts from this simplified interface
        if ("admin".equalsIgnoreCase(roleOfUserToDelete)) {
            JOptionPane.showMessageDialog(this, "Admin accounts cannot be deleted from this interface.", "Deletion Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete seller '" + usernameToDelete + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            boolean removed = false;
            // Iterate using Iterator to safely remove from userListForManagement
            Iterator<User> iterator = userListForManagement.iterator();
            while(iterator.hasNext()){
                User user = iterator.next();
                if(user.getUsername().equals(usernameToDelete) && "seller".equalsIgnoreCase(user.getRole())){
                    iterator.remove();
                    removed = true;
                    break;
                }
            }

            if (removed) {
                if (saveAllUsersToFile()) {
                    usersTableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Seller '" + usernameToDelete + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    manageSellerUsernameField.setText(""); // Clear form
                    manageSellerPasswordField.setText("");
                } else {
                    // If save fails, we need to add the user back to userListForManagement to keep consistency
                    // For simplicity in this version, we might just reload all users
                    loadUsersForManagement();
                    JOptionPane.showMessageDialog(this, "Failed to save changes after seller deletion. List reloaded.", "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seller not found for deletion or user is not a seller. Please refresh.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // Removed handleUpdateUser and isUsernameTaken (as simplified user mgmt doesn't need complex checks for update)
    // A simpler isUsernameTaken was incorporated into handleAddSeller
}
