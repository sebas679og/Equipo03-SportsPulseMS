package com.sportspulse.leagues.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * FlexibleErrorsDeserializer Custom Jackson deserializer that handles flexible error response
 * formats.
 *
 * <p>Supports deserialization of both arrays and single objects into a {@link List} of {@link
 * Object}. If the JSON token starts with an array, the entire array is mapped to a list. If it
 * starts with an object, the object is wrapped into a single-element list. For any other token, an
 * empty list is returned.
 */
public class FlexibleErrorsDeserializer extends JsonDeserializer<List<Object>> {

  @Override
  public List<Object> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
    if (p.currentToken() == JsonToken.START_ARRAY) {
      return p.readValueAs(new TypeReference<List<Object>>() {});
    } else if (p.currentToken() == JsonToken.START_OBJECT) {
      Map<String, Object> errorMap = p.readValueAs(new TypeReference<Map<String, Object>>() {});
      return List.of(errorMap);
    }
    return List.of();
  }
}
