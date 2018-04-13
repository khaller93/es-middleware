package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util;

import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLResultFormatException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

/**
 * This class is an implementation of {@link GraphQueryResult} that maintains the response of a
 * CONSTRUCT or DESCRIBE SPARQL query executed by the RDF4J framework.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class RDF4JGraphQueryResult extends RDF4JQueryResult<RDFFormat> implements
    at.ac.tuwien.ifs.es.middleware.dto.sparql.GraphQueryResult {

  private static final List<RDFFormat> GRAPH_QUERY_RESULT_FORMATS = Arrays
      .asList(RDFFormat.JSONLD, RDFFormat.TURTLE, RDFFormat.RDFXML, RDFFormat.NTRIPLES,
          RDFFormat.BINARY);

  private static final String GRAPH_QUERY_RESULT_FORMATS_STRING = RDF4JQueryResult
      .transformResultFormatsToReadableString(GRAPH_QUERY_RESULT_FORMATS);

  private Map<String, String> namespaces;
  private List<Statement> statements;

  public RDF4JGraphQueryResult(Map<String, String> namespaces, List<Statement> statements) {
    super(GRAPH_QUERY_RESULT_FORMATS, GRAPH_QUERY_RESULT_FORMATS_STRING);
    this.namespaces = namespaces;
    this.statements = statements;
  }

  @Override
  public byte[] performTransformation(RDFFormat format) throws SPARQLResultFormatException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      QueryResults.report(new IteratingGraphQueryResult(namespaces, statements),
          Rio.createWriter(format, out));
      return out.toByteArray();
    } catch (IOException e) {
      throw new SPARQLResultFormatException(e);
    }
  }

  @Override
  public Graph value() {
    return new RDF4J().asGraph(new LinkedHashModel(statements));
  }

  @Override
  public String toString() {
    return "RDF4JGraphQueryResult{" +
        "namespaces=" + namespaces +
        ", statements=" + statements +
        '}';
  }
}
