package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;

/**
 * A value maintained by normalizer classes.
 *
 * @author Kevin Haller
 * @version 1.2
 * @since 1.2
 */
class V {

  private final BigDecimal value;
  private final BigDecimal n;

  public V(BigDecimal value) {
    this(value, 1);
  }

  public V(BigDecimal value, long n) {
    checkArgument(value != null, "The value must not be null.");
    this.value = value;
    this.n = BigDecimal.valueOf(n);
  }

  /**
   * Gets the raw value of this object.
   *
   * @return the raw value in form of a {@link BigDecimal}.
   */
  public BigDecimal getRawValue() {
    return value;
  }

  /**
   * Adds the given value to this value.
   *
   * @param other value which shall be added to this value.
   * @return the result of the addition as a value.
   */
  public V add(V other) {
    return new V(other.value.multiply(other.n).add(this.value.multiply(this.n)), 1);
  }

  /**
   * Subtracts the given value from this value.
   *
   * @param other value which shall be subtracted from this value.
   * @return the result of the subtraction as a value.
   */
  public V subtract(V other) {
    return new V(this.value.multiply(this.n).subtract(other.value.multiply(other.n)), 1);
  }

  /**
   *
   * @param n
   * @return
   */
  public V pow(int n) {
    return new V(this.value.pow(n), this.n.longValue());
  }

  /**
   * Compares this value against the given one, and returns the minimum value.
   *
   * @param other value which shall be compared with this one.
   * @return the minimum value.
   */
  public V min(V other) {
    if (this.value.compareTo(other.value) < 0) {
      return this;
    }
    return other;
  }

  /**
   * Compares this value against the given one, and returns the maximum value.
   *
   * @param other value which shall be compared with this one.
   * @return the maximum value.
   */
  public V max(V other) {
    if (this.value.compareTo(other.value) > 0) {
      return this;
    }
    return other;
  }

}
