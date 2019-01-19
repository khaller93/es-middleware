package at.ac.tuwien.ifs.es.middleware.sparqlbuilder;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet.Facet;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet.OrFacet;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Literal;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.RDFTerm;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class helps to build a SPARQL query for faceted search.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class FacetedSearchQueryBuilder {

  private static final Logger logger = LoggerFactory.getLogger(FacetedSearchQueryBuilder.class);

  private final Variable subjectVariable;
  private GraphPattern graphPattern;

  private FacetedSearchQueryBuilder(String subjectVariable) {
    checkArgument(subjectVariable != null && !subjectVariable.isEmpty(),
        "The variable name of the subject of interest must be specified.");
    this.subjectVariable = SparqlBuilder.var(subjectVariable);
  }

  /**
   * This method returns a {@link FacetedSearchQueryBuilder} that allows to restrict the returned
   * resul
   *
   * @param subjectVariableName the variable name for which the builder shall be constructed.
   * @return a {@link FacetedSearchQueryBuilder} that is restricting the results based on the given
   * subject.
   */
  public static FacetedSearchQueryBuilder forSubject(String subjectVariableName) {
    return new FacetedSearchQueryBuilder(subjectVariableName);
  }

  /**
   * Adds a filter to the query such that no resource will be returned that is an instance of one of
   * the given classes. The given list must not be null, and if empty, no filter will be added to
   * the query.
   *
   * @param classes a list of classes ({@link Resource}) of which all their instances shall be
   * excluded.
   */
  public void excludeInstancesOfClassResources(List<Resource> classes) {
    checkArgument(classes != null, "The list of classes to exclude must not be null.");
    if (!classes.isEmpty()) {
      GraphPattern nonExistsFilterPattern = GraphPatterns.filterNotExists(GraphPatterns.union(
          classes.stream()
              .map(c -> GraphPatterns.tp(subjectVariable, RdfPredicate.a, QT.transform(c)))
              .toArray(TriplePattern[]::new)));
      this.graphPattern = graphPattern != null ? GraphPatterns.and(nonExistsFilterPattern) :
          nonExistsFilterPattern;
    }
  }

  /**
   * Adds a filter to the query such that no resource will be returned that is an instance of one of
   * the given classes. The given list must not be null, and if empty, no filter will be added to
   * the query.
   *
   * @param classes a list of classes ({@link BlankNodeOrIRI}) of which all their instances shall be
   * excluded.
   */
  public void excludeInstancesOfClasses(List<BlankNodeOrIRI> classes) {
    checkArgument(classes != null, "The list of classes to exclude must not be null.");
    this.excludeInstancesOfClassResources(
        classes.stream().map(Resource::new).collect(Collectors.toList()));
  }

  /**
   * Adds pattern to the query such that a resource can only be in the result set, if it is an
   * instance of at least one of the given classes.
   *
   * @param classes a list of classes ({@link Resource}) of which all their instances shall
   * potentially included.
   */
  public void includeInstancesOfClassResources(List<Resource> classes) {
    checkArgument(classes != null, "The list of classes to include must not be null.");
    if (!classes.isEmpty()) {
      GraphPattern pattern = GraphPatterns.union(classes.stream()
          .map(c -> GraphPatterns.tp(subjectVariable, RdfPredicate.a, QT.transform(c)))
          .toArray(TriplePattern[]::new));
      this.graphPattern = graphPattern != null ? GraphPatterns.and(pattern) : pattern;
    }
  }

  /**
   * Adds pattern to the query such that a resource can only be in the result set, if it is an
   * instance of at least one of the given classes.
   *
   * @param classes a list of classes ({@link Resource}) of which all their instances shall
   * potentially included.
   */
  public void includeInstancesOfClasses(List<BlankNodeOrIRI> classes) {
    checkArgument(classes != null, "The list of classes to include must not be null.");
    includeInstancesOfClassResources(
        classes.stream().map(Resource::new).collect(Collectors.toList()));
  }

  /**
   * Adds a {@link Facet} to the query.
   *
   * @param facet {@link Facet} that shall be added to the query.
   */
  public void addPropertyFacet(Facet facet) {
    if (facet instanceof OrFacet) {
      GraphPattern orPattern = or(((OrFacet) facet).getProperty(),
          new LinkedList<>(((OrFacet) facet).getValues()));
      graphPattern = graphPattern != null ? graphPattern.and(orPattern) : orPattern;
    } else {
      throw new IllegalArgumentException(
          String.format("The given facet type '%s' is unknown.", facet));
    }
  }

  private GraphPattern or(Resource resource, List<RDFTerm> values) {
    RdfPredicate predicate = QT.predicate(resource);
    return GraphPatterns.union(
        values.stream().map(v -> GraphPatterns.tp(subjectVariable, predicate, QT.transformTerm(v)))
            .toArray(TriplePattern[]::new));
  }

  /**
   * Get the constructed query body as a string.
   *
   * @return the constructed query body as a string.
   */
  public String getQueryBody() {
    return graphPattern != null ? graphPattern.getQueryString() : "";
  }

}
