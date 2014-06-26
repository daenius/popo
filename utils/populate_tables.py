__author__ = 'dennizhu'
import sqlite3

db_connect = sqlite3.connect('content.db')
db_connect.row_factory = sqlite3.Row
db2 = sqlite3.connect('../content.db')
db2.row_factory = sqlite3.Row
c = db_connect.cursor()
c2 = db2.cursor()

# Fill up schools table
# c2.execute("SELECT * FROM schools")
# records = c2.fetchall()
# for r in records:
#     c.execute("INSERT INTO schools ( name ) VALUES (?)", (r['name'], ))
#
# c.execute("UPDATE schools SET centerLatitude = 37.872219, centerLongitude = -122.258556 WHERE name=?", ('University of California', ))

c2.execute("SELECT * FROM poi")
records = c2.fetchall()
for r in records:
    c.execute("INSERT INTO poi ( name, type, latitude, longitude, school_id, contenttext ) VALUES (?, ?, ?, ?, 1, ?)", (r['name'], r['type'], r['latitude'], r['longitude'], r['contenttext']))
db_connect.commit()
db2.close()
db_connect.close()