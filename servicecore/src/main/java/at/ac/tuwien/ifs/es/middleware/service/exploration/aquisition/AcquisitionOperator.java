package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import java.io.Serializable;

/**
 * This is a marker interface for acquisition operators. These operators are gathering and preparing
 * the result list that can be enriched by {@link at.ac.tuwien.ifs.es.middleware.service.exploration.exploitation.ExploitationOperator}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AcquisitionOperator<T extends Serializable> extends ExplorationFlowStep<T> {

}
