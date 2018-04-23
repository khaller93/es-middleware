package at.ac.tuwien.ifs.es.middleware.controller;

import at.ac.tuwien.ifs.es.middleware.dto.heartbeat.Beat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This {@link RestController} provides methods to check the heart beat of this middleware.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/heartbeat")
@Api(value = "Heart beat",
    description = "Methods to check the heart beat of this middleware")
public class HeartBeatController {

  @GetMapping(value = "")
  @ApiOperation(value = "Requests a short response from this middleware.")
  @ApiResponses({
      @ApiResponse(code = 200, message = "A short response from this middleware.")
  })
  public Beat beat() {
    return new Beat("ESM", "1.0.0");
  }

}
