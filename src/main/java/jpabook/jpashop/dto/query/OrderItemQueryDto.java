package jpabook.jpashop.dto.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.OrderItem;
import lombok.Getter;

@Getter
public class OrderItemQueryDto {

    @JsonIgnore
    private Long orderId;
    private String itemName;
    private int orderPrice;
    private int count;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
