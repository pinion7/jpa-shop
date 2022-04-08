package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("M") // 없으면 그냥 기본 class이름으로 적용되는데, 이렇게 자신이 원하는 이름으로 넣을 수도 있다는 것 보여주려는거임
@Getter @Setter
public class Movie extends Item {

    private String director;
    private String actor;
}
