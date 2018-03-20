package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util;

import at.ac.tuwien.ifs.exploratorysearch.dto.exception.QueryResultFormatException;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.SelectQueryResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.IteratingTupleQueryResult;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes the {@link TupleQueryResult} of a SPARQL query executed with the RDF4J framework
 * and abstracts it to {@link SelectQueryResult}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class RDF4JSelectQueryResult implements SelectQueryResult {

  private static final Logger logger = LoggerFactory.getLogger(RDF4JSelectQueryResult.class);

  private static final List<QueryResultFormat> SELECT_QUERY_RESULT_FORMATS = Arrays
      .asList(TupleQueryResultFormat.SPARQL, TupleQueryResultFormat.JSON,
          TupleQueryResultFormat.CSV, TupleQueryResultFormat.TSV, TupleQueryResultFormat.BINARY);

  private static final String SELECT_QUERY_RESULT_FORMATS_STRING;

  static {
    SELECT_QUERY_RESULT_FORMATS_STRING = String
        .format("[%s]",
            SELECT_QUERY_RESULT_FORMATS.stream().map(f -> String.join(",", f.getMIMETypes()))
                .reduce((a, b) -> a + "," + b).orElse(""));
  }

  private List<String> bindingNames;
  private List<BindingSet> bindingSets;

  public RDF4JSelectQueryResult(List<String> bindingNames, List<BindingSet> bindingSets) {
    this.bindingNames = bindingNames;
    this.bindingSets = bindingSets;
  }

  @Override
  public byte[] transform(String format) throws QueryResultFormatException {
    Optional<QueryResultFormat> formatOptional = QueryResultFormat
        .matchMIMEType(format, SELECT_QUERY_RESULT_FORMATS);
    if (formatOptional.isPresent()) {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        QueryResults
            .report(new IteratingTupleQueryResult(bindingNames, bindingSets),
                QueryResultIO.createTupleWriter(formatOptional.get(), out));
        return out.toByteArray();
      } catch (IOException e) {
        throw new QueryResultFormatException(e);
      }
    } else {
      throw new QueryResultFormatException(
          String.format("The given format '%s' is not part of the supported ones %s.",
              format, SELECT_QUERY_RESULT_FORMATS_STRING));
    }
  }

  @Override
  public String toString() {
    return "RDF4JSelectQueryResult{" +
        "bindingNames=" + bindingNames +
        ", bindingSets=" + bindingSets +
        '}';
  }
}
