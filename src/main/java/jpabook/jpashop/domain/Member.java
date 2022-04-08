package jpabook.jpashop.domain;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    // 연관관계의 주인이 아닌 곳에 mappedBy를 넣어주고, 값으론 연관관계주인 테이블에서 ManyToOne으로 사용하고 있는 Member타입의 변수명을 넣어줌
    // mappedBy를 넣는 순간 스스로가 매핑 주체가 되는 게 아니고, Many쪽 엔티티의 필드에 의해 매핑되는 거울일 뿐이게 되는 것!
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
    /* 컬렉션은 굳이 생성자에서 초기화하지말고 이렇게 필드에서 바로 초기화하자! 그게 효율적!
     일단 null 문제에서 안전하다. 그리고 하이버네이트는 엔티티를 영속화할때 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다.
     만약 getOrders() 처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 매커니즘에 문제가 발생할 수 있다.
     (애초에 컬렉션은 한번 생성만하고 이후엔 조회만 해야함. 컬렉션의 변경을 초래하는 일은 없어야 함)
     따라서 필드레벨에서 한번만 생성하고 그 이후엔 더이상 건들지 않는 것이 가장 안전하고 코드도 간결하다.
    */
}
