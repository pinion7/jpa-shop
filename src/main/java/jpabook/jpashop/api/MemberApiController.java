package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.dto.request.CreateMemberRequestDto;
import jpabook.jpashop.dto.response.CreateMemberResponseDto;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

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
}
