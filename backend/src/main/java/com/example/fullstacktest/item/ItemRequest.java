package com.example.fullstacktest.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 500) String note
) {
}
