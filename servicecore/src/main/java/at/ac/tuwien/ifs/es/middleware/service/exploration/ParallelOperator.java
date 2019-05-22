package at.ac.tuwien.ifs.es.middleware.service.exploration;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.factory.DynamicExplorationFlowFactory;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.ParallelPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.request.ExplorationFlowStepRequest;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.parallel")
public class ParallelOperator implements
    ExplorationFlowStep<ExplorationContext, ExplorationContext, ParallelPayload> {

  private static final Logger logger = LoggerFactory.getLogger(ParallelOperator.class);

  private final DynamicExplorationFlowFactory dynamicExplorationFlowFactory;
  private final TaskExecutor taskExecutor;

  @Autowired
  public ParallelOperator(
      DynamicExplorationFlowFactory dynamicExplorationFlowFactory,
      TaskExecutor taskExecutor) {
    this.dynamicExplorationFlowFactory = dynamicExplorationFlowFactory;
    this.taskExecutor = taskExecutor;
  }

  @Override
  public String getUID() {
    return "esm.parallel";
  }

  @Override
  public Class<ExplorationContext> getExplorationContextInputClass() {
    return ExplorationContext.class;
  }

  @Override
  public Class<ExplorationContext> getExplorationContextOutputClass() {
    return ExplorationContext.class;
  }

  @Override
  public Class<ParallelPayload> getPayloadClass() {
    return ParallelPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, ParallelPayload payload) {
    logger.debug("A parallel computation of the given flows {} was issued.", payload);
    ExecutorCompletionService<ExplorationContext> completionService = new ExecutorCompletionService<>(
        taskExecutor);
    List<List<ExplorationFlowStepRequest>> flows = payload.getFlows();
    if (flows != null && !flows.isEmpty()) {
      for (List<ExplorationFlowStepRequest> flow : flows) {
        completionService.submit(new ExplorationContextCallback(context, flow));
      }
      for (int i = 0; i < flows.size(); i++) {
        try {
          Future<ExplorationContext> ctxFuture = completionService.take();
          ExplorationContext ctx = ctxFuture.get();
          context.values().merge(ctx.values());
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
          logger.error("Error occurred while computing flows in parallel. {}", e.getMessage());
        }
      }
    }
    logger.debug("A parallel computation of the given flows {} has finished.", payload);
    return context;
  }

  /**
   * Callback for the parallel execution of {@link ExplorationFlow}s.
   */
  private final class ExplorationContextCallback implements Callable<ExplorationContext> {

    private ExplorationContext context;
    private List<ExplorationFlowStepRequest> flow;

    public ExplorationContextCallback(ExplorationContext context,
        List<ExplorationFlowStepRequest> flow) {
      this.context = context;
      this.flow = flow;
    }

    @Override
    public ExplorationContext call() {
      return dynamicExplorationFlowFactory.constructFlow(context, flow).execute();
    }
  }

}
