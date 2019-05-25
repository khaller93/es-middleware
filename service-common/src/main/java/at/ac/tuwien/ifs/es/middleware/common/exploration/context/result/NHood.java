package at.ac.tuwien.ifs.es.middleware.common.exploration.context.result;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.IdentifiableResult;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class NHood implements IdentifiableResult {

  private Resource subject;
  private RHood RHood;

  public NHood(
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
    return "NHood{" +
        "subject=" + subject +
        ", RHood=" + RHood +
        '}';
  }
}
