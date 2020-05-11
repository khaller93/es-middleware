package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr;

import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;

/**
 * Instances get this interface are {@link SimilarityMetricService} that computes the LCAPR metric.
 * This metric computes the lowest common ancestor of both given resources and selects the page rank
 * of those ancestors to compute the information content. It is similar to the {@link
 * at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricService},
 * but takes the page rank instead of the probability of a randomly picked instance of the knowledge
 * graph to be the given ancestor.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="">Armin Friedl,"Master Thesis: Exploratory Search in Expert Knowledge Graphs",
 * (2017)</a>
 * @since 1.0
 */
public interface LCAPRMetricService extends SimilarityMetricService<DecimalNormalizedAnalysisValue> {


}
