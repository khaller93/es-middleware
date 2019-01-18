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
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class FacetedSearchQueryBuilder {

  private static final Logger logger = LoggerFactory.getLogger(FacetedSearchQueryBuilder.class);

  private GraphPattern graphPattern;
  private final Variable subjectVariable;

  private FacetedSearchQueryBuilder(String subjectVariable) {
    checkArgument(subjectVariable != null && !subjectVariable.isEmpty(),
        "The variable name of the subject of interest must be specified.");
    this.subjectVariable = SparqlBuilder.var(subjectVariable);

  }

  /**
   *
   */
  public static FacetedSearchQueryBuilder forSubject(String subjectVariableName) {
    return new FacetedSearchQueryBuilder(subjectVariableName);
  }


  public static String buildFacets(String subjectVariableName, List<Facet> facets) {
    FacetedSearchQueryBuilder facetedSearchQueryBuilder = new FacetedSearchQueryBuilder(
        subjectVariableName);
    for (Facet facet : facets) {
      if (facet instanceof OrFacet) {
        facetedSearchQueryBuilder.andPropertyFacet(((OrFacet) facet).getProperty(),
            new LinkedList<RDFTerm>(((OrFacet) facet).getValues()));
      }
    }
    return facetedSearchQueryBuilder.getQueryBody();
  }


  public void andPropertyFacet(Resource resource, List<RDFTerm> values) {
    if (graphPattern == null) {
      graphPattern = GraphPatterns.and(or(resource, values));
    } else {
      graphPattern = graphPattern.and(or(resource, values));
    }
  }

  private GraphPattern or(Resource resource, List<RDFTerm> values) {
    RdfPredicate predicate = QT.predicate(resource);
    return GraphPatterns.union(
        values.stream().map(v -> GraphPatterns.tp(subjectVariable, predicate, QT.transformTerm(v)))
            .toArray(TriplePattern[]::new));
  }

  /**
   *
   */
  public String getQueryBody() {
    return graphPattern != null ? graphPattern.getQueryString() : "";
  }

  public static void main(String args[]) {
    System.out.println(
        ">>>>" + SparqlBuilder
            .where(
                GraphPatterns.union(
                    GraphPatterns.tp(Rdf.iri("a"), Rdf.iri("a"), Rdf.iri("b")),
                    GraphPatterns.tp(Rdf.iri("a"), Rdf.iri("a"), Rdf.iri("b"))
                )
            ).getQueryString());
    System.out.println(">>>>>" + FacetedSearchQueryBuilder.buildFacets("subject", Arrays.asList(
        new OrFacet(new Resource("http://test.a"),
            Arrays.asList(new Literal("a"), new Literal("b"))))));
  }

}
