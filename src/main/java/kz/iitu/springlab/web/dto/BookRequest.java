package kz.iitu.springlab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.iitu.springlab.catalog.Genre;

@Schema(description = "Request body for creating or updating a book")
public record BookRequest(
        @Schema(description = "Title of the book", example = "Design Patterns", maxLength = 120)
        @NotBlank
        @Size(max = 120)
        String title,

        @Schema(description = "Author name", example = "Erich Gamma")
        @NotBlank
        String author,

        @Schema(description = "Publication year between 1450 and 2100", example = "1994", minimum = "1450", maximum = "2100")
        @Min(1450)
        @Max(2100)
        int year,

        @Schema(description = "Literary genre", example = "PROGRAMMING")
        @NotNull
        Genre genre
) {
}
