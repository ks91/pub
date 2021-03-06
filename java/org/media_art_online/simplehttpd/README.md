#simplehttpd

This Java program is a simple httpd that requires zero configuration (except for setting the port number upon start up).
This has been developed for classes at Faculty of Information and Communications, Bunkyo University, Japan, where
students learn how to write web pages. This program is deployed in the classes for the students to experience communication
between their web browsers and an HTTP server.

To build the program:
`$ ant package`

This will create "javaout" directory under the same level as "java", where simplehttpd[-version].jar is put.
Place the jar file anywhere that fits.

To start the program:
`$ java -jar simplehttpd[-version].jar`

or double-click on the icon of the jar file if your environment allows that.

Upon starting up, the program asks for the port number, whose default value is 8480.
The document root is where the jar file is placed.
While at work, the server shows the log of HTTP communication.

What's new in version 1.1.3 (2015-06-14):
- Handles query strings (ignores them).

What's new in version 1.1.2 (2015-05-31):
- Better XML syntax checking and error messages.

What's new in version 1.1.1 (2015-05-24):
- Better XML syntax error messages.
- Perhaps now appears on Windows taskbar.

What's new in version 1.1.0 (2015-05-23):
- Shows a pop-up menu over the log window (answered a student's request)
- Directory listing.
- Optional XML syntax checking for HTML files.
- Shows a mouse-over text for URL-encoded strings on the log window.

What's new in version 1.0.1:
- Supports well-known ports (user needs to be authorized).
- "Permission denied" and "Address already in use" errors are detected upon socket binding.
- Supports URL-encoded URLs.
- Log dialog correctly displays the port number.
