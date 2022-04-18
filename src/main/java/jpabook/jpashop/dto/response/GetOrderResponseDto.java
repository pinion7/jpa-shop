package jpabook.jpashop.dto.response;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetOrderResponseDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public GetOrderResponseDto(Order order) { // dto는 엔티티를 참조해도 괜찮음
        orderId = order.getId();
        name = order.getMember().getName(); // Lazy를 초기화하는 로직 (영속성 컨텍스트에서 우선 찾음 -> 하지만 없으면 db쿼리날라가게됨)
        orderDate = order.getOrderDate();
        orderStatus = order.getStatus();
        address = order.getDelivery().getAddress(); // Lazy를 초기화하는 로직
    }
}
