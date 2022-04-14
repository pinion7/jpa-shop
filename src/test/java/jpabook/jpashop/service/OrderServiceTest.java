package jpabook.jpashop.service;

import jpabook.jpashop.DeliveryStatus;
import jpabook.jpashop.OrderStatus;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.excption.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    void order() {
        // given
        Member member = createMember();
        Book book = createBook("좋은 책", 10000, 10);
        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER); // 상품 주문시 상태는 ORDER여야 함.
        assertThat(getOrder.getOrderItems().size()).isEqualTo(1); // 주문 상품은 한개이므로 사이즈는 1이어야 함.
        assertThat(book.getStockQuantity()).isEqualTo(10 - orderCount) ;// 주문 수량만큼 재고가 줄어야 함
        assertThat(getOrder.getTotalPrice()).isEqualTo(book.getPrice() * orderCount); // 주문 가격은 가격 * 수량이어야 함.
    }

    @Test
    void overOrder() {
        // given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);
        int orderCount = 11;

        // when & then
        assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), item.getId(), orderCount);
        });
    }

    @Test
    void cancelOrder() {
        // given
        Member member = createMember();
        Item item = createBook("나쁜 책", 10000, 10);
        int orderCount = 5;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL); // 취소하면 주문상태가 cancel되어야 하고
        assertThat(item.getStockQuantity()).isEqualTo(10); // 재고수량이 다시 원복이 되어야 함.
//        assertThat(getOrder.getTotalPrice()).isEqualTo(item.getPrice() * orderCount); // 이것도 원복해야되지 않을까?
    }

    @Test
    void cancelCompletedOrder() {
        // given
        Member member = createMember();
        Item item = createBook("나쁜 책", 10000, 10);
        int orderCount = 5;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);
        Order getOrder = orderRepository.findOne(orderId);
        getOrder.getDelivery().setStatus(DeliveryStatus.COMP); // 배송완료로 강제 바꿈

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            orderService.cancelOrder(orderId);
        }); // 배송완료되었기 때문에 취소 불가능! -> IllegalStateException 발생!
    }



    private Book createBook(String name, int orderPrice, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(orderPrice);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울","강가", "123-123"));
        em.persist(member);
        return member;
    }
}