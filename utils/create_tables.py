__author__ = 'dennizhu'
import sqlite3

db_connect = sqlite3.connect('content.db')
c = db_connect.cursor()

# Create table
c.execute(
    "CREATE TABLE IF NOT EXISTS poi(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
    "name TEXT, type INTEGER, latitude REAL, longitude REAL, content TEXT, "
    "picture BLOB, lastupdated INTEGER, notes TEXT)")
c.execute(
    "CREATE TABLE IF NOT EXISTS schools(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
    "name TEXT, center_latitude REAL, center_longitude REAL)")
db_connect.commit()
db_connect.close()