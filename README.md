dijon
=====

Purpose
-------
Dijon is a collection of simple set of webservices for retrieving data from both 
the data warehouse(aka bourgogne) and the Solr-index (aka izeure). 

Requirement
-----------
* MySQL and Java JDK 1.7 and an internet connection.
* Play framework version 2.2.x (<a href="http://www.playframework.com/download#older-versions">http://www.playframework.com/download#older-versions</a>). Please note that as of today dijon DOESN'T WORK with Play 2.3.X
* The Bourgogne database (<a href="http://github.com/GBIF-Sweden/bourgogne">http://github.com/GBIF-Sweden/bourgogne</a>)
* The Izeure index (<a href="https://github.com/GBIF-Sweden/izeure">https://github.com/GBIF-Sweden/izeure</a>)
* Apache Tomcat 7 (<a href="http://tomcat.apache.org/">http://tomcat.apache.org/</a>)

Installation
------------
Although Play framework applications can run by themselves, it has been decided 
to make Dijon a usual web application run by Tomcat, hence the conversion to a 
WAR file later.

First of all, Solr can run as a standalone application or as a Tomcat 
application. This means that solr car run on different port according to you 
installation and that you may change a the solURL constant in the file 
dijon/app/controllers/Search.java.

Your application needs too to access your Bourgogne database. Open the file 
dijon/conf/application.conf and find the section "Database configuration". There 
you must change the user name (in db.default.user) and password (in 
db.default.password) for making Dijon access your database.

You must as well decides which name you will give to your Dijon installation. 
It can be as simple as "dijon", or "api", or "whateveryoulike". In the same file 
as before you should find the variable application.context and change it value 
according to what you just choose. It is very important otherwise the 
application won't work. By default its value is "/dijon/".

Download Play framework and install it. On GNU/Linux change the PATH from the 
command line:
<code>
$ export PATH=$PATH:/path/to/play-2.2.X
</code>

You can then move the directory of Dijon, then start Play and then create a WAR 
file:
<code>
$ play
[dijon]$ war
</code>

You can then rename the file /path/to/dijon/target/dijon-1.0-SNAPSHOT.war to
dijon.war or api.war or whateveryoulike.war as you decided before, and upload it 
to your Tomcat webserver.

Usage
-----
Get the size of the index: done through GET method on 
URL: http://www.example.com/dijon/izeure/size

Make a search: done through POST method
URL: http://www.example.com/dijon/search/occurrences
DarwinCore Parameters are:
* Scientific name: [scientificName] as string
* Event date: [eventDateLow] and [eventDateHigh] as string: in form YYYY-MM-DD
* Basis of record: [basisOfRecord] as string: can only be "Fossil Specimen", 
"Living Specimen", "Human Observation", "Machine Observation", "Preserved Specimen"
* Catalogue number: [catalogNumber] as string
* Recorded by: [recordedBy] as string
* Field number: [fieldNumber] as string
* Institution code: [institutionCode] as string
* Collection code: [collectionCode] as string
* Continent: [continent] as string
* Country: [country] as string
* State/Province: [stateProvince] as string
* County: [county] as string
* Bounding box: North East [bb_ne] and South West [bb_sw] in decimal degrees

Other parameters:
* Sorting columns: [sort] as string; contains one column or a comma-separated 
list of columns
* Sorting order: [order] as string: "asc" or "desc"
* Page number: [page] as number; starts at 0
* Number of records: [size] as number

Download the result of a search: done through GET metod
URL: http://www.example.com/dijon/download/occurrences
DarwinCore parameters are the same as above. They are the only one required. 
This provides a CSV file containing up to one million records.

Get the last version of an occurrence. ABCD1234 represents the occurrence ID.
As XML:
http://www.example.com/dijon/occurrence/ABCD1234 or
http://www.example.com/dijon/occurrence/ABCD1234/xml

As JSON:
http://www.example.com/dijon/occurrence/ABCD1234/json

Get the given version of an occurrence. VER123 represents the version number.
As XML:
http://www.example.com/dijon/occurrence/ABCD1234/VER123 or
http://www.example.com/dijon/occurrence/ABCD1234/VER123/xml

As JSON:
http://www.example.com/dijon/occurrence/ABCD1234/VER123/json

Get the all the versions of an occurrence:
http://www.example.com/dijon/occurrence/ABCD1234/raw

