package at.ac.tuwien.ifs.es.middleware.service.exploration.context.result;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Identifiable;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class NHood implements Identifiable {

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
