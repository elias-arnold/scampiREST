README - Scampi App Developer Package
=====================================

This package includes everything required to develop applications
for the Scampi opportunistic networking middleware:
 - Developer Guide
 - API library (AppLib), including source and javadocs
 - Example projects
 - Android applications
 - JAR binary of the Scampi middleware

* Contents of the Package:

Developer Guide.pdf: Developer guide detailing the use of the
Scampi API.

lib: JAR files for Java/Android projects that contain the binary,
source and javadocs for the AppLib API library that is used to
develop applications for the Scampi middleware.

example: Examples for using the Scampi API. Note that copy of the
JAR files must be placed in the project lib/ directories for the
projects to compile. Includes IntelliJ projects.

example/android: Example Android applications that use the Scampi
middleware. Includes a messaging application and a photo sharing
application.

bin: Binaries for the Scampi instances and applications.

bin/android: Android applications. LibeRouter is the middleware
instance that must be running on the device and that applications
connect to. GuerrillaBoards messaging application, and GuerrillaPics
photo sharing application.

bin/java: Pure Java middleware instance. Can be run on any machine
with a JVM. Comes with a default configuration that will connect
to any Android devices running the LibeRouter application. To
run the middleware instance:
$ java -jar SCAMPI.jar default_settings.txt
or with debug logging to stdout:
$ java -jar SCAMPI.jar -s -l debug default_settings.txt

* Version History:
- 0.4
 * Updated SCAMPI binary
 * Updated default settings for SCAMPI binary (disabled multicast)
- 0.2   - Android HTML5 API:
 * lib/ScampiAndroidHtml5
 * example/android/ScampiHtml5-HelloWorld
 * Developer Guide.pdf: Added section on Android HTML5 API
- 0.1.1 - Updated Developer Guide.pdf
- 0.1   - Initial release