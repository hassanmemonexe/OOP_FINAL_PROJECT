package supermarket.view;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
// UUID is not used for Bill ID

import supermarket.functionality.Item;
import supermarket.functionality.User;
import supermarket.functionality.BillItem;

/**
 * Seller panel for creating bills.
 * Simplified Bill ID Generation: Timestamp-based.
 * Currency updated to PKR.
 */
public class SellerFrame extends JFrame {
    private User sellerUser;
    private JComboBox<Item> itemComboBox;
    private JSpinner quantitySpinner;
    private JButton addItemToBillButton;
    private JTextArea billTextArea;
    private JButton generateBillButton;
    private JButton clearBillButton;
    private JLabel totalAmountLabel;

    private List<Item> availableItemsList;
    private List<BillItem> currentBillItemsList;

    private static final String ITEMS_FILE_PATH = "items.txt";
    private static final String BILLS_FILE_PATH = "bills.txt";
    private static final String BILL_ID_PREFIX_IN_FILE = "Bill ID: ";

    public SellerFrame(User sellerUser) {
        this.sellerUser = sellerUser;
        this.availableItemsList = new ArrayList<>();
        this.currentBillItemsList = new ArrayList<>();

        setTitle("Seller Panel - Welcome " + sellerUser.getUsername());
        setSize(750, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Add Item to Bill"));

        selectionPanel.add(new JLabel("Select Item:"));
        itemComboBox = new JComboBox<>();
        itemComboBox.setPreferredSize(new Dimension(300, 25));
        selectionPanel.add(itemComboBox);

        selectionPanel.add(new JLabel("Quantity:"));
        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
        quantitySpinner = new JSpinner(spinnerModel);
        quantitySpinner.setPreferredSize(new Dimension(70, 25));
        selectionPanel.add(quantitySpinner);

        addItemToBillButton = new JButton("Add to Bill");
        addItemToBillButton.setPreferredSize(new Dimension(120, 25));
        selectionPanel.add(addItemToBillButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(100, 25));
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        selectionPanel.add(Box.createHorizontalStrut(20));
        selectionPanel.add(logoutButton);

        add(selectionPanel, BorderLayout.NORTH);

        billTextArea = new JTextArea(15, 50);
        billTextArea.setEditable(false);
        billTextArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane billScrollPane = new JScrollPane(billTextArea);
        billScrollPane.setBorder(BorderFactory.createTitledBorder("Current Bill / Receipt"));
        add(billScrollPane, BorderLayout.CENTER);

        JPanel bottomControlsPanel = new JPanel(new BorderLayout(10, 5));

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalAmountLabel = new JLabel("Total: PKR 0.00");
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalPanel.add(totalAmountLabel);
        bottomControlsPanel.add(totalPanel, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        generateBillButton = new JButton("Generate & Save Bill");
        generateBillButton.setPreferredSize(new Dimension(180, 30));
        buttonsPanel.add(generateBillButton);

        clearBillButton = new JButton("Clear Bill");
        clearBillButton.setPreferredSize(new Dimension(120, 30));
        buttonsPanel.add(clearBillButton);
        bottomControlsPanel.add(buttonsPanel, BorderLayout.EAST);

        add(bottomControlsPanel, BorderLayout.SOUTH);

        addItemToBillButton.addActionListener(e -> handleAddItemToBill());
        generateBillButton.addActionListener(e -> handleGenerateAndSaveBill());
        clearBillButton.addActionListener(e -> handleClearBill());

        loadAvailableItems();
        updateBillDisplay();
    }

    private void loadAvailableItems() {
        availableItemsList.clear();
        itemComboBox.removeAllItems();
        File itemsFile = new File(ITEMS_FILE_PATH);
        if (!itemsFile.exists()) {
            JOptionPane.showMessageDialog(this, "Items data file not found.", "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(itemsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Item item = Item.fromCSVString(line);
                if (item != null) {
                    availableItemsList.add(item);
                    itemComboBox.addItem(item);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddItemToBill() {
        Item selectedItem = (Item) itemComboBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an item.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int quantity = (Integer) quantitySpinner.getValue();
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be at least 1.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentBillItemsList.add(new BillItem(selectedItem, quantity));
        updateBillDisplay();
        quantitySpinner.setValue(1);
        if (itemComboBox.getItemCount() > 0) {
            itemComboBox.setSelectedIndex(0);
        }
    }

    private void updateBillDisplay() {
        StringBuilder billText = new StringBuilder();
        double currentTotal = 0.0;
        if (currentBillItemsList.isEmpty()) {
            billText.append("<<< Add items to start a new bill >>>");
        } else {
            billText.append(String.format("%-15s %-25s %-13s %-5s %-15s\n", "Item ID", "Item Name", "Price(PKR)", "Qty", "Subtotal(PKR)")); // Adjusted for potentially longer timestamp ID
            billText.append("---------------------------------------------------------------------------------\n");
            for (BillItem billItem : currentBillItemsList) {
                billText.append(String.format("%-15s %-25.25s PKR %-10.2f %-5d PKR %-12.2f\n",
                        billItem.getItem().getId(),
                        billItem.getItem().getName(),
                        billItem.getItem().getPrice(),
                        billItem.getQuantity(),
                        billItem.getSubtotal()));
                currentTotal += billItem.getSubtotal();
            }
            billText.append("---------------------------------------------------------------------------------\n");
        }
        billTextArea.setText(billText.toString());
        totalAmountLabel.setText(String.format("Total: PKR %.2f", currentTotal));
    }

    /**
     * Generates a unique bill ID using timestamp and a random number.
     * @return A unique bill ID as a String.
     */
    private String generateNewBillId() {
        return System.currentTimeMillis() + "-" + (int)(Math.random()*1000);
    }

    private void handleGenerateAndSaveBill() {
        if (currentBillItemsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot generate an empty bill.", "Billing Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = generateNewBillId(); // Use simplified timestamp-based ID
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String billDate = dateFormat.format(new Date());
        double finalTotalAmount = currentBillItemsList.stream().mapToDouble(BillItem::getSubtotal).sum();

        StringBuilder receiptBuilder = new StringBuilder();
        receiptBuilder.append("==========================================================\n");
        receiptBuilder.append("                     SUPERMARKET RECEIPT                  \n");
        receiptBuilder.append("==========================================================\n");
        receiptBuilder.append(BILL_ID_PREFIX_IN_FILE).append(billId).append("\n");
        receiptBuilder.append("Seller:  ").append(sellerUser.getUsername()).append("\n");
        receiptBuilder.append("Date:    ").append(billDate).append("\n");
        receiptBuilder.append("----------------------------------------------------------\n");
        receiptBuilder.append(String.format("%-15s %-25s %-5s %-13s %-13s\n", "Item ID", "Item Name", "Qty", "Price (PKR)", "Amount (PKR)"));
        receiptBuilder.append("----------------------------------------------------------\n");

        for (BillItem bi : currentBillItemsList) {
            receiptBuilder.append(String.format("%-15s %-25.25s %-5d PKR %-10.2f PKR %-10.2f\n",
                    bi.getItem().getId(),
                    bi.getItem().getName(),
                    bi.getQuantity(),
                    bi.getItem().getPrice(),
                    bi.getSubtotal()));
        }
        receiptBuilder.append("----------------------------------------------------------\n");
        receiptBuilder.append(String.format("%48s PKR %.2f\n", "TOTAL AMOUNT: ", finalTotalAmount));
        receiptBuilder.append("==========================================================\n");
        receiptBuilder.append("                 Thank you for your purchase!             \n");
        receiptBuilder.append("==========================================================\n\n");

        String finalReceipt = receiptBuilder.toString();
        billTextArea.setText(finalReceipt);

        try (PrintWriter writer = new PrintWriter(new FileWriter(BILLS_FILE_PATH, true))) {
            writer.print(finalReceipt);
            JOptionPane.showMessageDialog(this, "Bill (ID: " + billId + ") generated and saved successfully!", "Bill Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving bill to file: " + e.getMessage(), "File Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleClearBill() {
        currentBillItemsList.clear();
        updateBillDisplay();
        quantitySpinner.setValue(1);
        if (itemComboBox.getItemCount() > 0) {
            itemComboBox.setSelectedIndex(0);
        }
        JOptionPane.showMessageDialog(this, "Bill cleared.", "Bill Cleared", JOptionPane.INFORMATION_MESSAGE);
    }
}
