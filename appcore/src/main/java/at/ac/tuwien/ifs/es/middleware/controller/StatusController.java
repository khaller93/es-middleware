package at.ac.tuwien.ifs.es.middleware.controller;

import at.ac.tuwien.ifs.es.middleware.SystemInfo;
import at.ac.tuwien.ifs.es.middleware.dto.status.BackendServiceStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.Beat;
import at.ac.tuwien.ifs.es.middleware.service.systemstatus.SystemStatusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This {@link RestController} provides methods to check the status beat of this middleware.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/status")
@Api(value = "Heart beat", description = "Methods to check the status of this middleware")
public class StatusController {

  private SystemStatusService systemStatusService;

  public StatusController(@Autowired SystemStatusService systemStatusService) {
    this.systemStatusService = systemStatusService;
  }

  @GetMapping(value = "/heartbeat")
  @ApiOperation(value = "Requests a short response from this middleware.")
  @ApiResponses({
      @ApiResponse(code = 200, message = "A short response from this middleware including meta information.")
  })
  public Beat beat() {
    return Beat.ok(SystemInfo.MIDDLEWARE_NAME, SystemInfo.MIDDLEWARE_VERSION);
  }

  @GetMapping(value = "/supported/exploration/operators")
  @ApiOperation(value = "Gets the provided exploration operators of this middleware.")
  @ApiResponses({
      @ApiResponse(code = 200, message = "A map with supported operators, where the key is the type of the operator.")
  })
  public Map<String, List<String>> getExplorationFlowOperators() {
    return systemStatusService.getExplorationFlowOperators();
  }

  @GetMapping(value = "/health/of/backend")
  @ApiOperation(value = "Gets the health of the backend service (SPARQL, Full-Text-Search, Gremlin).")
  @ApiResponses({
      @ApiResponse(code = 200, message = "")
  })
  public Map<String, BackendServiceStatus> getHealthOfBackend() {
    return systemStatusService.checkHealthOfBackend();
  }

}
