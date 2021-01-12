package sustain.metadata.schema;

import org.bson.Document;
import sustain.metadata.Constants;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.input.Types;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.schema.output.FieldMetadata;
import sustain.metadata.schema.output.Type;
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

        //sets maximum
        setMaxValue(fieldMetadata, resultDoc.get(Constants.MAXIMUM_NUMBER));
        //sets minimum
        setMinValue(fieldMetadata, resultDoc.get(Constants.MINIMUM_NUMBER));

        collectionMetaData.getFieldMetadata().add(fieldMetadata);
    }

    private static void setMaxValue(FieldMetadata fieldMetadata, Object value)
    {
        if(value instanceof Integer)
        {
            fieldMetadata.setMax(((Integer) value).doubleValue());
        }
        else if(value instanceof String)
        {
            fieldMetadata.setMax(Double.parseDouble((String) value));
        }
        else if(value instanceof Long)
        {
            fieldMetadata.setMax(((Long) value).doubleValue());
        }
        else if(value instanceof Float)
        {
            fieldMetadata.setMax(((Float) value).doubleValue());
        }
        else {
            fieldMetadata.setMax(((Double) value));
        }
    }

    private static void setMinValue(FieldMetadata fieldMetadata, Object value)
    {
        if(value instanceof Integer)
        {
            fieldMetadata.setMin(((Integer) value).doubleValue());
        }
        else if(value instanceof String)
        {
            fieldMetadata.setMin(Double.parseDouble((String) value));
        }
        else if(value instanceof Long)
        {
            fieldMetadata.setMin(((Long) value).doubleValue());
        }
        else if(value instanceof Float)
        {
            fieldMetadata.setMin(((Float) value).doubleValue());
        }
        else
        {
            fieldMetadata.setMin(((Double) value));
        }
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

    public static void mapTemporalMetaInfoFromEpochTime(CollectionMetaData collectionMetaData, Document resultDoc, FieldInfo fieldInfo)
    {
        FieldMetadata fieldMetadata = new FieldMetadata();
        fieldMetadata.setName(fieldInfo.getId().getKey());
        fieldMetadata.setType(getType(fieldInfo.getValue().getTypes()));

        long max = ((org.bson.types.Decimal128) resultDoc.get(Constants.MAXIMUM_NUMBER)).longValue();
        long min = ((org.bson.types.Decimal128) resultDoc.get(Constants.MINIMUM_NUMBER)).longValue();
        fieldMetadata.setMaxDate(max);
        fieldMetadata.setMinDate(min);

        collectionMetaData.getFieldMetadata().add(fieldMetadata);
    }

    private static Type getType(Types type)
    {
        Type returnType;
        if(type.getNumber() != null)
        {
            returnType = Type.NUMBER;
        }
        else if(type.getObjectId() != null)
        {
            returnType = Type.OBJECTID;
        }
        else if(type.getArray() != null)
        {
            returnType = Type.ARRAY;
        }
        else if(type.getDate() != null)
        {
            returnType = Type.DATE;
        }
        else if(type.getObject() != null)
        {
            returnType = Type.OBJECT;
        }
        else
        {
            returnType = Type.STRING;
        }
        return returnType;
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
