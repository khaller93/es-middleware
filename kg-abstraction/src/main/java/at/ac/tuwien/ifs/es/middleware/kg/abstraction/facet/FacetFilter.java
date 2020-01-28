package at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Using a facet of resources to filter them based on given values or a range of values for the
 * corresponding facet.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(use = Id.NAME, property = "@type")
public interface FacetFilter {

}
