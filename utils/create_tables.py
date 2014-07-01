__author__ = 'dennizhu'
import sqlite3
import os
import inspect
from os.path import join

current_path = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
db_connect = sqlite3.connect(join(current_path, 'content.db'))
c = db_connect.cursor()

# Create table
c.execute(
    "CREATE TABLE poi(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
    "name TEXT, type INTEGER, latitude REAL, longitude REAL, school_id INTEGER, contenttext TEXT, "
    "picture BLOB, lastupdated INTEGER, notes TEXT, "
    "FOREIGN KEY(school_id) REFERENCES schools(_id))")
c.execute(
    "CREATE TABLE schools(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
    "name TEXT, centerLatitude REAL, centerLongitude REAL)")
c.close()
db_connect.commit()
db_connect.close()
print "Tables Created Successfully"