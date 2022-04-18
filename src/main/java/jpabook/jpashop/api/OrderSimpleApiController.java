package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.dto.query.GetOrderQueryDto;
import jpabook.jpashop.dto.response.GetOrderResponseDto;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.query.OrderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * xtoOne 관계에 대한 것을 다룰 것(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * 이거 요청때리면
     * V1. 엔티티 직접 노출 (실제로 엔티티를 직접 노출하는 것은 좋지 않다. 단, 일부러 비교를 위해 V1은 엔티티 노출방식으로 구현해보는 것)
     *  1) 프록시 객체 json 매핑 실패
     *    - 설명: order member 와 order address 는 지연 로딩이다. 따라서 실제 엔티티 대신에 프록시 존재
     *    - 문제: 허나 jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모름
     *    - 해결: 그래서 Hibernate5Module을 스프링 빈으로 등록하면 해결(스프링 부트 사용중)
     *    - Hibernate5Module 모듈 등록, LAZY=null 처리 필요 ()
     *  2) 양방향 연관관계 무한 루프 문제
     *    - 설명: 엔티티 노출 방식에서 양방향 연관관계는 서로를 참조하고 있다.
     *    - 문제: 양쪽을 서로 호출하면서 무한 루프가 걸린다. (뫼비우스의 띠처럼 무한루프돌면서 데이터를 뽑아냄)
     *    - 해결: 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭! 한곳을 @JsonIgnore 처리 해야 한다.
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // FORCE_LAZY_LOADING 옵션을 끄고 아래처럼 강제초기화 하는 방법도 있음 (하지만 이것도 필요없는 방법을 이후에 보여주겠음)
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화 해줌
            order.getDelivery().getAddress(); // Lazy 강제 초기화 해줌
        }
        return all;
    }

    /**
     * V2. Dto를 활용한 방식
     *  - 허나 V2 조차도 지연로딩 초기화로 인한 불필요한 쿼리가 V1과 동일하게 많이 생성되어 나감
     *  - 일명 N + 1 문제 발생!
     */
    @GetMapping("/api/v2/simple-orders")
    public List<GetOrderResponseDto> ordersV2() {
        // 현재 db에 2개의 더미데이터가 있음. 따라서 Sql 1번으로 2개의 order 데이터를 가져오는 셈 (일단 1번의 쿼리에 + 앞으로 N(2)번의 쿼리가 예정된 셈)
        // 즉 첫번째 쿼리의 결과로 N번만큼 쿼리가 추가 실행되는 문제를 N + 1(혹은 1 + N) 문제라고 지칭함.
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        // 근데 이 예제에선 더 심각함. 1 + N(2) + N(2) 문제에 해당함. (dto 가보면 알겠지만 Lazy 초기화 쿼리가 1개가 아닌 2개가 있기 때문...!)
        // 즉, 위에서 가져온 2개의 데이터에 대한 루프를 돔(당근 2바퀴 돌겠지?) -> 근데 Dto 클래스에 가보면 1바퀴당 Lazy 초기화를 2번때리네?
        // 2바퀴를 도는데 1바퀴당 sql을 2번씩 날리게 되니 총 2 + 2 = 4회의 추가 sql 쿼리가 날라가게 됨!
        // 그럼 최초에 1 + N(2) 문제에서 나아가, 알고보니 1 + N(2) + N(2) = 5회가 날라가게 된 것!
        return orders.stream()
//                .map(order -> new GetOrderResponseDto(order)) 이거를 축약한 형태로 쓰면 아래와 같음 (같은 거라는 것 외우기)
                .map(GetOrderResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * V3. DTO + Fetch 조인을 활용한 방식
     */
    @GetMapping("/api/v3/simple-orders")
    public List<GetOrderResponseDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(order -> new GetOrderResponseDto(order))
                .collect(Collectors.toList());
    }

    /**
     * V4. JPA에서 DTO로 바로 조회함으로써 약간의 성능 상승! (정말 필요한 컬럼만 셀렉트되어 쿼리가 나가는!)
     * 다만 장단이 존재함.
     * 장점으론 db네트워크 성능 최적화: 성능이야 V4가 V3보다 소폭 더 좋은건 팩트! (근데 워낙 요즘에 컴퓨터 성능이 좋아서 솔직히 유의미한 차이가 아님)
     * 반면 단점으론 V4 방식은 일단 2차 가공이 어려움. 엔티티가 아닌 dto로 조회했기 때문에 비즈니스 로직에서 추가적인 핸들링을 하는게 불가능.
     * 딱 dto로 쿼리를 핏해서 데이터를 뽑기 때문에 해당 기능에서만 활용하기에 적합. dto로 조회한 건 다른곳에서 재활용할 수가 없음
     */
    @GetMapping("/api/v4/simple-orders")
    public List<GetOrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderDtos();
    }
}
