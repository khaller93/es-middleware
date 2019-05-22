package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class NEntry implements IdentifiableResult {

  private Resource subject;
  private RHood RHood;

  public NEntry(
      Resource subject,
      RHood RHood) {
    this.subject = subject;
    this.RHood = RHood;
  }

  public Resource getSubject() {
    return subject;
  }

  public RHood getRHood() {
    return RHood;
  }

  @Override
  public String getId() {
    return RHood.getId();
  }

  @Override
  public String toString() {
    return "NEntry{" +
        "subject=" + subject +
        ", RHood=" + RHood +
        '}';
  }
}
