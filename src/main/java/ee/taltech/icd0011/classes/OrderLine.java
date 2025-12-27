package ee.taltech.icd0011.classes;

public class OrderLine {

    private String itemName;
    private Integer quantity;
    private Integer price;

    public OrderLine(String itemName, Integer quantity, Integer price) {
        this.itemName = itemName;
        if (quantity > 0) {
            this.quantity = quantity;
        }
        if(price > 0) {
            this.price = price;
        }
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
        if (quantity > 0) {
            this.quantity = quantity;
        }
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        if (price > 0) {
            this.price = price;
        }
    }

}
