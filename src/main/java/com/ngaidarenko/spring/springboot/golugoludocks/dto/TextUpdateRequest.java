package com.ngaidarenko.spring.springboot.golugoludocks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TextUpdateRequest {
    private String paragraphId;
    private String text;
}
