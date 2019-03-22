package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation;

import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import java.io.Serializable;

/**
 * This is a marker interface for aggregation operator. These kind get operators aggregate the
 * results from limiting, ordering to computing sum and averages.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AggregationOperator<T extends Serializable> extends ExplorationFlowStep<T> {

}
