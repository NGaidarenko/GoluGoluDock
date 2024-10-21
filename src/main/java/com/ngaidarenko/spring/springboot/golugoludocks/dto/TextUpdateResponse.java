package com.ngaidarenko.spring.springboot.golugoludocks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TextUpdateResponse {
    private String paragraphId;
    private String text;

}
