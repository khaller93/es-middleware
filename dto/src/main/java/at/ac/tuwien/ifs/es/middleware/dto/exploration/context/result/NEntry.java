package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class NEntry implements IdentifiableResult {

  private Resource subject;
  private ResourceNeighbourhood resourceNeighbourhood;

  public NEntry(
      Resource subject,
      ResourceNeighbourhood resourceNeighbourhood) {
    this.subject = subject;
    this.resourceNeighbourhood = resourceNeighbourhood;
  }

  public Resource getSubject() {
    return subject;
  }

  public ResourceNeighbourhood getResourceNeighbourhood() {
    return resourceNeighbourhood;
  }

  @Override
  public String getId() {
    return resourceNeighbourhood.getId();
  }

  @Override
  public String toString() {
    return "NEntry{" +
        "subject=" + subject +
        ", resourceNeighbourhood=" + resourceNeighbourhood +
        '}';
  }
}
