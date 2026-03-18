package com.shopizer.gateway.preview.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Minimal "page-like" response wrapper used by preview stub endpoints.
 *
 * <p>We avoid adding Spring Data dependencies to the gateway, but still return a shape that
 * typical clients can treat like a Spring `Page` (i.e., `response.content` exists).
 *
 * <p>Additionally, for compatibility with some storefront clients, we expose the page content
 * via a `categories` JSON property alias (when used for category listing).
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
) {

  /**
   * Alias for {@link #content()} to support clients that expect a `categories` array when listing
   * store categories.
   */
  @JsonProperty("categories")
  public List<T> categories() {
    return content;
  }

  /**
   * Spring `Page`-like convenience field.
   */
  @JsonProperty("numberOfElements")
  public int numberOfElements() {
    return content == null ? 0 : content.size();
  }

  /**
   * Spring `Page`-like convenience field.
   */
  @JsonProperty("empty")
  public boolean empty() {
    return numberOfElements() == 0;
  }
}
