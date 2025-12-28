package ee.taltech.icd0011.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class OrderDto {

    private Long id;

    @NotNull
    @Size(min = 2)
    private String orderNumber;

    @Valid
    private List<OrderRowDto> orderRows = new ArrayList<>();

    public OrderDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<OrderRowDto> getOrderRows() {
        return orderRows;
    }

    public void setOrderRows(List<OrderRowDto> orderRows) {
        this.orderRows = orderRows;
    }
}