package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSumersService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityMetricService;

/**
 * This service finds the least common subsumers of two resources (see {@link
 * LeastCommonSubSumersService} can be
 * used). Then the information content is computed for each of the subsumers. Information content is
 * a measurement for uncertainty and thus informational value. It represents the likelihood of
 * picking an instance of this subsumer, when randomly picking one instance from the whole data. The
 * Resnik similarity for a pair of resources is then the maximum of all common subsumers.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://arxiv.org/abs/cmp-lg/9511007">Resnik, Philip. "Using
 * Information Content to Evaluate Semantic Similarity in a Taxonomy" (1995)</a>
 * @since 1.0
 */
public interface ResnikSimilarityMetricService extends SimilarityMetricService<Double> {

}
