__author__ = 'dennizhu'
import sqlite3
import csv

import os
import inspect
from os.path import join

current_path = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
db_connect = sqlite3.connect(join(current_path, 'content.db'))
db_connect.row_factory = sqlite3.Row

csv.register_dialect('popo', delimiter='|', quoting=csv.QUOTE_MINIMAL)

with open(join(current_path, 'popo_schools.csv'), 'rb') as f:
    reader = csv.DictReader(f, dialect='popo')
    to_db = [(row['_id'], row['name'], row['centerLatitude'], row['centerLongitude']) for row in reader]

cursor = db_connect.cursor()
cursor.executemany("INSERT INTO schools (_id, name, centerLatitude, centerLongitude) VALUES (?, ?, ?, ?);", to_db)

with open(join(current_path, 'popo.csv'), 'rb') as f:
    reader = csv.DictReader(f, dialect='popo')
    to_db = [(row['name'], row['type'], row['latitude'], row['longitude'], row['school_id'], row['contenttext'],
              row['lastupdated'], row['notes']) for row in reader]

cursor.executemany(
    "INSERT INTO poi (name, type, latitude, longitude, school_id, contenttext, lastupdated, notes) "
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
    to_db)

db_connect.commit()

cursor.close()
db_connect.close()
print "Tables Populated Successfully"