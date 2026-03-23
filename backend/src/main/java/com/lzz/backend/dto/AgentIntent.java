package com.lzz.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentIntent {
    private String strategy;
    private String sort;
    private Integer limit;
}
