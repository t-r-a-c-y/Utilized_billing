package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Generate a bill from an existing meter reading")
public record BillGenerateRequest(

        @Schema(example = "1", description = "The meter to bill")
        @NotNull Long meterId,

        @Schema(example = "5")
        @NotNull @Min(1) @Max(12) Integer month,

        @Schema(example = "2026")
        @NotNull @Min(2000) Integer year,

        @Schema(example = "15", description = "Days until the bill is due (default 15)")
        @Min(1) Integer dueInDays
) {}
