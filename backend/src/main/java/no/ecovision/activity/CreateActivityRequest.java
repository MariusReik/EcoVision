package no.ecovision.activity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateActivityRequest(

        @NotBlank @Size(max = 50)
        String activityTypeCode,

        @NotNull @DecimalMin(value = "0", inclusive = false)
        BigDecimal quantity,

        @NotNull @PastOrPresent
        LocalDate occurredOn,

        @Size(max = 280)
        String note) {
}
