# This is the python script which gets executed via the Java program to analyze a particular collection in MongoDB
# For analyzing multiple collections this needs to be executed in a shell, with relevant arguments as below
#
# python3 script.py <MongoDBName> <MongoHostName> <MongoPort>

import os
import sys

#print(sys.argv[1])
os.system("variety " + sys.argv[1] + " --host " + sys.argv[2] + " --port " + sys.argv[3] + " --outputFormat=\'json\'" + " --quiet")