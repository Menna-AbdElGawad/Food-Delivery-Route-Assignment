package models;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public abstract class MenuItem {

    private final int restaurantId;
    private final int id;
    private String name;
    private BigDecimal price;
    private String category;
    private int preparationTime;
    private boolean available = true;

    public MenuItem(int restaurantId, int id, String name, BigDecimal price,
                            String category, int preparationTime) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }

        if (preparationTime <= 0) {
            throw new IllegalArgumentException("Preparation time must be > 0");
        }

        this.restaurantId = restaurantId;
        this.id = id;
        this.name = name.trim();
         this.price = requirePositivePrice(price);
        this.category = category.trim();
        this.preparationTime = preparationTime;
    }


    public abstract BigDecimal pricing(BigDecimal kilograms);


    public boolean isAvailable() {
        return available;
    }

    public void setPrice(BigDecimal price) {
        this.price = requirePositivePrice(price);
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    private BigDecimal requirePositivePrice(BigDecimal price) {
        if(price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }

        return price;
    }

    protected static BigDecimal requirePositiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return quantity;
    }

    protected static BigDecimal requireWholeQuantity(BigDecimal quantity) {
        requirePositiveQuantity(quantity);
        if (quantity.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("This item is sold by count, quantity must be a whole number");
        }
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return restaurantId == menuItem.restaurantId && id == menuItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, id);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "restaurantId=" + restaurantId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", preparationTime=" + preparationTime +
                ", available=" + available +
                '}';
    }
}