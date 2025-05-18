package supermarket.functionality; // Moved to functionality package

// Item class is in the same 'functionality' package, so direct import not strictly needed
// but can be explicit: import supermarket.functionality.Item;

/**
 * Helper class to represent an item and its quantity in a bill.
 * Updated for new package structure.
 */
public class BillItem {
    private Item item; // Item class from functionality package
    private int quantity;

    public BillItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return item.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %s) x %d @ PKR %.2f = PKR %.2f",
                item.getName(), item.getId(), quantity, item.getPrice(), getSubtotal());
    }
}
