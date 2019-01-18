package at.ac.tuwien.ifs.es.middleware.service.exploration.registry;

import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * This class implements a scanner for classes that are annotated with the {@link
 * RegisterForExplorationFlow} annotation and implemented the {@link
 * at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep} interface. All found
 * classes are registered at {@link ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class AutomaticExplorationFlowStepScanner implements
    ApplicationListener<ContextRefreshedEvent> {

  private static final Logger logger = LoggerFactory
      .getLogger(AutomaticExplorationFlowStepScanner.class);

  private ExplorationFlowRegistry explorationFlowRegistry;

  public AutomaticExplorationFlowStepScanner(
      @Autowired ExplorationFlowRegistry explorationFlowRegistry) {
    this.explorationFlowRegistry = explorationFlowRegistry;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext applicationContext = event.getApplicationContext();
    BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext;
    String[] registerForBeans = applicationContext
        .getBeanNamesForAnnotation(RegisterForExplorationFlow.class);
    for (String beanName : registerForBeans) {
      BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
      String className = beanDefinition.getBeanClassName();
      try {
        Class<?> aClass = Class.forName(className);
        if (ExplorationFlowStep.class.isAssignableFrom(aClass)) {
          String registrationName = aClass
              .getAnnotation(RegisterForExplorationFlow.class).value();
          logger.info("Found the exploration flow step get class ({}) with name '{}'",
              aClass.getSimpleName(), aClass, registrationName);
          explorationFlowRegistry.register(registrationName,
              (Class<? extends ExplorationFlowStep>) aClass);
        } else {
          logger.warn(
              "Found {} annotated for registration in exploration flow, but did not implement the {} interface.",
              aClass, ExplorationFlowStep.class.getSimpleName());
        }
      } catch (ClassNotFoundException e) {
        logger
            .warn("Was not able to register the exploration flow step get class '{}'. {}", className,
                e.getMessage());
      }
    }
  }
}
