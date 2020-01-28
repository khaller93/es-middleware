package at.ac.tuwien.ifs.es.middleware.controller;

import at.ac.tuwien.ifs.es.middleware.controller.meta.EFExceptionDTO;
import at.ac.tuwien.ifs.es.middleware.controller.meta.TimeMetadata;
import at.ac.tuwien.ifs.es.middleware.service.exploration.exception.ExplorationFlowServiceExecutionException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.request.DynamicExplorationFlowRequest;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.status.OperatorInfo;
import at.ac.tuwien.ifs.es.middleware.service.exploration.status.OperatorStatusService;
import at.ac.tuwien.ifs.es.middleware.service.exploration.status.SimpleOperatorStatusService;
import at.ac.tuwien.ifs.es.middleware.service.exploration.factory.DynamicExplorationFlowFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This {@link RestController} handles all incoming requests for the exploratory search services.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/explore")
@Api(value = "Exploratory Search Endpoint",
    description = "Operations to explore the managed knowledge graph.")
public class ExploratorySearchController {

  private static final Logger logger = LoggerFactory.getLogger(ExploratorySearchController.class);

  private DynamicExplorationFlowFactory dynamicExplorationFlowFactory;
  private OperatorStatusService operatorStatusService;

  private ObjectMapper payloadMapper;

  @Autowired
  public ExploratorySearchController(
      DynamicExplorationFlowFactory dynamicExplorationFlowFactory,
      SimpleOperatorStatusService operatorStatusService,
      ObjectMapper payloadMapper) {
    this.dynamicExplorationFlowFactory = dynamicExplorationFlowFactory;
    this.operatorStatusService = operatorStatusService;
    this.payloadMapper = payloadMapper;
  }

  @PostMapping(value = "/with/custom/flow", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Apply a custom exploration flow.")
  public ExplorationContext exploreWithCustomFlow(
      @RequestBody DynamicExplorationFlowRequest request)
      throws ExplorationFlowSpecificationException {
    Instant timestampEntered = Instant.now();
    ExplorationContext context = dynamicExplorationFlowFactory.constructFlow(request).execute();
    context.metadata().put("time", payloadMapper.valueToTree(new TimeMetadata(timestampEntered)));
    return context;
  }

  @GetMapping(value = "/operators")
  @ApiOperation(value = "Gets the provided exploration operators get this micro service.")
  @ApiResponses({
      @ApiResponse(code = 200, message = "A map with supported operators, where the key is the type get the operator.")
  })
  public Map<String, List<String>> getExplorationFlowOperatorWithUID() {
    return operatorStatusService.getExplorationFlowOperators();
  }

  @GetMapping(value = "/operators/{uid}")
  @ApiOperation(value = "Gets info about the operator with given uid.")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Operator found and information will be returned."),
      @ApiResponse(code = 404, message = "No operator with the given uid can be found.")
  })
  public ResponseEntity<OperatorInfo> getExplorationFlowOperatorWithUID(
      @ApiParam(value = "The unique id of the operator.", required = true) @PathVariable String uid) {
    Optional<OperatorInfo> operatorInfoOpt = operatorStatusService
        .getExplorationFlowOperatorInfo(uid);
    return operatorInfoOpt.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /*
   * --------- Exception Handling ----------
   */

  @ExceptionHandler(ExplorationFlowServiceExecutionException.class)
  public ResponseEntity<EFExceptionDTO> handleExplorationFlowServiceExecutionException(
      ExplorationFlowServiceExecutionException ex, HttpServletRequest request) {
    logger.error("Request '{}' accepting '{}' caused an internal exploration flow exception: {}.",
        request.getRequestURI(), request.getHeader("Accept"), ex.getMessage());
    return new ResponseEntity<>(
        new EFExceptionDTO("Could not service given exploration flow. " + ex.getMessage()),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ExplorationFlowSpecificationException.class)
  public ResponseEntity<EFExceptionDTO> handleExplorationFlowSpecificationException(
      ExplorationFlowSpecificationException ex, HttpServletRequest request) {
    logger.error(
        "Request '{}' accepting '{}' caused an exploration flow specification exception: {}.",
        request.getRequestURI(), request.getHeader("Accept"), ex.getMessage());
    return new ResponseEntity<>(
        new EFExceptionDTO("The specification of the exploration flow is invalid. " + ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

}
