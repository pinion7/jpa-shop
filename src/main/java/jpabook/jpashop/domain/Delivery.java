package jpabook.jpashop.domain;

import jpabook.jpashop.DeliveryStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    // OneToOne도 fetch default는 lazy
    @OneToOne(mappedBy = "delivery", fetch = LAZY)
    private Order order;

    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
}