package ee.taltech.icd0011.classes;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderLine {

    private String itemName;
    
    @NotNull
    @Min(1)
    private Integer quantity;
    
    @NotNull
    @Min(1)
    private Integer price;

    public OrderLine(String itemName, Integer quantity, Integer price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderLine() { }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
