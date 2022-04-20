package jpabook.jpashop.dto.response;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class GetOrdersResponseDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemDto> orderItems; // GetOrderResponseDto에서 이것만 추가하는 DTO

    public GetOrdersResponseDto(Order order) {
        orderId = order.getId();
        name = order.getMember().getName(); // Lazy를 초기화하는 로직 (영속성 컨텍스트에서 우선 찾음 -> 하지만 없으면 db쿼리날라가게됨)
        orderDate = order.getOrderDate();
        orderStatus = order.getStatus();
        address = order.getDelivery().getAddress(); // Lazy를 초기화
        orderItems = order.getOrderItems().stream()
                .map(orderItem -> new OrderItemDto(orderItem))
                .collect(Collectors.toList()); // 바로 위 코드 없으면 초기화 안되서 null 뜸
    }
}
