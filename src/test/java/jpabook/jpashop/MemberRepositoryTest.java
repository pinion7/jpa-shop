package jpabook.jpashop;

import jpabook.jpashop.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository; // Di 주입

    @Test
    @Transactional // 이게 있어야 테스트가 가능함. jpa는 반드시 트랜잭션과 함께해야하므로. (단, 테스트에 저 어노테이션이 있으면 테스트가 끝나고 다 롤백이됨: 즉 db에 변화는 없음!)
    @Rollback(false) // 허나 이걸 넣으면 롤백 안해줌! db에서 확인하고 싶으면 쓰면 됨!
    void testMember() {
        // given
        Member member = new Member();
        member.setUsername("memberA");

        // when
        Long saveId = memberRepository.save(member);
        Member findMember = memberRepository.find(saveId);

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

        // 아래는 true!
        // 같은 트랜잭션 안에서 저장 및 조회 하면, 영속성 컨텍스트가 똑같을 수밖에 없음. 같은 영속성 컨텍스트에서 id값이 같으면, 같은 엔티티로 식별함!
        Assertions.assertThat(findMember).isEqualTo(member);
        System.out.println("findMember == member: " + (findMember == member));
    }
    

}