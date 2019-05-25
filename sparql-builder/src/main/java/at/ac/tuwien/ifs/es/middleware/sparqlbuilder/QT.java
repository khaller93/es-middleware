package at.ac.tuwien.ifs.es.middleware.sparqlbuilder;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfLiteral;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfValue;

/**
 * This class provides methods to transform RDF terms from Apache Commons to RDF4J SPARQL builder.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class QT {

  /**
   * Transform the given {@link BlankNodeOrIRI} to a {@link RdfResource} that can be used in the
   * SPARQL query builder.
   *
   * @param resource the {@link BlankNodeOrIRI} that shall be transformed. It must not be null.
   * @return {@link RdfResource} that can be used in the query.
   */
  public static RdfResource resource(BlankNodeOrIRI resource) {
    checkArgument(resource != null, "The given resource for transformation must not be null.");
    if (resource instanceof IRI) {
      return Rdf.iri(((IRI) resource).getIRIString());
    } else {
      return Rdf.bNode(((BlankNode) resource).uniqueReference());
    }
  }

  /**
   * Transforms the given {@link IRI} to a {@link RdfPredicate} that can be used in the SPARQL query
   * builder.
   *
   * @param resource the {@link IRI} that shall be transformed. It must not be null.
   * @return {@link RdfPredicate} that can be used in the query.
   */
  public static RdfPredicate predicate(IRI resource) {
    checkArgument(resource != null, "The given resource for transformation must not be null.");
    return Rdf.iri(resource.getIRIString());
  }

  /**
   * Transforms the given {@link RDFTerm} to a {@link RdfValue} that can be used in the SPARQL query
   * builder.
   *
   * @param term the {@link RDFTerm} that shall be transformed. It must not be null.
   * @return {@link RdfValue} that can be used in the query.
   */
  public static RdfValue value(RDFTerm term) {
    checkArgument(term != null, "The given term for transformation must not be null.");
    if (term instanceof BlankNodeOrIRI) {
      return resource((BlankNodeOrIRI) term);
    } else if (term instanceof Literal) {
      return literal(((Literal) term));
    } else {
      throw new IllegalArgumentException("The given RDF term is unknown.");
    }
  }

  /**
   * Transforms the given {@link Literal} to a {@link RdfLiteral} that can be used in the SPARQL
   * query builder.
   *
   * @param literal the {@link Literal} that shall be transformed. It must not be null.
   * @return {@link RdfLiteral} that can be used in the query.
   */
  public static RdfLiteral literal(Literal literal) {
    checkArgument(literal != null, "The given literal for transformation must not be null.");
    Optional<String> optionalLanguageTag = literal.getLanguageTag();
    if (optionalLanguageTag.isPresent()) {
      return Rdf.literalOfLanguage(literal.getLexicalForm(), optionalLanguageTag.get());
    } else {
      IRI datatype = literal.getDatatype();
      if (datatype != null) {
        return Rdf.literalOfType(literal.getLexicalForm(), Rdf.iri(datatype.getIRIString()));
      } else {
        return Rdf.literalOf(literal.getLexicalForm());
      }
    }
  }

}
