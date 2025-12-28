package ee.taltech.icd0011.classes;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLine {

    private Long id;
    private String itemName;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    @Positive
    private Integer price;
}
