simplehttpd

This Java program is a simple httpd that requires zero configuration (except for setting the port number upon start up).
This has been developed for classes at Faculty of Information and Communications, Bunkyo University, Japan, where
students learn how to write web pages. This program is deployed in the classes for the students to experience communication
between their web browsers and an HTTP server.

To build the program:
$ ant package

This will create "javaout" directory under the same level as "java", where simplehttpd.jar is put.
Place the jar file anywhere that fits.

To start the program:
$ java -jar simplehttpd.jar

or double-click on the icon of the jar file if your environment allows that.

Upon starting up, the program asks for the port number, whose default value is 8480.
The document root is where the jar file is placed.
While at work, the server shows the log of HTTP communication.
