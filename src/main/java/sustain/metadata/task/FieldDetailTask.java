package sustain.metadata.task;

import org.javatuples.Pair;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.input.Types;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;
import java.util.List;

/**
 * Created by laksheenmendis on 10/19/20 at 9:53 PM
 */
public class FieldDetailTask implements Runnable {

    private FieldInfo fieldInfo;
    private CollectionMetaData collectionMetaData;
    private String collectionName;

    public FieldDetailTask(FieldInfo fieldInfo, CollectionMetaData collectionMetaData, String collectionName) throws ValueNotFoundException{
        this.fieldInfo = fieldInfo;
        this.collectionMetaData = collectionMetaData;
        this.collectionName = collectionName;
    }

    @Override
    public void run() {
        String fieldName = fieldInfo.getId().getKey();

        try {
            if(!(PropertyLoader.getIgnoredFields().contains(fieldName) || fieldName.contains("geometry")))
            {
                // if the field is listed as a child field, no need to generate a separate metadata section
                boolean isChildField = isAStructuredField(collectionName, fieldName, true);
                boolean isParentField = isAStructuredField(collectionName, fieldName, false);

                List<String> ignoredCollectionFields = PropertyLoader.getIgnoredCollectionFields(collectionName);
                if( (!isChildField && ignoredCollectionFields != null && !ignoredCollectionFields.contains(fieldName)) || ignoredCollectionFields == null)
                {
                    processBasedOnType(isParentField);
                }
            }
        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean isAStructuredField(String collectionName, String fieldName, boolean child)
    {
        List<Pair<String, String>> structuredFields = PropertyLoader.getStructuredFields(collectionName);

        if(structuredFields != null)
        {
            for(Pair<String, String> pair : structuredFields)
            {
                if(child && pair.getValue1().equals(fieldName)) // if a child
                {
                    return true;
                }
                else if(!child && pair.getValue0().equals(fieldName)) // if a parent
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void processBasedOnType(boolean isParentField )
    {
        FieldTypeTask typeTask = new FieldTypeTask();
        String fieldName = fieldInfo.getId().getKey();
        Types type = fieldInfo.getValue().getTypes();

        System.out.println("Started processing for " + collectionName + "/" + fieldName);

        //TODO for other types of structured parent fields
        if(isParentField && type.getString() != null)
        {
            typeTask.getAndMapStructuredFields(collectionName, fieldName, collectionMetaData, fieldInfo);
        }
        else
        {
            processNonParentField(type, fieldName);
        }
    }

    private void processNonParentField(Types type, String fieldName)
    {
        FieldTypeTask typeTask = new FieldTypeTask();
        if(type.getNumber() != null )
        {
            typeTask.getAndMapNumericTypes(collectionName, fieldName, collectionMetaData, fieldInfo);
        }
        else if(type.getString() != null )
        {
            processStringType(fieldName, typeTask);
        }
        else if(type.getDate() != null)
        {
            typeTask.getAndMapDateField(collectionName, fieldName, collectionMetaData, fieldInfo, true);
        }
        else if(type.getArray() != null)
        {
            processArrayType(fieldName, typeTask);
        }
        else if(type.getObject() != null && fieldName.equals("epoch_time"))
        {
            processObjectType(fieldName, typeTask);
        }
    }

    private void processObjectType(String fieldName, FieldTypeTask typeTask) {
        // need to change the type of the field
        fieldInfo.getValue().getTypes().setObject(null);
        fieldInfo.getValue().getTypes().setDate(1L);
        typeTask.getAndMapEpochTime(collectionName, fieldName, collectionMetaData, fieldInfo);
    }

    private void processStringType(String fieldName, FieldTypeTask typeTask)
    {
        //identify date fields by key
        boolean dateField = fieldName.toLowerCase().contains("date");
        if(dateField)
        {
            // need to change the type of the field
            fieldInfo.getValue().getTypes().setString(null);
            fieldInfo.getValue().getTypes().setDate(1L);
            typeTask.getAndMapDateField(collectionName, fieldName, collectionMetaData, fieldInfo, false);
        }
        else
        {
            typeTask.getAndMapStringType(collectionName, fieldName, collectionMetaData, fieldInfo);
        }
    }

    private void processArrayType(String fieldName, FieldTypeTask typeTask)
    {
        // array could contain different types of data (Ex; String, Integer, Double, etc)
        String arrayType = typeTask.findArrayType(collectionName, fieldName);

        // extract metadata only if the array consists of a unique type
        if(arrayType != null)
        {
            if(arrayType.equals("String"))
            {
                typeTask.getAndMapStringType(collectionName, fieldName, collectionMetaData, fieldInfo);
            }
            else
            {
                typeTask.getAndMapNumericTypes(collectionName, fieldName, collectionMetaData, fieldInfo);
            }
        }
    }


}