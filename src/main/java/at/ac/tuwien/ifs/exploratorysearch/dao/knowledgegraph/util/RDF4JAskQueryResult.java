package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util;

import at.ac.tuwien.ifs.exploratorysearch.dto.exception.QueryResultFormatException;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.AskQueryResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;

/**
 * This class is an implementation of {@link AskQueryResult} that maintains the response of a ASK
 * SPARQL query. This is either {@code true}, or {@code false}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class RDF4JAskQueryResult implements AskQueryResult {

  private static final List<QueryResultFormat> ASK_QUERY_RESULT_FORMATS = Arrays
      .asList(BooleanQueryResultFormat.SPARQL, BooleanQueryResultFormat.JSON,
          BooleanQueryResultFormat.TEXT);

  private static final String ASK_QUERY_RESULT_FORMATS_STRING;

  static {
    ASK_QUERY_RESULT_FORMATS_STRING = String
        .format("[%s]",
            ASK_QUERY_RESULT_FORMATS.stream().map(f -> String.join(",", f.getMIMETypes()))
                .reduce((a, b) -> a + "," + b).orElse(""));
  }

  private boolean value;

  public RDF4JAskQueryResult(boolean value) {
    this.value = value;
  }

  /**
   * Returns the responded value of an ASK SPARQL query. This is either {@code true}, or {@code
   * false}.
   *
   * @return the responded value of an ASK SPARQL query.
   */
  public boolean value() {
    return value;
  }

  @Override
  public byte[] transform(String format) throws QueryResultFormatException {
    Optional<QueryResultFormat> formatOptional = QueryResultFormat
        .matchMIMEType(format, ASK_QUERY_RESULT_FORMATS);
    if (formatOptional.isPresent()) {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        QueryResultIO.createBooleanWriter(formatOptional.get(), out).handleBoolean(value);
        return out.toByteArray();
      } catch (IOException e) {
        throw new QueryResultFormatException(e);
      }
    } else {
      throw new QueryResultFormatException(
          String.format("The given format '%s' is not part of the supported ones %s.",
              format, ASK_QUERY_RESULT_FORMATS_STRING));
    }
  }

  @Override
  public String toString() {
    return "RDF4JAskQueryResult{" + value + "}";
  }
}
