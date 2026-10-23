package kz.iitu.springlab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.iitu.springlab.catalog.Genre;

import java.time.Instant;

@Schema(description = "Response representation of a book in the catalogue")
public record BookResponse(
        @Schema(description = "Unique identifier", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "Design Patterns")
        String title,

        @Schema(description = "Author name", example = "Erich Gamma")
        String author,

        @Schema(description = "Publication year", example = "1994")
        int year,

        @Schema(description = "Literary genre", example = "PROGRAMMING")
        Genre genre,

        @Schema(description = "Timestamp when resource was created", example = "2026-10-23T10:15:30Z")
        Instant createdAt
) {
}
