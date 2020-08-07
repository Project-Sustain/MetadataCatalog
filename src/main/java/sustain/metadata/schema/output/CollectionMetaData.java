package sustain.metadata.schema.output;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laksheenmendis on 8/3/20 at 10:05 PM
 */
public class CollectionMetaData {

    private String collection;
    List<FieldMetadata> fieldMetadata;

    public CollectionMetaData(String collection) {
        this.collection = collection;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public List<FieldMetadata> getFieldMetadata() {
        if(fieldMetadata == null)
        {
            fieldMetadata = new ArrayList<>();
        }
        return fieldMetadata;
    }

    public void setFieldMetadata(List<FieldMetadata> fieldMetadata) {
        this.fieldMetadata = fieldMetadata;
    }
}
