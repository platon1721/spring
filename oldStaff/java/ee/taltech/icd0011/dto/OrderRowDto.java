package ee.taltech.icd0011.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class OrderRowDto {

    private String itemName;

    @NotNull(message = "Quantity must be specified")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Price must be specified")
    @Min(value = 1, message = "Price must be greater than 0")
    private Integer price;

    public OrderRowDto() {
    }

    public OrderRowDto(String itemName, Integer quantity, Integer price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

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