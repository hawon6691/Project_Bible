package com.pbshop.java.spring.maven.jpa.postgresql.inquiry.dto;

import jakarta.validation.constraints.NotBlank;

public final class InquiryDtos {

    private InquiryDtos() {
    }

    public record CreateInquiryRequest(
            @NotBlank String title,
            @NotBlank String content,
            boolean isSecret
    ) {
    }

    public record AnswerInquiryRequest(
            @NotBlank String answer
    ) {
    }
}
