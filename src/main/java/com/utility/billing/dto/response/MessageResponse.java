package com.utility.billing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** Simple message envelope for actions that have no richer payload. */
@Schema(description = "Generic message response")
public record MessageResponse(
        @Schema(example = "Account verified successfully. You can now log in.") String message
) {}
