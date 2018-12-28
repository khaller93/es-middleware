package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util;

import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLResultFormatException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLResultSerializationException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.common.lang.FileFormat;

/**
 * This class implements all generic methods for {@link QueryResult} implemented with RDF4J.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class RDF4JQueryResult<T extends FileFormat> implements QueryResult {

  private List<T> supportedFormats;
  private String readableFormatsString;

  public RDF4JQueryResult(List<T> supportedFormats, String readableFormatsString) {
    this.supportedFormats = supportedFormats;
    this.readableFormatsString = readableFormatsString;
  }

  @Override
  public Optional<String> matchMimeType(List<String> mimeTypes) {
    for (String mimeType : mimeTypes) {
      Optional<T> formatOptional = FileFormat.matchMIMEType(mimeType, supportedFormats);
      if (formatOptional.isPresent()) {
        return Optional.of(formatOptional.get().getDefaultMIMEType());
      }
    }
    return Optional.empty();
  }

  /**
   * A hook for specific implementations.
   */
  public abstract byte[] performTransformation(T format) throws SPARQLResultFormatException;

  @Override
  public byte[] transform(String mimeType) throws SPARQLResultFormatException {
    Optional<T> formatOptional = FileFormat.matchMIMEType(mimeType, supportedFormats);
    return performTransformation(
        formatOptional.orElseThrow(getMimeTypeException(Collections.singletonList(mimeType))));
  }

  @Override
  public byte[] transform(List<String> mimeTypes)
      throws SPARQLResultFormatException, SPARQLResultSerializationException {
    for (String mimeType : mimeTypes) {
      Optional<T> formatOptional = FileFormat.matchMIMEType(mimeType, supportedFormats);
      if (formatOptional.isPresent()) {
        return performTransformation(formatOptional.get());
      }
    }
    throw getMimeTypeException(mimeTypes).get();
  }

  @Override
  public Supplier<SPARQLResultFormatException> getMimeTypeException(List<String> mimeTypes) {
    return () -> new SPARQLResultFormatException(
        String.format("The given format '%s' is not part of the supported ones %s.", mimeTypes,
            readableFormatsString));
  }

  /**
   * Transforms the given {@code formats} into a human-readable string.
   *
   * @param formats that shall be printed in a human-readable way.
   * @return human-readable list of the given {@code formats}.
   */
  public static String transformResultFormatsToReadableString(List<? extends FileFormat> formats) {
    return String.format("[%s]", formats.stream().map(f -> String.join(",", f.getMIMETypes()))
        .collect(Collectors.joining(",")));
  }

}
