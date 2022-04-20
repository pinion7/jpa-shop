package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepositoryOld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

//@RunWith(SpringRunner.class) // junit4 버전
@ExtendWith(SpringExtension.class) // junit5 버전
@SpringBootTest
@Transactional // 테스트케이스에 붙으면 끝나고 롤백 해줌!
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired
    MemberRepositoryOld memberRepository;
//    @Autowired EntityManager em; // 롤백을 비활성화 시키면서도 commit을 보는 방법 (em 만들어서 flush시키기!)

    @Test
//    @Rollback(false) // 기본적으로 이게 true라 transaction commit이 안이루어지고 롤백됨. commit을 보고싶으면 롤백을 활성화시키면 됨.
    void createMember() {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        Long memberId = memberService.join(member);

        //then
//        em.flush(); // 이렇게하면 이후 롤백이 적용되더라도, 일단 현재 commit이 강제로 이루어지기 때문에 db에 쿼리는 날라감!
        assertThat(memberId).isEqualTo(memberRepository.findOne(memberId).getId());
    }

    @Test
    void throwDuplicateEx() {
        // given
        Member member1 = new Member();
        member1.setName("lee");

        Member member2 = new Member();
        member2.setName("lee");

        // when
        memberService.join(member1);

        //then
        assertThrows(IllegalStateException.class, () -> {
            memberService.join(member2); // 동일한 이름이라 예외 터짐!
        });

//        assertThatThrownBy(IllegalStateException.class, "이미 존재하는 회원입니다.", )
    }

    @Test
    void findMembers() {
        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("lee");

        Member member3 = new Member();
        member3.setName("park");

        // when
        memberService.join(member1);
        memberService.join(member2);
        memberService.join(member3);

        //then
        List<Member> members = memberService.findMembers();
        assertThat(members.size()).isEqualTo(3);
    }

    @Test
    void findMember() {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        memberService.join(member);

        //then
        Member findMember = memberService.findMember(member.getId());
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getName()).isEqualTo(member.getName());
    }
}