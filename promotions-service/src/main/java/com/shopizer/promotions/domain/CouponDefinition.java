package com.shopizer.promotions.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Coupon definition stored by promotions-service.
 *
 * <p>Invariants (also mirrored by DB CHECK constraint):
 * <ul>
 *   <li>PERCENTAGE: percentageOff != null, amountOff == null, currency == null</li>
 *   <li>FIXED_AMOUNT: amountOff != null, currency != null, percentageOff == null</li>
 * </ul>
 */
@Entity
@Table(name = "coupon_definition", schema = "shopizer")
public class CouponDefinition {

  public enum CouponType {
    PERCENTAGE,
    FIXED_AMOUNT
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code", nullable = false, unique = true, length = 64)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 16)
  private CouponType type;

  @Column(name = "percentage_off", precision = 7, scale = 4)
  private BigDecimal percentageOff;

  @Column(name = "amount_off", precision = 19, scale = 4)
  private BigDecimal amountOff;

  @Column(name = "currency", length = 3)
  private String currency;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Column(name = "starts_at")
  private Instant startsAt;

  @Column(name = "ends_at")
  private Instant endsAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    normalize();
    validateInvariants();
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    normalize();
    validateInvariants();
    this.updatedAt = Instant.now();
  }

  private void normalize() {
    if (code != null) {
      code = code.trim().toUpperCase(Locale.ROOT);
    }
    if (currency != null) {
      currency = currency.trim().toUpperCase(Locale.ROOT);
    }
  }

  private void validateInvariants() {
    if (code == null || code.isBlank()) {
      throw new IllegalStateException("CouponDefinition.code must be non-blank");
    }
    if (type == null) {
      throw new IllegalStateException("CouponDefinition.type must be set");
    }

    if (type == CouponType.PERCENTAGE) {
      if (percentageOff == null) {
        throw new IllegalStateException("percentageOff must be provided for PERCENTAGE coupons");
      }
      if (amountOff != null || currency != null) {
        throw new IllegalStateException("amountOff/currency must be null for PERCENTAGE coupons");
      }
    } else if (type == CouponType.FIXED_AMOUNT) {
      if (amountOff == null || currency == null || currency.isBlank()) {
        throw new IllegalStateException("amountOff and currency must be provided for FIXED_AMOUNT coupons");
      }
      if (percentageOff != null) {
        throw new IllegalStateException("percentageOff must be null for FIXED_AMOUNT coupons");
      }
    }
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public CouponType getType() {
    return type;
  }

  public void setType(CouponType type) {
    this.type = type;
  }

  public BigDecimal getPercentageOff() {
    return percentageOff;
  }

  public void setPercentageOff(BigDecimal percentageOff) {
    this.percentageOff = percentageOff;
  }

  public BigDecimal getAmountOff() {
    return amountOff;
  }

  public void setAmountOff(BigDecimal amountOff) {
    this.amountOff = amountOff;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Instant getStartsAt() {
    return startsAt;
  }

  public void setStartsAt(Instant startsAt) {
    this.startsAt = startsAt;
  }

  public Instant getEndsAt() {
    return endsAt;
  }

  public void setEndsAt(Instant endsAt) {
    this.endsAt = endsAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CouponDefinition that)) return false;
    return Objects.equals(id, that.id) && Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code);
  }
}
