package at.ac.tuwien.ifs.exploratorysearch.service.exploration.aquisition;

import at.ac.tuwien.ifs.exploratorysearch.service.exception.ExplorationFlowSpecificationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AcquisitionSourceFactory {

  private ApplicationContext context;

  public AcquisitionSourceFactory(@Autowired ApplicationContext context) {
    this.context = context;
  }

  /**
   * @param name for which the corresponding {@link AcquisitionSource} shall be obtained.
   * @return corresponding {@link AcquisitionSource} for the given name.
   * @throws ExplorationFlowSpecificationException if there is no {@link AcquisitionSource} for the
   * given name.
   */
  public AcquisitionSource get(String name) throws ExplorationFlowSpecificationException {
    try {
      return context
          .getBean(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase()
              + "AcquisitionSource", AcquisitionSource.class);
    } catch (NoSuchBeanDefinitionException bean) {
      throw new ExplorationFlowSpecificationException(
          String.format("'%s' does not reference a known acquisition source.", name));
    }
  }

}
