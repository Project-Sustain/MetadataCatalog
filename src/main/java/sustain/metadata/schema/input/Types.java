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
 * Created by laksheenmendis on 8/3/20 at 11:58 AM
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ObjectId",
        "String",
        "Array",
        "Number",
        "Date",
        "Object"
})
public class Types {
    @JsonProperty("ObjectId")
    private Long objectId;
    @JsonProperty("String")
    private Long string;
    @JsonProperty("Array")
    private Long array;
    @JsonProperty("Number")
    private Long number;
    @JsonProperty("Date")
    private Long date;
    @JsonProperty("Object")
    private Object object;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Types() {
    }

    /**
     *
     * @param objectId
     * @param string
     * @param array
     * @param number
     * @param date
     */
    public Types(Long objectId, Long string, Long array, Long number, Long date) {
        this.objectId = objectId;
        this.string = string;
        this.array = array;
        this.number = number;
        this.date = date;
    }

    @JsonProperty("ObjectId")
    public Long getObjectId() {
        return objectId;
    }

    @JsonProperty("ObjectId")
    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @JsonProperty("String")
    public Long getString() {
        return string;
    }

    @JsonProperty("String")
    public void setString(Long string) {
        this.string = string;
    }

    @JsonProperty("Array")
    public Long getArray() {
        return array;
    }

    @JsonProperty("Array")
    public void setArray(Long array) {
        this.array = array;
    }

    @JsonProperty("Number")
    public Long getNumber() {
        return number;
    }

    @JsonProperty("Number")
    public void setNumber(Long number) {
        this.number = number;
    }

    @JsonProperty("Date")
    public Long getDate() {
        return date;
    }

    @JsonProperty("Date")
    public void setDate(Long date) {
        this.date = date;
    }

    @JsonProperty("Object")
    public Object getObject() {
        return object;
    }

    @JsonProperty("Object")
    public void setObject(Object object) {
        this.object = object;
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
