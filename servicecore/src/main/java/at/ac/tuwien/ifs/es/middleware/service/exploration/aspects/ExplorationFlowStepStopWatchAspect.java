package at.ac.tuwien.ifs.es.middleware.service.exploration.aspects;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This aspect injects before and after {@link ExplorationFlowStep} is executed to log the elapsed
 * time between the execution of an operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Aspect
@Component
@Profile("operatorwatch")
public class ExplorationFlowStepStopWatchAspect {

  private static final Logger logger = LoggerFactory
      .getLogger(ExplorationFlowStepStopWatchAspect.class);

  private static final AtomicLong atomicOperatorIdCounter = new AtomicLong(0);
  private static final LongBinaryOperator counterFunction = (current, delta) ->
      current > Long.MAX_VALUE - delta ? 0 : current + delta;

  private ObjectMapper objectMapper;
  private Boolean loggingExecution;
  private Boolean stopWatch;
  private Boolean stopWatchLogging;

  @Autowired
  public ExplorationFlowStepStopWatchAspect(ObjectMapper objectMapper,
      @Value("${esm.flow.execution.logging:#{true}}") Boolean loggingExecution,
      @Value("${esm.flow.stopwatch:#{true}}") Boolean stopWatch,
      @Value("${esm.flow.stopwatch.logging:#{true}}") Boolean stopWatchLogging) {
    this.objectMapper = objectMapper;
    this.loggingExecution = loggingExecution;
    this.stopWatch = stopWatch;
    this.stopWatchLogging = stopWatchLogging;
  }

  @Pointcut(value = "execution(* at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep.apply(..)) && args(context,payload) && this(flowStep)", argNames = "context,payload,flowStep")
  public void apply(ExplorationContext context, Object payload, ExplorationFlowStep flowStep) {
  }

  @Around(value = "apply(context,payload,flowStep)", argNames = "proceedingJoinPoint,context,payload,flowStep")
  public ExplorationContext doTaskAround(ProceedingJoinPoint proceedingJoinPoint,
      ExplorationContext context,
      Object payload, ExplorationFlowStep flowStep) throws Throwable {
    long id = atomicOperatorIdCounter.accumulateAndGet(1, counterFunction);
    if (loggingExecution) {
      logger.debug("Operator '{}'({}) scheduled with payload {}.", flowStep.getUID(), id, payload);
    }
    Instant start = Instant.now();
    try {
      ExplorationContext returnedContext = (ExplorationContext) proceedingJoinPoint.proceed();
      if (stopWatch && returnedContext != null) {
        StopWatchInfo stopWatchInfo = new StopWatchInfo(flowStep.getUID(), start, Instant.now());
        ObjectNode stopWatchOperatorInfo = objectMapper.valueToTree(stopWatchInfo);
        ObjectNode stopWatch = (ObjectNode) ((Optional<JsonNode>) returnedContext
            .getMetadataFor("stopwatch")).orElse(JsonNodeFactory.instance.objectNode());
        stopWatch.set(String.valueOf(id), stopWatchOperatorInfo);
        returnedContext.setMetadataFor("stopwatch", stopWatch);
        if (stopWatchLogging) {
          logger.debug("Operator '{}'({}) processing time: {}.", flowStep.getUID(), id,
              stopWatchInfo);
        }
      }
      if (loggingExecution) {
        logger.debug("Operator '{}'({}) successfully applied.", flowStep.getUID(), id);
      }
      return returnedContext;
    } catch (Throwable throwable) {
      if (loggingExecution) {
        logger.debug("Operator '{}' successfully failed with '{}'.", flowStep.getUID(), throwable);
      }
      throw throwable;
    }
  }

  /**
   * Info for processing time measurement.
   */
  private static class StopWatchInfo implements Serializable {

    private String name;
    private Instant start;
    private Instant end;
    private Long processingTimeInMs;

    public StopWatchInfo(String name, Instant start, Instant end) {
      checkArgument(name != null && !name.isEmpty(), "The given name must not be null or empty.");
      checkArgument(start != null && end != null, "The start and end timestamp must not be null.");
      this.name = name;
      this.start = start;
      this.end = end;
      this.processingTimeInMs = start.until(end, ChronoUnit.MILLIS);
    }

    public String getName() {
      return name;
    }

    public Instant getStart() {
      return start;
    }

    public Instant getEnd() {
      return end;
    }

    public Long getProcessingTimeInMs() {
      return processingTimeInMs;
    }

    @Override
    public String toString() {
      return "StopWatchInfo{" +
          "name='" + name + '\'' +
          ", start=" + start +
          ", end=" + end +
          ", processingTimeInMs=" + processingTimeInMs +
          '}';
    }
  }

}
