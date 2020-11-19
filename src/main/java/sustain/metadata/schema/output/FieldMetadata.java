package sustain.metadata.schema.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laksheenmendis on 8/3/20 at 10:06 PM
 */
public class FieldMetadata {

    private String name;
    private Type type;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private double min;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private double max;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Object> values;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Long minDate;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Long maxDate;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Map<String, List<String>> map;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String childName;

    public FieldMetadata() {
    }

    public FieldMetadata(String name, Type type, double min, double max, List<Object> values, long minDate, long maxDate) {
        this.name = name;
        this.type = type;
        this.min = min;
        this.max = max;
        this.values = values;
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public List<Object> getValues() {
        if(values == null)
        {
            values = new ArrayList<>();
        }
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public Long getMinDate() {
        return minDate;
    }

    public void setMinDate(Long minDate) {
        this.minDate = minDate;
    }

    public Long getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Long maxDate) {
        this.maxDate = maxDate;
    }

    public Map<String, List<String>> getMap() {
        if(map == null)
        {
            map = new HashMap<>();
        }
        return map;
    }

    public void setMap(Map<String, List<String>> map) {
        this.map = map;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }
}
