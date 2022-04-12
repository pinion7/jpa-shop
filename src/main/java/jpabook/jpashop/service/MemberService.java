package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 이렇게 해주면 읽기전용으로 디비 성능 최적화 해줌
//@AllArgsConstructor // 필드를 체크한 뒤, 생성자 및 의존성 주입을 다 해줌
@RequiredArgsConstructor // 위와 같으나 final 키워드가 있는 필드에 한해서만 해당 작업을 해 줌 -> 가장 좋은 방식
public class MemberService {

    // 방식 1: 필드 DI 직접 주입 -> 권장 X
//    @Autowired
    private final MemberRepository memberRepository; // 결국엔 이거빼고 다생략가능 ㅋㅋㅋ

    // 방식 2: setter DI 주입 -> 권장 X
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    // 방식 3: 생성자 DI 주입 -> 이게 가장 추천되는 방식
//    @Autowired // 생성자가 하나면 자동으로 @Autowired를 주입해주기 때문에 생략가능!
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    // 회원가입
    @Transactional // 이렇게 안에 더 넣어주면, 안에 넣은걸 적용하게 됨. 그리고 readOnly는 디폴트가 false임
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }


    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 회원 단건 조회
    public Member findMember(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
