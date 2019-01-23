package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import java.io.Serializable;
import java.util.Objects;

public class RP implements Serializable {

  private String resourceA;
  private String resourceB;

  private RP(String resourceA, String resourceB) {
    this.resourceA = resourceA;
    this.resourceB = resourceB;
  }

  public static RP of(String resourceA, String resourceB) {
    return new RP(resourceA, resourceB);
  }

  public static RP of(ResourcePair pair) {
    return new RP(pair.getFirst().getId(), pair.getSecond().getId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RP rp = (RP) o;
    return resourceA.equals(rp.resourceA) &&
        resourceB.equals(rp.resourceB);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resourceA, resourceB);
  }

  @Override
  public String toString() {
    return "RP{" +
        "resourceA='" + resourceA + '\'' +
        ", resourceB='" + resourceB + '\'' +
        '}';
  }
}
