package at.ac.tuwien.ifs.exploratorysearch.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This {@link RestController} handles incoming SPARQL
 * queries and delegates them to the SPARQL service.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/sparql")
public class SPARQLController {

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public void query(@RequestParam String query){

    }

}
