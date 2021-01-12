package sustain.metadata.task;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.javatuples.Pair;
import sustain.metadata.mongodb.QueryRunner;
import sustain.metadata.schema.Mapper;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sustain.metadata.Constants.ARRAY_TYPE_CHECK_DOCUMENT_LIMIT;

/**
 * Created by laksheenmendis on 1/10/21 at 1:39 AM
 */
public class FieldTypeTask {

    private QueryRunner queryRunner = new QueryRunner();

    public void getAndMapNumericTypes(String collectionName, String fieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo)
    {
        boolean categorical = false;
        try {
            //these integer fields should be considered as categorical
            if(PropertyLoader.getSpecialNumericFields().contains(fieldName))
            {
                List<Object> distinctIntegerValues = queryRunner.getDistinctIntegerValues(collectionName, fieldName);
                if(distinctIntegerValues != null)
                {
                    Mapper.mapCategoricalMetaInfo(collectionMetaData, distinctIntegerValues, fieldInfo);
                    categorical = true;
                }
            }

            if(!categorical)
            {
                Document resultDoc = queryRunner.getMinMax(collectionName, fieldName);
                Mapper.mapNumericMetaInfo(collectionMetaData, resultDoc, fieldInfo);
            }

        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void getAndMapDateField(String collectionName, String fieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo, boolean isRealDateField)
    {
        Document resultDoc;
        if(isRealDateField)
        {
            resultDoc = queryRunner.getMinMax(collectionName, fieldName);
        }
        else
        {
            resultDoc = queryRunner.getMinMaxDate(collectionName, fieldName);
        }

        if(resultDoc != null)
        {
            Mapper.mapTemporalMetaInfo(collectionMetaData, resultDoc, fieldInfo);
        }
    }

    public void getAndMapEpochTime(String collectionName, String fieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo)
    {
        Document resultDoc = queryRunner.getMinMax(collectionName, fieldName);

        if(resultDoc != null)
        {
            Mapper.mapTemporalMetaInfoFromEpochTime(collectionMetaData, resultDoc, fieldInfo);
        }
    }

    private String getChildField(String collectionName, String parentName)
    {
        List<Pair<String, String>> structuredFields = PropertyLoader.getStructuredFields(collectionName);

        for(Pair<String, String> pair : structuredFields)
        {
            if(pair.getValue0().equals(parentName))
            {
                return pair.getValue1();
            }
        }
        return null;
    }

    public void getAndMapStructuredFields(String collectionName, String parentFieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo) {

        String childFieldName = getChildField(collectionName, parentFieldName);
        Map<String, List<String>> parentChildMap = new HashMap<>();

        // First we need to get all the distinct values for the parent field
        List<Object> categories = queryRunner.getDistinctCategories(collectionName, parentFieldName);

        // Next, get all the valid distinct values for the child field per each parent category
        for(Object category : categories)
        {
            String catString = (String) category;
            List<Bson> filters = new ArrayList<Bson>();
            filters.add(Filters.eq(parentFieldName, catString));

            Bson filter = Filters.and(filters);

            List<String> childCatList = queryRunner.getChildCategories(collectionName, childFieldName, filter);

            parentChildMap.put(catString, childCatList);
        }

        Mapper.mapStructureMetaInfo(collectionMetaData, parentChildMap, fieldInfo, childFieldName);

    }

    public void getAndMapStringType(String collectionName, String fieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo) {

        List<Object> distinctCategories = queryRunner.getDistinctCategories(collectionName, fieldName);
        if(distinctCategories != null)
        {
            Mapper.mapCategoricalMetaInfo(collectionMetaData, distinctCategories, fieldInfo);
        }
    }

    public String findArrayType(String collectionName, String fieldName) {

        FindIterable<Document> findIterable = queryRunner.findIterableType(collectionName, fieldName);
        MongoCursor<Document> iterator = findIterable.iterator();

        HashMap<String, Integer> typeCountMap = new HashMap<>();

        while(iterator.hasNext())
        {
            Object obj = iterator.next();
            String typeString = ((Document) obj).get(fieldName).getClass().getSimpleName().getClass().getSimpleName();

            if(typeCountMap.get(typeString) == null)
            {
                typeCountMap.put(typeString, 1);
            }
            else
            {
                typeCountMap.put(typeString, typeCountMap.get(typeString) + 1);
            }
        }

        //checks whether the HashMap has the exact number of counts as Constants.ARRAY_TYPE_CHECK_DOCUMENT_LIMIT,
        //for a particular type (ex; String, Integer, Double, etc)
        //thus we can deduce the Array consists of a particular data type
        for(String typeStr : typeCountMap.keySet())
        {
            if( typeCountMap.get(typeStr) == ARRAY_TYPE_CHECK_DOCUMENT_LIMIT)
            {
                System.out.println(typeStr);
                return typeStr;
            }
        }
        return null;
    }

}
