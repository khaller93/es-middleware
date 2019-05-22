package at.ac.tuwien.ifs.es.middleware.service.exploration.context.facet;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * A facet 
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(use = Id.NAME, property = "@type")
public interface Facet {

}
