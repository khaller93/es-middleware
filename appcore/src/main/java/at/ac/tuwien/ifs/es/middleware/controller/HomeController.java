package at.ac.tuwien.ifs.es.middleware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This controller simply redirects from the the root URL to the human-readable swagger
 * documentation site.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Controller
public class HomeController {

  @RequestMapping("")
  public String home() {
    return "redirect:/swagger-ui.html";
  }
}
