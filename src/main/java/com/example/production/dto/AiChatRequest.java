package com.example.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @NotBlank(message = "message không được rỗng")
    @Size(max = 2000, message = "message tối đa 2000 ký tự")
    private String message;

    private String contextType = "GENERAL";
}
