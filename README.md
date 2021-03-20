# MetadataCatalog

The purpose of this project is to extract metadata from MongoDB collections.
This metadata will be used in populating UI components in the data explorer.

For instance, if the field is numeric, it will extract minimum and maximum values.
If the field is a String, it will extract all the distinct values which exist for that particular string.

The final output of this will be a json file. It contains an array of elements. 
Each element represents a collection in the MongoDB.

## Methodology

This is built using an open source tool named; [**variety-cli**](https://github.com/variety/variety-cli?fbclid=IwAR2vZMmAgI_Uq6kPiaplbHs3jQraJOYEXA2NpoxnRGR1sOStSkAd13Y7h64). It is an analyzing tool for MongoDB.
Source code can be found [here](https://github.com/variety/variety-cli?fbclid=IwAR2vZMmAgI_Uq6kPiaplbHs3jQraJOYEXA2NpoxnRGR1sOStSkAd13Y7h64). 
However, there's another tool named [**variety**](https://github.com/variety/variety). It uses a mongo command, and it is
impossible to execute mongo commands via Java programs, using ProcessBuilder or Runtime classes. Thus, we've chosen
**variety-cli**, and **it should be installed prior to using MetadalaCatalog**.

Apart from the above mentioned tool, MongoDB Java Driver is used to connect to MongoDB and execute various queries.

Step 1: Read **config.properties** file and get user specified configurations. (this file should contain details regarding MongoDB. Ex: database name, host name, port, etc)

Step 2: Connect to MongoDB via MongoDB Java driver and get all the names of the collections in the specified MongoDB database.

Step 3: Run variety-cli commands to analyze each collection, via a Python script (script.py) in an iterative manner. 
These commands gets executed via the Java program.(This was 
needed because even the same variety command didn't produce a json output when executed via the Java program) This command outputs details about each 
field in a collection in JSON format.

Step 4: Within each iteration of Step 3, program reads the output from each command and a separate object is created, for each field in the collection.

Step 5: Filter out the fields which have a presence of the configured "percentage.threshold" value (others are ignored). This value is configured in config.properties. If a NumberFormatException occurs, it will be set to 100%.

Step 6: Connect to MongoDB via the MongoDB Java driver, and execute relevant queries for different data types.
(Queries differ for String, Numeric, Array and Date field types)

Step 7: Form the output object (CollectionMetaData.java)

Step 8: Convert the object to JSON and write to the output file.

## How to run MetadataCatalog?

Clean and build the project using gradle. Next, create the jar by using the command "gradle jar".
Make sure "config.properties" and "script.py" files are in the same directory as the jar file.

Next, update the "config.properties" with correct details of MongoDB.

Ex:

    - mongodb.host --> mongod or mongos host name
    - mongodb.port --> port on which mongod or mongos is running
    - mongodb.db --> name of the database
    
Note: Do not change the contents of script.py

Open a terminal and navigate to the directory in which the jar resides.
Execute "java -jar <MetadataCatalog_jar_name>.jar"

## How to import the metadata into database?

Replace the placeholders of below command with appropriate values and run the command on a terminal.

mongoimport --db <db_name> --collection <collection_name> --host <mongos_host_name> -j 8 --jsonArray --file "<json_file_name>"