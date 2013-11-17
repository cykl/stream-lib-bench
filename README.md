
# Some benchmarks for the stream-lib

This small project is intended to help
[stream-lib](https://github.com/clearspring/stream-lib) contributors to check
the performance impact of their contribution and to discover area of
improvement for the project.

Micro benchmarks must be used to compare two implementations or to track
performance regressions. Do not use them to predict application behavior or
to known if a given operation is fast enough. They are not realistic use cases, 
and they are written to be as fast as possible (no IO bottleneck, 
everyhing in CPU cache etc.).

Macro benchmarks try to simulate real world use cases. They can be used to get
back-of-the-envelope performance estimations for real world application, or to
discover if it worth optimizing something further. For example, the merge
operation will most likely require reading bytes from the disk or network. If
the code is fast enough to be IO bound, optimizing it is a waste of time.

Be sure to understand the meaning of a given test and how compare two numbers
before trying to use it.

This project is very young, always check the code and the output before
phrasing any statement. Feel free to provide feedback, improve the test suite
or add new tests.

## Howto 

### Install dependencies

#### Java

JDK 7 or higher is required.

#### jmh

The benchmarks use [jmh](http://openjdk.java.net/projects/code-tools/jmh/). 
Unfortunately the OpenJDK team has not yet released a stable version of jmh.
You have to build it by yourself and publish the artifact in your local 
Maven repository. 

~~~bash
$ hg clone http://hg.openjdk.java.net/code-tools/jmh/ jmh
$ cd jmh/
$ mvn clean install -DskipTests=true
~~~

### Build

~~~bash
$ gradle compile
~~~

### Run

~~~bash
# List available benchmarks
$ gradle run -Pargs="-l"
# Run the whole test suite
$ gradle run
~~~~

