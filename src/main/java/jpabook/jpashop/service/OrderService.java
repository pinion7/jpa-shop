package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        // 1. 엔티티 조회
        Member member = memberRepository.findById(memberId).get();
        Item item = itemRepository.findOne(itemId);

        // 2. 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 3. 주문상품 생성 (이 생성메서드가 아닌 다른 방식을 생성하는 건 막아야 됨 -> 가령 생성자로 해당 객체를 생성하는 것을 막는 방법 -> protected키워드로 객체에 빈생성자 만들어두기!
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 4. 주문 생성 (3번과 이하동문)
        Order order = Order.createOrder(member, delivery, orderItem);

        // 5. 각각에 주문 저장 (cascade 적용이 되어 있어서 하나만 저장해줘도 orderItem, delivery에 다 order가 저장 -> 근데 잘써야함 위험할수 있음 차라리 각각 다 repository만들어서 쓰는 게 안전)
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);

        // 주문 취소
        order.cancel();
    }

    /**
     * 주문 검색
     */
    public List<Order> findOrders(OrderSearch orderSearch) {

        // 스트링 합치기 방식 주석처리
//        return orderRepository.findAllByString(orderSearch);

        // queryDsl 방식 적용!
        return orderRepository.findAll(orderSearch);
    }
}
