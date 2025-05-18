package supermarket.functionality; // Moved to functionality package

import java.util.Objects;

/**
 * Represents an item in the supermarket.
 * Updated for new package structure.
 */
public class Item {
    private String id;
    private String name;
    private double price;

    public Item(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String toCSVString() {
        return id + "," + name + "," + price;
    }

    public static Item fromCSVString(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvString.split(",");
        if (parts.length == 3) {
            try {
                String id = parts[0].trim();
                String name = parts[1].trim();
                double price = Double.parseDouble(parts[2].trim());
                return new Item(id, name, price);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing item price from CSV: " + csvString + " - " + e.getMessage());
                return null;
            }
        }
        System.err.println("Invalid CSV string for Item (expected 3 parts): " + csvString);
        return null;
    }

    @Override
    public String toString() {
        return name + " - PKR " + String.format("%.2f", price) + " (ID: " + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
