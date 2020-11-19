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
import java.util.Map;

/**
 * Created by laksheenmendis on 8/3/20 at 10:33 PM
 */
public class Mapper {

    public static void mapNumericMetaInfo(CollectionMetaData collectionMetaData, Document resultDoc, FieldInfo fieldInfo) {

        FieldMetadata fieldMetadata = new FieldMetadata();
        fieldMetadata.setName(fieldInfo.getId().getKey());
        fieldMetadata.setType(getType(fieldInfo.getValue().getTypes()));
        if(resultDoc.get(Constants.MAXIMUM_NUMBER) instanceof Integer)
        {
            fieldMetadata.setMax(((Integer) resultDoc.get(Constants.MAXIMUM_NUMBER)).doubleValue());
        }
        else if(resultDoc.get(Constants.MAXIMUM_NUMBER) instanceof String)
        {
            fieldMetadata.setMax(Double.valueOf((String) resultDoc.get(Constants.MAXIMUM_NUMBER)));
        }
        else if(resultDoc.get(Constants.MAXIMUM_NUMBER) instanceof Long)
        {
            fieldMetadata.setMax(((Long) resultDoc.get(Constants.MAXIMUM_NUMBER)).doubleValue());
        }
        else if(resultDoc.get(Constants.MAXIMUM_NUMBER) instanceof Float)
        {
            fieldMetadata.setMax(((Float) resultDoc.get(Constants.MAXIMUM_NUMBER)).doubleValue());
        }
        else
        {
            fieldMetadata.setMax((Double) resultDoc.get(Constants.MAXIMUM_NUMBER));
        }

        if(resultDoc.get(Constants.MINIMUM_NUMBER) instanceof Integer)
        {
            fieldMetadata.setMin(((Integer)resultDoc.get(Constants.MINIMUM_NUMBER)).doubleValue());
        }
        else if(resultDoc.get(Constants.MINIMUM_NUMBER) instanceof String)
        {
            fieldMetadata.setMin(Double.valueOf((String)resultDoc.get(Constants.MINIMUM_NUMBER)));
        }
        else if(resultDoc.get(Constants.MINIMUM_NUMBER) instanceof Long)
        {
            fieldMetadata.setMin(((Long) resultDoc.get(Constants.MINIMUM_NUMBER)).doubleValue());
        }
        else if(resultDoc.get(Constants.MINIMUM_NUMBER) instanceof Float)
        {
            fieldMetadata.setMin(((Float) resultDoc.get(Constants.MINIMUM_NUMBER)).doubleValue());
        }
        else
        {
            fieldMetadata.setMin((Double) resultDoc.get(Constants.MINIMUM_NUMBER));
        }

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
        fieldMetadata.setMaxDate(max);
        fieldMetadata.setMinDate(min);

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

    public static void mapStructureMetaInfo(CollectionMetaData collectionMetaData, Map<String, List<String>> parentChildMap, FieldInfo fieldInfo, String childFieldName) {

        FieldMetadata fieldMetadata = new FieldMetadata();
        fieldMetadata.setName(fieldInfo.getId().getKey());
        fieldMetadata.setType(getType(fieldInfo.getValue().getTypes()));
        fieldMetadata.setChildName(childFieldName);
        fieldMetadata.setMap(parentChildMap);

        collectionMetaData.getFieldMetadata().add(fieldMetadata);
    }
}
