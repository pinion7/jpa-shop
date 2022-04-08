package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // 기본 생성자가 그래도 따로 있어야함. JPA 기본 스펙상의 문제 -> 그래야 가동되는데 문제가 없음.
    // 대신에 public으로 그냥 두면 함부로 개발자가 new키워드로 생성할 수도 있는데, JPA 기본 스펙에서는 protected까지는 허용해 줌
    // 이렇게 해두면 아무렇게나 new 키워드로 활용해서는 안되는 생성자구나 라고 인식할 수 있음
    protected Address() {
    }

    // 사실 setter는 엥간하믄 사용하면 안됨. 그만큼 side effect 발생 가능성이 높음. setter를 모두 열어놓으면 변경포인트가 많아서 유지보수가 어려움
    // 최초에 생성자로 되도록 생성하고, 정 cud가 또 필요하면 따로 메서드를 명확하게 만들어서 써야함.
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
