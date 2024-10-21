package com.ngaidarenko.spring.springboot.golugoludocks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LockRequest {
    private String paragraphId;
    private String ownerId;
}
