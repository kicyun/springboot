package com.example.demo.model.book;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SearchHistoryResult {
    String keyword;
    private LocalDateTime cratedAt;
}
