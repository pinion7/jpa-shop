package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.dto.query.GetOrderFlatQueryDto;
import jpabook.jpashop.dto.query.GetOrdersQueryDto;
import jpabook.jpashop.dto.query.OrderItemQueryDto;
import jpabook.jpashop.dto.response.GetOrderResponseDto;
import jpabook.jpashop.dto.response.GetOrdersResponseDto;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.query.OrderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


// OrderSimpleApiController랑 거의 비슷한데, 차이점은 일대다 컬렉션도 조회하느냐? 그럴때의 문제점과 성능최적화를 보여주기 위함임
// (참고로 OrderSimpleApiController에서는 Order 엔티티의 일대다 필드라고 할 수 있는 orderItems는 전혀 조회하지 않고 있음)
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    // 1. 엔티티 노출 방식 (비추!)
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // 강제 초기화 (영속성 컨텍스트가 비어있으면 db로 부터 데이터를 가져오게끔 하는 것)
            order.getDelivery().getAddress(); // 강제 초기화

            List<OrderItem> orderItems = order.getOrderItems(); // 강제 초기화
            orderItems.stream().forEach(o -> o.getItem().getName()); // 강제 초기화
        }
        return all;
    }

    // 2. Dto 방식 (GetOrdersResponseDto의 내부 필드인 OrderItem 조차도 Dto를 적용) -> 단, 문제는 쿼리가 너무많이나가~~~~
    @GetMapping("/api/v2/orders")
    public List<GetOrdersResponseDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(order -> new GetOrdersResponseDto(order))
                .collect(Collectors.toList());
    }

    // 3. 페치 조인 활용으로 일대다 컬렉션 조회! -> 페이징 불가...!
    @GetMapping("/api/v3/orders")
    public List<GetOrdersResponseDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(order -> new GetOrdersResponseDto(order))
                .collect(Collectors.toList());
    }

    // 3.1 페치 조인 활용으로 일대다 컬렉션 조회! -> 페이징 및 성능 최적화 가능!
    @GetMapping("/api/v3.1/orders")
    public List<GetOrdersResponseDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        return orders.stream()
                .map(order -> new GetOrdersResponseDto(order))
                .collect(Collectors.toList());
    }

    // 4. JPA에서 DTO 직접 적용해서 조회하는 방식도 해보자! (1 + N 문제 발생하는 케이스!) -> 즉, 단건 조회에서만 추천
    @GetMapping("/api/v4/orders")
    public List<GetOrdersQueryDto> ordersV4() {
        List<GetOrdersQueryDto> orders = orderQueryRepository.findOrdersQueryDtos();

        // 위에서 orderItems 필드는 빈값으로 돌아오기 때문에, 아래처럼 orders를 루프를 돌리면서 매번 쿼리를 날려 빈 필드를 채워줘야함.
        // Repository에 모든 로직을 두는 방법도 있지만 이건 개인적으로 서비스로직에 가깝다 생각되어 여기에 위치시킴
        // 근데 이건 한바퀴 돌때마다 쿼리를 날리기에 1 + N 문제에 해당됨
        orders.forEach(o -> {
            List<OrderItemQueryDto> orderItems = orderQueryRepository.findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return orders;
    }

    // 5. JPA에서 DTO 직접 적용해서 조회 및 성능최적화! (4번의 1 + N 문제 해결하는 케이스!)
    @GetMapping("/api/v5/orders")
    public List<GetOrdersQueryDto> ordersV5() {
        List<GetOrdersQueryDto> orders = orderQueryRepository.findOrdersQueryDtos();

        // 일단 orderIds만 뽑고
        List<Long> orderIds = orders.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());

        // orderIds를 바탕으로 한방의 쿼리로 orderItems를 다 가져옴
        List<OrderItemQueryDto> orderItems = orderQueryRepository.findOrderItemsByOptimization(orderIds);

        // 그걸 Map 메모리에 key-value 형태로 저장해둠
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));

        // orders를 루프돌면서 메모리에 저장해둔 결과를 세팅해줌 (루프돌때마다 쿼리를 날려서 orderItems를 세팅하는 게 아닌, 메모리에 저장해둔 결과를 반영하여 세팅)
        // 즉 1 + N의 쿼릭 나가지 않고 총 2번의 쿼리가 나감.
        orders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return orders;
    }

    // 6. JPA에서 DTO 직접 적용해서 조회 및 성능최적화! (5번에서 2번의 쿼리로 최적화 한걸 -> 6번에선 1번의 쿼리로 최적화!)
    // 잘못하면 5번보다 성능도 안좋고... 페이징도 불가능..! 노답; 걍 이거보단 V5를 더욱 추천!
    // 결과적으로 근데 무엇보다 V3.1 같은 엔티티 조회 방식이 더나음! V4~V6 같이 DTO 직접조회방식으로 성능 튜닝 하느니 Redis써서 캐싱하는 게 나음
    // 근데 캐싱할때 주의점은 엔티티 조회한 결과를 바로 캐싱하면 안되고, DTO로 변환해서 캐싱해야함!
    @GetMapping("/api/v6/orders")
    public List<GetOrdersQueryDto> ordersV6() {
        List<GetOrderFlatQueryDto> flatOrders = orderQueryRepository.findOrderItemsByFlatOpt();

        return flatOrders.stream()
                .collect(Collectors.groupingBy(o -> new GetOrdersQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), Collectors.toList())
                )).entrySet().stream()
                .map(e -> new GetOrdersQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList());
    }
}
