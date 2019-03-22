package at.ac.tuwien.ifs.es.middleware.service.exploration.aspects;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This aspect injects before and after {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep}
 * is executed to log useful information like the passed payload.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Aspect
@Component
@Profile("operatorlogging")
public class ExplorationFlowStepExecutionLoggerAspect {

  private static final Logger logger = LoggerFactory
      .getLogger(ExplorationFlowStepExecutionLoggerAspect.class);

  private static final ConcurrentMap<Object, Long> operatorMap = new ConcurrentHashMap<>();
  private static final AtomicLong atomicOperatorIdCounter = new AtomicLong(0);
  private static final LongBinaryOperator counterFunction = (current, delta) ->
      current > Long.MAX_VALUE - delta ? 0 : current + delta;

  @Pointcut(value = "execution(* at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep.apply(..)) && args(context,payload) && this(flowStep)", argNames = "context,payload,flowStep")
  public void apply(ExplorationContext context, Object payload, ExplorationFlowStep flowStep) {
  }

  @Before(value = "apply(context,payload,flowStep)", argNames = "context,payload,flowStep")
  public void doBeforeTask(ExplorationContext context, Object payload,
      ExplorationFlowStep flowStep) {
    long operatorId = atomicOperatorIdCounter.getAndAccumulate(1, counterFunction);
    logger.debug("Operator '{}'({}) scheduled with payload {}.", flowStep.getUID(), operatorId,
        payload);
    operatorMap.put(payload, operatorId);
  }

  @AfterReturning(value = "apply(context,payload,flowStep)", argNames = "context,payload,flowStep")
  public void doAfterTaskSuccessful(ExplorationContext context, Object payload,
      ExplorationFlowStep flowStep) {
    logger.debug("Operator '{}'({}) successfully applied.", flowStep.getUID(),
        operatorMap.get(payload));
    operatorMap.remove(payload);
  }

  @AfterThrowing(value = "apply(context,payload,flowStep)", argNames = "context,payload,flowStep,exception", throwing = "exception")
  public void doAfterTaskFailed(ExplorationContext context, Object payload,
      ExplorationFlowStep flowStep, Exception exception) {
    logger.debug("Operator '{}' successfully failed with '{}'.", flowStep.getUID(), exception);
  }

}
