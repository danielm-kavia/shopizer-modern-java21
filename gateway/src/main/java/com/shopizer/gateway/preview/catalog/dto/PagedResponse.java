package com.shopizer.gateway.preview.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Minimal "page-like" response wrapper used by preview stub endpoints.
 *
 * <p>We avoid adding Spring Data dependencies to the gateway, but still return a shape that
 * typical clients can treat like a Spring `Page` (i.e., `response.content` exists).
 */
@Schema(name = "PagedResponse", description = "Minimal page-like response with content + pagination metadata.")
public record PagedResponse<T>(
    @Schema(description = "Page content")
    List<T> content,
    @Schema(description = "0-based page number")
    int number,
    @Schema(description = "Requested page size")
    int size,
    @Schema(description = "Total number of elements across all pages")
    long totalElements,
    @Schema(description = "Total number of pages")
    int totalPages,
    @Schema(description = "Whether this is the first page")
    boolean first,
    @Schema(description = "Whether this is the last page")
    boolean last
) {}
