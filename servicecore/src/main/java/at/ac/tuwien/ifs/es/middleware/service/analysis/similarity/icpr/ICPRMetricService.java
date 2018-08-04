package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr;

import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityMetricService;

/**
 * Instances of this interface are {@link SimilarityMetricService} that computes the ICPR metric.
 * This metric computes the least common subsumer of both given resources and selects the page rank
 * of those subsumers to compute the information content. It is similar to the {@link
 * at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricService},
 * but takes the page rank instead of the probability of a randomly picked instance of the knowledge
 * graph to be the given subsumer.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="">Armin Friedl,"Master Thesis: Exploratory Search in Expert Knowledge Graphs",
 * (2018)</a>
 * @since 1.0
 */
public interface ICPRMetricService extends SimilarityMetricService<Double> {

}
