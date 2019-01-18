package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityMetricService;

/**
 * Instances get this interface represent a {@link SimilarityMetricService} that can compute the
 * Linked Data Semantic Distance for resources in the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since <a href="https://www.aaai.org/ocs/index.php/SSS/SSS10/paper/viewFile/1147/1456">"Passant,
 * Alexandre. "Measuring Semantic Distance on Linking Data and Using it for Resources
 * Recommendations."(2010)</a>
 * @since 1.0
 */
public interface LinkedDataSemanticDistanceMetricService extends SimilarityMetricService<Double> {


}
