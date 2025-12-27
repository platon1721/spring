package ee.taltech.icd0011.dao;

import ee.taltech.icd0011.classes.OrderLine;

import java.sql.ResultSet;
import java.sql.SQLException;

final class OrderLineMapper {

    private static final String FIELD_QUANTITY = "quantity";
    private static final String FIELD_PRICE = "price";

    private OrderLineMapper() {
    }

    // Package-private for use by OrderDaoImpl
    static OrderLine createFromResultSet(ResultSet rs) throws SQLException {
        OrderLine line = new OrderLine();
        line.setItemName(rs.getString("item_name"));

        int quantity = rs.getInt(FIELD_QUANTITY);
        int price = rs.getInt(FIELD_PRICE);

        line.setQuantity(quantity);
        line.setPrice(price);

        // If setters didn't set them (because they were <= 0), use reflection
        setFieldIfNull(line, FIELD_QUANTITY, quantity);
        setFieldIfNull(line, FIELD_PRICE, price);

        return line;
    }

    private static void setFieldIfNull(OrderLine line, String fieldName, int value) {
        try {
            if (fieldName.equals(FIELD_QUANTITY) && line.getQuantity() == null) {
                java.lang.reflect.Field field = OrderLine.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(line, value);
            } else if (fieldName.equals(FIELD_PRICE) && line.getPrice() == null) {
                java.lang.reflect.Field field = OrderLine.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(line, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed to set field " + fieldName + ": " + e.getMessage());
        }
    }
}