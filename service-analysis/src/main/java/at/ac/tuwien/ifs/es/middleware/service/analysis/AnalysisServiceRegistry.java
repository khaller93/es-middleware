package at.ac.tuwien.ifs.es.middleware.service.analysis;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.FullTextSearchService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService;
import com.google.common.collect.Sets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * This service provides utilities for scanning and gathering all registered {@link
 * AnalysisService}s that can be found in the application.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class AnalysisServiceRegistry {

  private static final Logger logger = LoggerFactory.getLogger(AnalysisServiceRegistry.class);

  private final Environment env;
  private final Map<String, AnalysisServiceEntry> analysisServiceMap;

  /**
   * Creates a new {@link AnalysisServiceRegistry}.
   *
   * @param env that contains configurations for the analysis services.
   */
  @Autowired
  public AnalysisServiceRegistry(Environment env) {
    this.env = env;
    this.analysisServiceMap = new ConcurrentHashMap<>();
  }

  /**
   * Scans for {@link AnalysisService} registered with {@link RegisterForAnalyticalProcessing} using
   * the given Spring context {@link ApplicationContext}.
   *
   * @param context {@link ApplicationContext}
   */
  public void scanAndRegisterAnalysisServices(ApplicationContext context) {
    BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) context;
    String[] registerForBeans = context
        .getBeanNamesForAnnotation(RegisterForAnalyticalProcessing.class);
    for (String beanName : registerForBeans) {
      BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
      String className = beanDefinition.getBeanClassName();
      try {
        Class<?> aClass = Class.forName(className);
        if (AnalysisService.class.isAssignableFrom(aClass)) {
          RegisterForAnalyticalProcessing annotation = aClass
              .getAnnotation(RegisterForAnalyticalProcessing.class);
          logger.info("Found the analytical processing class named with '{}' ({}).",
              aClass.getSimpleName(), annotation.name(), aClass);
          try {
            registerAnalysisService((AnalysisService) context.getBean(aClass), annotation);
          } catch (BeansException b) {
            logger.error("Could not register analysis service '{}' ({}).", annotation.name(),
                aClass);
            b.printStackTrace();
          }
        } else {
          logger.warn(
              "Found class '{}' annotated for registration in analysis processing, but did not implement the {} interface.",
              aClass, AnalysisService.class.getSimpleName());
        }
      } catch (ClassNotFoundException e) {
        logger
            .warn("Was not able to register the analysis service class '{}'. {}",
                className, e.getMessage());
      }
    }
  }

  /**
   * Registers the given analysis service.
   *
   * @param analysisService {@link AnalysisService} that shall be registered.
   * @param annotation {@link RegisterForAnalyticalProcessing} that describes the given analysis
   * service.
   */
  private void registerAnalysisService(AnalysisService analysisService,
      RegisterForAnalyticalProcessing annotation) {
    checkArgument(analysisService != null, "Analysis service must not be null for registration.");
    checkArgument(annotation != null,
        "RegisterForAnalyticalProcessing annotation must be specified for registration.");
    Set<Class<?>> combinedRequirements = Sets.newHashSet(annotation.prerequisites());
    if (annotation.requiresSPARQL()) {
      combinedRequirements.add(SPARQLService.class);
    }
    if (annotation.requiresFullTextSearch()) {
      combinedRequirements.add(FullTextSearchService.class);
    }
    if (annotation.requiresGremlin()) {
      combinedRequirements.add(GremlinService.class);
    }
    logger.debug("Registers analysis service {} with requirements {}.", analysisService,
        combinedRequirements);
    if (!annotation.disabled()) {
      analysisServiceMap
          .put(annotation.name(),
              new AnalysisServiceEntry(annotation.name(), analysisService, combinedRequirements,
                  env.getProperty("esm.analysis.disable." + annotation.name(), "false").trim()
                      .toLowerCase().equals("true")));
    }
  }

  /**
   * Gets a list of deep copied {@link AnalysisServiceEntry}.
   *
   * @return a list of deep copied {@link AnalysisServiceEntry}.
   */
  public List<AnalysisServiceEntry> getRegisteredAnalysisServices() {
    return new LinkedList<>(analysisServiceMap.values());
  }

}
