package com.ngaidarenko.spring.springboot.golugoludocks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LockResponse {
    private String paragraphId;
    private boolean locked;
    private String ownerId;
}