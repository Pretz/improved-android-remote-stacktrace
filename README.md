Android Remote Stacktrace: Improved
===================================

**This project has been forked to carry on maintenance.** We thank Alexander Pretzlav for keeping the original code and improvements.

This project is fork of [Android Remote Stacktrace][1] which adds a number of important features:

 * Customizable interface for handling stacktraces
 * Behaves better with the filesystem by saving stacktraces to their own directory, instead of your application's root documents directory
 * Allows optional debug logging, which will also mark your application as a DEBUG build in stack traces

See also: [javadoc](http://pretz.github.com/improved-android-remote-stacktrace/javadoc/)

What follows is the original documentation for Android Remote Stacktrace, with small modifications for the few API changes made by me.

## Client side usage

Download the latest `trace.jar` file [found here](https://github.com/downloads/Pretz/improved-android-remote-stacktrace/trace.jar). Drop it into your Android project and in the properties for your project add it to "Java Build Path" -> "Libraries". Alternately, check out the source of this project, import it as an Eclipse Android Library Project, and add it as a library dependency to your app.

If you use the default `HttpPostStackInfoSender`, you must enable internet access for your application:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

In the onCreate method of your activity or in your service, you must call either `public static boolean register(Context context, String url)` (for the default HTTP POST behavior) or `public static boolean register(Context context, final StackInfoSender stackInfoSender, final boolean debug)` found in the class ExceptionHandler. Do something like this:

```java
ExceptionHandler.register(this, "http://trace.nullwire.com");
```

Or, using your own handler:

```java
ExceptionHandler.register(this, new MyFancyExceptionSender(), isDebugEnabled);
```

If you wish to implement your own StackInfoSender, see the [javadoc documentation](http://pretz.github.com/improved-android-remote-stacktrace/javadoc/index.html?com/nullwire/trace/StackInfoSender.html) for the interface.

## Server side installation

If you would like to store your stack traces on your own server, you will have to register the exception handler like this:

```java
ExceptionHandler.register(this, "http://your.domain/path"); 
```

At `http://your.domain/path` the client side implementation will expect to find [this simple PHP script](https://github.com/Pretz/improved-android-remote-stacktrace/blob/master/server/collect/server.php), which will take three POST parameters: 'package_name', 'package_version' and 'stacktrace'. The collected data is simply stored in a plain text file. You can extend the script to send you an email with the stack trace if you like - just uncomment the last line and change the email address.

## Building the JAR

The JAR may be built by issuing the following command:

    ant jar

This will produce a trace.jar file.

Cleaning up is done by:

    ant clean
    
## Support

If you have problems, feel free to drop me a mail at mads.kristiansen@nullwire.com.

## Additional information

How to integrate stack trace collection with Redmine: http://nullwire.com/capturing_android_exceptions_remotely.

## Contributors

Thanks to these people, who contributed with code changes and/or bug reports.

[Glen Humphrey](http://glendonhumphrey.com/), [Evan Charlton](http://evancharlton.com/), [Peter Hewitt](http://dweebos.com/)

## License


The MIT License

Copyright (c) 2009 Mads Kristiansen, Nullwire ApS

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


[1]: http://code.google.com/p/android-remote-stacktrace