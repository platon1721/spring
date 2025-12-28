package ee.taltech.icd0011.dao;

import ee.taltech.icd0011.classes.Order;

import java.util.List;

public interface OrderDao {
    Order save(Order order);
    Order findById(Long id);
    List<Order> findAll();
    List<Order> findAllWithLines();
    boolean deleteById(Long id);
    List<Order> saveBatch(List<Order> orders);
}
