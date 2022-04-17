package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.dto.Result;
import jpabook.jpashop.dto.request.CreateMemberRequestDto;
import jpabook.jpashop.dto.request.UpdateMemberRequestDto;
import jpabook.jpashop.dto.response.CreateMemberResponseDto;
import jpabook.jpashop.dto.response.GetMemberResponseDto;
import jpabook.jpashop.dto.response.UpdateMemberResponseDto;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // 비추! 엔티티를 직접활용하여 response하는 자체가 안좋음 (그리고 컬렉션을 직접 반환하는 것도 안좋음. 기본을 json으로 내보내고 거기에 배열도 포함시키는 방식이 되어야함)
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    // 컬렉션을 직접 반환하지 않고 객체로 감싸서 내보내야 결과반환에 있어 유연성이 생김!
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<GetMemberResponseDto> collect = findMembers.stream()
                .map(m -> new GetMemberResponseDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect, collect.size()); // 이렇게 한번 감싸줘야 곧장 컬력센타입으로 안나가고, 컬렉션이 객체에 담겨서 나가 결과반환의 유지보수 유연성이 생김!
    }

    // 비추! 엔티티를 직접활용하여 request하는 자체도 역시 안좋음
    @PostMapping("/api/v1/members")
    public CreateMemberResponseDto saveMemberV1(@RequestBody @Validated Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponseDto(id);
    }

    // 무조건 dto 만들어서 써! 엔티티 그대로 노출해서 쓰는 건 아주 문제될점이 많음!
    @PostMapping("/api/v2/members")
    public CreateMemberResponseDto saveMemberV2(@RequestBody @Validated CreateMemberRequestDto request) {
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponseDto(id);
    }

    @PutMapping("/api/v2/members/{memberId}")
    public UpdateMemberResponseDto updateMemberV2(
            @PathVariable Long memberId,
            @RequestBody @Validated UpdateMemberRequestDto request
    ) {
        memberService.update(memberId, request.getName());
        Member findMember = memberService.findMember(memberId);
        return new UpdateMemberResponseDto(findMember.getId(), findMember.getName());
    }
}
