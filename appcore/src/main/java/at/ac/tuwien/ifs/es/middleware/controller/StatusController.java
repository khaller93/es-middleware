package at.ac.tuwien.ifs.es.middleware.controller;

import at.ac.tuwien.ifs.es.middleware.SystemInfo;
import at.ac.tuwien.ifs.es.middleware.dto.status.Beat;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.service.systemstatus.BackendObserverService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This {@link RestController} provides methods to check the status beat get this middleware.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/")
@Api(value = "Heart beat", description = "Methods to check the status get this middleware")
public class StatusController {

  private BackendObserverService backendObserverService;

  @Autowired
  public StatusController(BackendObserverService backendObserverService) {
    this.backendObserverService = backendObserverService;
  }

  @GetMapping(value = "/heartbeat")
  @ApiOperation(value = "Requests a short response from this microservice.")
  @ApiResponses({
      @ApiResponse(code = 200, message = "A short response from this middleware including meta information.")
  })
  public Beat beat() {
    return Beat.ok(SystemInfo.MIDDLEWARE_NAME, SystemInfo.MIDDLEWARE_VERSION);
  }


  @GetMapping(value = "/health/backend")
  @ApiOperation(value = "Gets the health get the backend service (SPARQL, Full-Text-Search, Gremlin).")
  @ApiResponses({
      @ApiResponse(code = 200, message = "")
  })
  public Map<String, KGDAOStatus> getHealthOfBackend() {
    return backendObserverService.getBackendServiceStatusMap();
  }

}
