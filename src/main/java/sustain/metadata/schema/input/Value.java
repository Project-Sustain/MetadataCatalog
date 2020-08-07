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
 * Created by laksheenmendis on 8/3/20 at 11:59 AM
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "types"
})
public class Value {
    @JsonProperty("types")
    private Types types;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Value() {
    }

    /**
     *
     * @param types
     */
    public Value(Types types) {
        super();
        this.types = types;
    }

    @JsonProperty("types")
    public Types getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(Types types) {
        this.types = types;
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
