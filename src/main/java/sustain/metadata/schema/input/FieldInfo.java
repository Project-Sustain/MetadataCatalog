package sustain.metadata.schema.input;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by laksheenmendis on 8/3/20 at 11:40 AM
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "_id",
        "value",
        "totalOccurrences",
        "percentContaining"
})
public class FieldInfo {

    @JsonProperty("_id")
    private Id id;
    @JsonProperty("value")
    private Value value;
    @JsonProperty("totalOccurrences")
    private Long totalOccurrences;
    @JsonProperty("percentContaining")
    private Long percentContaining;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public FieldInfo() {
    }

    /**
     *
     * @param totalOccurrences
     * @param percentContaining
     * @param id
     * @param value
     */
    public FieldInfo(Id id, Value value, Long totalOccurrences, Long percentContaining) {
        super();
        this.id = id;
        this.value = value;
        this.totalOccurrences = totalOccurrences;
        this.percentContaining = percentContaining;
    }

    @JsonProperty("_id")
    public Id getId() {
        return id;
    }

    @JsonProperty("_id")
    public void setId(Id id) {
        this.id = id;
    }

    @JsonProperty("value")
    public Value getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Value value) {
        this.value = value;
    }

    @JsonProperty("totalOccurrences")
    public Long getTotalOccurrences() {
        return totalOccurrences;
    }

    @JsonProperty("totalOccurrences")
    public void setTotalOccurrences(Long totalOccurrences) {
        this.totalOccurrences = totalOccurrences;
    }

    @JsonProperty("percentContaining")
    public Long getPercentContaining() {
        return percentContaining;
    }

    @JsonProperty("percentContaining")
    public void setPercentContaining(Long percentContaining) {
        this.percentContaining = percentContaining;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
