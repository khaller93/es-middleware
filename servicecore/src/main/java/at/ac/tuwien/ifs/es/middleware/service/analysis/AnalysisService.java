package at.ac.tuwien.ifs.es.middleware.service.analysis;

/**
 * Instances of this interface represent services that analyse the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AnalysisService<C> {

  /**
   * Computes the analysis.
   */
  C compute();

  /**
   *
   * @return
   */
  AnalysisEventStatus getStatus();
}