package jpabook.jpashop.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
public class CreateMemberRequestDto {
    @NotEmpty
    private String name;
}
