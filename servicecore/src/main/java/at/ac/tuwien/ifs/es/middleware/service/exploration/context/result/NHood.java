package at.ac.tuwien.ifs.es.middleware.service.exploration.context.result;

import at.ac.tuwien.ifs.es.middleware.service.exploration.context.IdentifiableResult;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class NEntry implements IdentifiableResult {

  private Resource subject;
  private at.ac.tuwien.ifs.es.middleware.service.exploration.context.result.RHood RHood;

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
