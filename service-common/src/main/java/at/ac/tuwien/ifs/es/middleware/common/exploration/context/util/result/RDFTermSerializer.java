package at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.RDFLiteral;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.RDFTerm;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

/**
 * This class is a custom {@link JsonSerializer} for {@link RDFTerm} superclass. It delegates the
 * control to the corresponding serializer functions (Resource, Literal).
 */
@JsonComponent
public class RDFTermSerializer extends JsonSerializer<RDFTerm> {

  @Override
  public void serialize(RDFTerm value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    if (value == null) {
      gen.writeNull();
    } else {
      if (value instanceof Resource) {
        RDFTermJsonComponent.writeResource((Resource) value, gen);
      } else if (value instanceof RDFLiteral) {
        RDFTermJsonComponent.writeLiteral((RDFLiteral) value, gen);
      } else {
        throw new IllegalArgumentException(
            String.format("Given RDFTerm subclass '%s' is unknown.",
                value.getClass().getSimpleName()));
      }
    }
  }
}
