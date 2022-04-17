package jpabook.jpashop.dto.response;

import lombok.Getter;

@Getter
public class CreateMemberResponseDto {
    private Long id;

    public CreateMemberResponseDto(Long id) {
        this.id = id;
    }
}
