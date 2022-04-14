package jpabook.jpashop.domain;

import jpabook.jpashop.DeliveryStatus;
import jpabook.jpashop.OrderStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 항상 Many쪽(fk가 있는 쪽)이 연관관계의 주인이라고 생각하면 됨.
    // 모든 연관관계는 Lazy로딩(지연로딩)으로 구현해야함. eager는 위험함 연관관계 다데려옴; ManyToOne은 기본 fetch가 eager라 lazy로 설정해줘야함
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id") // JoinColumn은 fk를 설정하고 이름지어준다고 생각하면 됨
    private Member member;

    // 반면 OneToMany는 fetch default가 lazy라 추가 설정이 필요없음
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // 가령 여기에 cascade를 넣어두면 order만 persist로 작업해줘도 연관된 orderItem도 같이 바꿔줌
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL) // 마찬가지로 cascade를 넣으면 order저장할때 delivery도 저장됨. 본래 모든 엔티티는 이렇게 설정안해주면 각각 다 저장, 업뎃, 삭제를 해줘야함
    @JoinColumn(name = "delivery_id") // OneToOne도 주로 쓰일 엔티티를 연관관계의 주인으로 정해서 이렇게 fk를 설정해줌
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    //-- 1. 연관관계 편의 메서드 -- (하나를 세팅할 때 다른 연관관계 엔티티도 세팅해주기 위한 편의 메서드, 양방향 세팅!)
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    // 위와 같은 편의 메서드가 없으면, 본래는 값을 세팅할때 아래처럼 비즈니스 로직이 이뤄줘야함
//    public static void main(String[] args) {
//        Member member = new Member();
//        Order order = new Order();
//        member.getOrders().add(order);
//        order.setMember(member);
//    }


    // 연관관계 편의 메서드 나머지들도 추가하겠음
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

//    protected Order() {} //@NoArgsConstructor(access = AccessLevel.PROTECTED) 로 대체 가능

    // == 2. 생성 메서드 == //
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    // == 3. 비즈니스 로직 == //
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    // == 4. 조회 로직 == //
    /**
     * 전체 주문가격 조회
     */
    public int getTotalPrice() {
        // 1. 기본적인 방식
//        int totalPrice = 0;
//        for (OrderItem orderItem : orderItems) {
//            totalPrice += orderItem.getTotalPrice();
//        }
//        return totalPrice;

        // 2. 스트림을 활용한 람다방식
        return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}
