Pragmatic Enterprise Scala
==========================

The code to go with the book I have written, available at:

- [https://www.createspace.com/4228776](https://www.createspace.com/4228776)
- [http://www.amazon.co.uk/Pragmatic-Enterprise-Scala-Ant-Kutschera/dp/1484007662/](http://www.amazon.co.uk/Pragmatic-Enterprise-Scala-Ant-Kutschera/dp/1484007662/)
- [http://www.amazon.com/Pragmatic-Enterprise-Scala-Ant-Kutschera/dp/1484007662/](http://www.amazon.com/Pragmatic-Enterprise-Scala-Ant-Kutschera/dp/1484007662/)
- [http://www.amazon.de/Pragmatic-Enterprise-Scala-Ant-Kutschera/dp/1484007662/](http://www.amazon.de/Pragmatic-Enterprise-Scala-Ant-Kutschera/dp/1484007662/)

There are two projects contained in this folder:

- eeventscom - This is the code that goes with the Shop site.
- eevents_admin - This is the code that goes with the Validation site.

The eeventscom folder contains a Maven script which can be used to download the dependencies.  Run "mvn validate" and that will fetch all the libraries and stick them into the Webcontent/WEB-INF/lib folder so that Eclipse can compile the project.  The project is otherwise *not* compatible with Maven, so don't bother trying a "mvn clean install" :-)

The eevents_admin folder contains a Play project and Play will automatically download the dependencies for you.


Errata
------

Page 106 - Please disregard the definition of higher kinded types - it is entirely wrong.  More information on higher kinded types is available at [http://twitter.github.io/scala_school/advanced-types.html#higher](http://twitter.github.io/scala_school/advanced-types.html#higher)

