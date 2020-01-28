package at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.RDFLiteral;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.RDFValueTerm;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class RDFTermJsonUtil {

  private static RDF valueFactory = new SimpleRDF();


  /**
   * Parses the given value and returns the corresponding {@link BlankNodeOrIRI}.
   *
   * @param value for which the corresponding {@link BlankNodeOrIRI} shall be returned.
   * @return {@link BlankNodeOrIRI} for the given {@code value}
   * @throws IllegalArgumentException if the given {@code value} is not a valid {@link IRI} or
   * {@link BlankNode}.
   */
  public static BlankNodeOrIRI valueOf(String value) throws IllegalArgumentException {
    if (value.startsWith("_:")) {
      return valueFactory.createBlankNode(value.replace("_:", ""));
    } else {
      return valueFactory.createIRI(value);
    }
  }

  /**
   * This method takes a look at the given {@link BlankNodeOrIRI} and returns a valid string of this
   * value for a subject, predicate or object in a SPARQL query.
   *
   * @param value which shall be transformed in a valid string for a SPARQL query.
   * @return a valid string representation of the given {@link BlankNodeOrIRI} for a SPARQL query
   * (subject, predicate, or object).
   */
  public static String stringForSPARQLResourceOf(BlankNodeOrIRI value) {
    if (value instanceof IRI) {
      return "<" + ((IRI) value).getIRIString() + ">";
    } else {
      return "<_:" + ((BlankNode) value).uniqueReference() + ">";
    }
  }

  /**
   * This method takes a look at the given {@link Resource} and returns a valid string of this value
   * for a subject, predicate or object in a SPARQL query.
   *
   * @param resource which shall be transformed in a valid string for a SPARQL query.
   * @return a valid string representation of the given {@link BlankNodeOrIRI} for a SPARQL query
   * (subject, predicate, or object).
   */
  public static String stringForSPARQLResourceOf(Resource resource) {
    BlankNodeOrIRI value = resource.value();
    if (value instanceof IRI) {
      return "<" + ((IRI) value).getIRIString() + ">";
    } else {
      return "<_:" + ((BlankNode) value).uniqueReference() + ">";
    }
  }

  public static String stringForSPARQLLiteralOf(RDFLiteral literal) {
    return literal.value().ntriplesString();
  }

  public static String stringForSPARQLRDFTermOf(RDFValueTerm term) {
    if (term instanceof Resource) {
      return stringForSPARQLResourceOf((Resource) term);
    } else if (term instanceof RDFLiteral) {
      return stringForSPARQLLiteralOf((RDFLiteral) term);
    } else {
      throw new IllegalArgumentException(String
          .format("Could not transform given value (%s) into SPARQL string.",
              term.getClass().getName()));
    }
  }

  public static String stringValue(BlankNodeOrIRI resource) {
    if (resource instanceof IRI) {
      return ((IRI) resource).getIRIString();
    } else {
      return "_:" + ((BlankNode) resource).uniqueReference();
    }
  }

}
