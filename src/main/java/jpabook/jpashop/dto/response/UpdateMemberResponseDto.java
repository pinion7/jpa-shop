package jpabook.jpashop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 모든 멤버변수를 바탕으로 하는 생성자 만들어줌
public class UpdateMemberResponseDto {
    private Long id;
    private String name;
}
