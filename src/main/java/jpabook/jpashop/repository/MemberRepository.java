package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 구현체를 안만들고 이렇게만 두어도 인식함. ㄷㄷ? (규칙이 적용되기 때문)
    // 가령 findByName이라고 이름을 지으면,
    // select m from Member m where m.name = ? 이라는 쿼리를 날림
    List<Member> findByName(String name);
}
