package at.ac.tuwien.ifs.es.middleware.sparqlbuilder;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Literal;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.RDFTerm;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import java.util.Optional;
import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfLiteral;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfValue;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class QT {

  /**
   * Transform the given {@link Resource} to a {@link RdfResource} that can be used in the SPARQL
   * query builder.
   *
   * @param resource the {@link Resource} that shall be transformed.
   * @return {@link RdfResource} that can be used in the query.
   */
  public static RdfResource transform(Resource resource) {
    checkArgument(resource != null, "The given resource for transformation must not be null.");
    BlankNodeOrIRI value = resource.value();
    if (value instanceof IRI) {
      return Rdf.iri(((IRI) value).getIRIString());
    } else {
      return Rdf.bNode(((BlankNode) value).uniqueReference());
    }
  }

  public static RdfPredicate predicate(Resource resource) {
    checkArgument(resource != null, "The given resource for transformation must not be null.");
    BlankNodeOrIRI value = resource.value();
    if (value instanceof IRI) {
      return Rdf.iri(((IRI) value).getIRIString());
    }
    throw new IllegalArgumentException("The predicate in faceted search must not be a blank node.");
  }

  public static RdfLiteral transform(Literal literal) {
    org.apache.commons.rdf.api.Literal value = literal.value();
    Optional<String> optionalLanguageTag = value.getLanguageTag();
    if (optionalLanguageTag.isPresent()) {
      return Rdf.literalOfLanguage(value.getLexicalForm(), optionalLanguageTag.get());
    } else {
      IRI datatype = value.getDatatype();
      if (datatype != null) {
        return Rdf.literalOfType(value.getLexicalForm(), Rdf.iri(datatype.getIRIString()));
      } else {
        return Rdf.literalOf(value.getLexicalForm());
      }
    }
  }

  public static RdfValue transformTerm(RDFTerm term) {
    /*if (term instanceof Resource) {
      return transform((Resource) term);
    } else*/
      if (term instanceof Literal) {
      return transform((Literal) term);
    } else {
      throw new IllegalArgumentException("The given RDF term is unknown.");
    }
  }

}
