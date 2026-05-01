package com.sportspulse.standings.utils.deserializers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FlexibleErrorsDeserializer")
class FlexibleErrorsDeserializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  // ---------------------------------------------------------------------------
  // Helper: parses a raw JSON string directly through the deserializer
  // ---------------------------------------------------------------------------

  private List<Object> deserialize(String json) throws IOException {
    try (JsonParser parser = objectMapper.createParser(json)) {
      parser.nextToken();
      DeserializationContext ctx = objectMapper.getDeserializationContext();
      return new FlexibleErrorsDeserializer().deserialize(parser, ctx);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> asMap(Object element) {
    return (Map<String, Object>) element;
  }

  // ===========================================================================
  // JSON array input
  // ===========================================================================

  @Nested
  @DisplayName("when input is a JSON array")
  class ArrayInput {

    @Test
    @DisplayName("returns an empty list for an empty array")
    void returnsEmptyList_forEmptyArray() throws IOException {
      List<Object> result = deserialize("[]");

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("returns a single-element list for a one-item array")
    void returnsSingleElementList_forOneItemArray() throws IOException {
      List<Object> result = deserialize("[{\"requests\":\"Limit reached\"}]");

      assertThat(result).hasSize(1);
      assertThat(asMap(result.getFirst())).containsEntry("requests", "Limit reached");
    }

    @Test
    @DisplayName("returns all elements for a multi-item array")
    void returnsAllElements_forMultiItemArray() throws IOException {
      List<Object> result = deserialize("[{\"key1\":\"val1\"},{\"key2\":\"val2\"}]");

      assertThat(result).hasSize(2);
      assertThat(asMap(result.get(0))).containsEntry("key1", "val1");
      assertThat(asMap(result.get(1))).containsEntry("key2", "val2");
    }

    @Test
    @DisplayName("preserves all key-value pairs inside array elements")
    void preservesKeyValuePairs_insideArrayElements() throws IOException {
      List<Object> result = deserialize("[{\"plan\":\"Your plan limit has been reached.\"}]");

      assertThat(result).hasSize(1);
      assertThat(asMap(result.getFirst()))
          .containsEntry("plan", "Your plan limit has been reached.");
    }
  }

  // ===========================================================================
  // JSON object input
  // ===========================================================================

  @Nested
  @DisplayName("when input is a JSON object")
  class ObjectInput {

    @Test
    @DisplayName("wraps a single object into a one-element list")
    void wrapsSingleObject_intoOneElementList() throws IOException {
      List<Object> result = deserialize("{\"requests\":\"Limit reached\"}");

      assertThat(result).hasSize(1);
      assertThat(asMap(result.getFirst())).containsEntry("requests", "Limit reached");
    }

    @Test
    @DisplayName("wraps an object with multiple keys into a one-element list")
    void wrapsObjectWithMultipleKeys_intoOneElementList() throws IOException {
      List<Object> result = deserialize("{\"plan\":\"exceeded\",\"requests\":\"0\"}");

      assertThat(result).hasSize(1);
      assertThat(asMap(result.getFirst()))
          .containsEntry("plan", "exceeded")
          .containsEntry("requests", "0");
    }

    @Test
    @DisplayName("wraps an empty object into a one-element list containing an empty map")
    void wrapsEmptyObject_intoOneElementListWithEmptyMap() throws IOException {
      List<Object> result = deserialize("{}");

      assertThat(result).hasSize(1);
      assertThat(asMap(result.getFirst())).isEmpty();
    }
  }

  // ===========================================================================
  // Other / unexpected token input
  // ===========================================================================

  @Nested
  @DisplayName("when input is neither an array nor an object")
  class OtherInput {

    @Test
    @DisplayName("returns an empty list for a JSON null value")
    void returnsEmptyList_forNullValue() throws IOException {
      List<Object> result = deserialize("null");

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("returns an empty list for a plain string value")
    void returnsEmptyList_forStringValue() throws IOException {
      List<Object> result = deserialize("\"some error string\"");

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("returns an empty list for a numeric value")
    void returnsEmptyList_forNumericValue() throws IOException {
      List<Object> result = deserialize("429");

      assertThat(result).isEmpty();
    }
  }

  // ===========================================================================
  // Integration: via ObjectMapper field deserialization
  // ===========================================================================

  @Nested
  @DisplayName("integrated via ObjectMapper")
  class IntegrationViaObjectMapper {

    record Wrapper(
        @JsonDeserialize(using = FlexibleErrorsDeserializer.class) List<Object> errors) {}

    @Test
    @DisplayName("deserializes an array field correctly")
    void deserializesArrayField_correctly() throws IOException {
      String json = "{\"errors\":[{\"requests\":\"exhausted\"}]}";

      Wrapper wrapper = objectMapper.readValue(json, Wrapper.class);

      assertThat(wrapper.errors()).hasSize(1);
      assertThat(asMap(wrapper.errors().getFirst())).containsEntry("requests", "exhausted");
    }

    @Test
    @DisplayName("deserializes an object field by wrapping it in a list")
    void deserializesObjectField_byWrappingInList() throws IOException {
      String json = "{\"errors\":{\"plan\":\"Limit reached\"}}";

      Wrapper wrapper = objectMapper.readValue(json, Wrapper.class);

      assertThat(wrapper.errors()).hasSize(1);
      assertThat(asMap(wrapper.errors().getFirst())).containsEntry("plan", "Limit reached");
    }

    @Test
    @DisplayName("deserializes an empty array field to an empty list")
    void deserializesEmptyArrayField_toEmptyList() throws IOException {
      String json = "{\"errors\":[]}";

      Wrapper wrapper = objectMapper.readValue(json, Wrapper.class);

      assertThat(wrapper.errors()).isEmpty();
    }
  }
}
