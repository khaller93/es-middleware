package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util;

import at.ac.tuwien.ifs.exploratorysearch.dto.exception.QueryResultFormatException;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.GraphQueryResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
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
public class RDF4JGraphQueryResult implements GraphQueryResult {

  private static final List<RDFFormat> GRAPH_QUERY_RESULT_FORMATS = Arrays
      .asList(RDFFormat.JSONLD, RDFFormat.TURTLE, RDFFormat.RDFXML, RDFFormat.NTRIPLES,
          RDFFormat.BINARY);

  private static final String GRAPH_QUERY_RESULT_FORMATS_STRING;

  static {
    GRAPH_QUERY_RESULT_FORMATS_STRING = String
        .format("[%s]",
            GRAPH_QUERY_RESULT_FORMATS.stream().map(f -> String.join(",", f.getMIMETypes()))
                .reduce((a, b) -> a + "," + b).orElse(""));
  }

  private Map<String, String> namespaces;
  private List<Statement> statements;

  public RDF4JGraphQueryResult(Map<String, String> namespaces, List<Statement> statements) {
    this.namespaces = namespaces;
    this.statements = statements;
  }

  @Override
  public byte[] transform(String format) throws QueryResultFormatException {
    Optional<RDFFormat> formatOptional = QueryResultFormat
        .matchMIMEType(format, GRAPH_QUERY_RESULT_FORMATS);
    if (formatOptional.isPresent()) {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        QueryResults.report(new IteratingGraphQueryResult(namespaces, statements),
            Rio.createWriter(formatOptional.get(), out));
        return out.toByteArray();
      } catch (IOException e) {
        throw new QueryResultFormatException(e);
      }
    } else {
      throw new QueryResultFormatException(
          String.format("The given format '%s' is not part of the supported ones %s.",
              format, GRAPH_QUERY_RESULT_FORMATS_STRING));
    }
  }

  @Override
  public String toString() {
    return "RDF4JGraphQueryResult{" +
        "namespaces=" + namespaces +
        ", statements=" + statements +
        '}';
  }

}
