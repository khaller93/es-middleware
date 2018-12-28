package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AnalysisPipelineProcessorDummy implements AnalysisPipelineProcessor {

  @Override
  public void registerAnalysisService(AnalysisService analysisService, boolean requiresSPARQL,
      boolean requiresFTS, boolean requiresGremlin,
      Set<Class<? extends AnalysisService>> requirements) {
    //do nothing
  }
}
