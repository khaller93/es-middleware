package at.ac.tuwien.ifs.es.middleware.sparqlbuilder.facet;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.ExcludeInstancesOfFacetFilter;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.FacetFilter;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.OneOfValuesFacetFilter;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.QT;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

/**
 * This class helps to build a SPARQL query for faceted search.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class FacetedSearchQueryBuilder {

  private final Variable subjectVariable;

  private final List<GraphPattern> existsPatterns = new LinkedList<>();
  private final List<GraphPattern> notExistsPatterns = new LinkedList<>();

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
   * @param classes a list of classes ({@link BlankNodeOrIRI}) of which all their instances shall be
   * excluded.
   */
  public void excludeInstancesOfClassResources(List<BlankNodeOrIRI> classes) {
    checkArgument(classes != null, "The list of classes to exclude must not be null.");
    if (!classes.isEmpty()) {
      notExistsPatterns.add(GraphPatterns.union(
          classes.stream()
              .map(c -> GraphPatterns.tp(subjectVariable, RdfPredicate.a, QT.value(c)))
              .toArray(TriplePattern[]::new)));
    }
  }

  /**
   * Adds pattern to the query such that a resource can only be in the result set, if it is an
   * instance of at least one of the given classes.
   *
   * @param classes a list of classes ({@link BlankNodeOrIRI}) of which all their instances shall
   * potentially included.
   */
  public void includeInstancesOfClassResources(List<BlankNodeOrIRI> classes) {
    checkArgument(classes != null, "The list of classes to include must not be null.");
    if (!classes.isEmpty()) {
      existsPatterns.add(GraphPatterns.union(classes.stream()
          .map(c -> GraphPatterns.tp(subjectVariable, RdfPredicate.a, QT.value(c)))
          .toArray(TriplePattern[]::new)));
    }
  }

  /**
   * Adds pattern to the query such that a resource can only be in the result set, if it is an
   * instance of at least one of the given classes.
   *
   * @param classes a list of classes ({@link BlankNodeOrIRI}) of which all their instances shall
   * potentially included.
   */
  public void includeInstancesOfClasses(List<BlankNodeOrIRI> classes) {
    checkArgument(classes != null, "The list of classes to include must not be null.");
    includeInstancesOfClassResources(classes);
  }

  /*
   * Adds a {@link FacetFilter} to the query.
   *
   * @param facet {@link FacetFilter} that shall be added to the query.
   */
  public void addPropertyFacet(FacetFilter facetFilter) {
    if (facetFilter instanceof OneOfValuesFacetFilter) {
      existsPatterns.add(or(((OneOfValuesFacetFilter) facetFilter).getProperty(),
          new LinkedList<>(((OneOfValuesFacetFilter) facetFilter).getValues())));
    } else if (facetFilter instanceof ExcludeInstancesOfFacetFilter) {
      excludeInstancesOfClassResources(
          ((ExcludeInstancesOfFacetFilter) facetFilter).getClasses().stream().map(Resource::value)
              .collect(Collectors.toList()));
    } else {
      throw new IllegalArgumentException(
          String.format("The given facet type '%s' is unknown.", facetFilter));
    }
  }

  private GraphPattern or(IRI resource, List<RDFTerm> values) {
    RdfPredicate predicate = QT.predicate(resource);
    return GraphPatterns.union(
        values.stream().map(v -> GraphPatterns.tp(subjectVariable, predicate, QT.value(v)))
            .toArray(TriplePattern[]::new));
  }

  /**
   * Builds the {@link GraphPattern} that can then be added to a SPARQL query.
   *
   * @return the constructed graph pattern.
   */
  public GraphPattern build() {
    GraphPattern filterExists = null;
    GraphPattern filterNotExists = null;
    if (!existsPatterns.isEmpty()) {
      filterExists = GraphPatterns
          .filterExists(existsPatterns.toArray(new GraphPattern[0]));
    }
    if (!notExistsPatterns.isEmpty()) {
      filterNotExists = GraphPatterns
          .filterNotExists(notExistsPatterns.toArray(new GraphPattern[0]));
    }
    if (filterExists == null && filterNotExists == null) {
      return null;
    } else if (filterExists != null && filterNotExists != null) {
      return GraphPatterns.and(filterExists, filterNotExists);
    } else if (filterExists != null) {
      return filterExists;
    } else {
      return filterNotExists;
    }
  }

  /**
   * Get the constructed query body as a string.
   *
   * @return the constructed query body as a string.
   */
  public String getQueryBody() {
    GraphPattern pattern = build();
    return pattern != null ? pattern.getQueryString() : "";
  }

}
