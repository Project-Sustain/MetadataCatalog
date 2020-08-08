package sustain.metadata.schema;

import org.bson.Document;
import sustain.metadata.Constants;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.input.Types;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.schema.output.FieldMetadata;
import sustain.metadata.schema.output.Type;
import java.sql.Date;
import java.util.List;

/**
 * Created by laksheenmendis on 8/3/20 at 10:33 PM
 */
public class Mapper {

    public static void mapNumericMetaInfo(CollectionMetaData collectionMetaData, Document resultDoc, FieldInfo fieldInfo) {

        FieldMetadata fieldMetadata = new FieldMetadata();
        fieldMetadata.setName(fieldInfo.getId().getKey());
        fieldMetadata.setType(getType(fieldInfo.getValue().getTypes()));
        fieldMetadata.setMax((Double) resultDoc.get(Constants.MAXIMUM_NUMBER));
        fieldMetadata.setMin((Double) resultDoc.get(Constants.MINIMUM_NUMBER));

        collectionMetaData.getFieldMetadata().add(fieldMetadata);
    }

    public static void mapCategoricalMetaInfo(CollectionMetaData collectionMetaData, List<Object> distinctCategories, FieldInfo fieldInfo)
    {
        FieldMetadata fieldMetadata = new FieldMetadata();
        fieldMetadata.setName(fieldInfo.getId().getKey());
        fieldMetadata.setType(getType(fieldInfo.getValue().getTypes()));
        fieldMetadata.setValues(distinctCategories);

        collectionMetaData.getFieldMetadata().add(fieldMetadata);
    }

    public static void mapTemporalMetaInfo(CollectionMetaData collectionMetaData, Document resultDoc, FieldInfo fieldInfo)
    {
        FieldMetadata fieldMetadata = new FieldMetadata();
        fieldMetadata.setName(fieldInfo.getId().getKey());
        fieldMetadata.setType(getType(fieldInfo.getValue().getTypes()));

        long max = ((java.util.Date) resultDoc.get(Constants.MAXIMUM_NUMBER)).getTime();
        long min = ((java.util.Date) resultDoc.get(Constants.MINIMUM_NUMBER)).getTime();
        fieldMetadata.setMaxDate(new Date(max).toString());
        fieldMetadata.setMinDate(new Date(min).toString());

        collectionMetaData.getFieldMetadata().add(fieldMetadata);
    }

    private static Type getType(Types type)
    {
        if(type.getNumber() != null)
        {
            return Type.NUMBER;
        }
        else if(type.getObjectId() != null)
        {
            return Type.OBJECTID;
        }
        else if(type.getArray() != null)
        {
            return Type.ARRAY;
        }
        else if(type.getDate() != null)
        {
            return Type.DATE;
        }
        else
        {
            return Type.STRING;
        }
    }
}
