package at.ac.tuwien.ifs.es.middleware.service.exploration;

import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.AcquisitionSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Component
public class ExplorationFlowFactory {

  private AcquisitionSourceFactory acquisitionSourceFactory;

  @Autowired
  public ExplorationFlowFactory(AcquisitionSourceFactory acquisitionSourceFactory) {
    this.acquisitionSourceFactory = acquisitionSourceFactory;
  }

  public ExplorationFlow get(String source) throws ExplorationFlowSpecificationException {
    ExplorationFlow explorationFlow = new ExplorationFlow(acquisitionSourceFactory.get(source));
    //TODO: Implement
    return explorationFlow;
  }
}
