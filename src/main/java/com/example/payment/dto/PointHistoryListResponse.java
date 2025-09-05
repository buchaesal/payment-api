package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PointHistoryListResponse {
    private String status;
    private List<PointHistoryResponse> pointHistories;
    
    public PointHistoryListResponse(String status, List<PointHistoryResponse> pointHistories) {
        this.status = status;
        this.pointHistories = pointHistories;
    }
}