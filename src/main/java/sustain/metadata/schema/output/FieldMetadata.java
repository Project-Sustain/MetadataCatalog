package sustain.metadata.schema.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> values;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String minDate;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String maxDate;

    public FieldMetadata() {
    }

    public FieldMetadata(String name, Type type, double min, double max, List<String> values, String minDate, String maxDate) {
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

    public List<String> getValues() {
        if(values == null)
        {
            values = new ArrayList<>();
        }
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getMinDate() {
        return minDate;
    }

    public void setMinDate(String minDate) {
        this.minDate = minDate;
    }

    public String getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(String maxDate) {
        this.maxDate = maxDate;
    }
}
