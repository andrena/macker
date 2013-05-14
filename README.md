[![Build Status](https://buildhive.cloudbees.com/job/andrena/job/macker/badge/icon)](https://buildhive.cloudbees.com/job/andrena/job/macker/)

[Documentation and examples](http://innig.net/macker/guide/)

This is a [fork from innig.net](http://innig.net/macker/) ([source](http://sourceforge.net/p/macker/code/177/tree/trunk/macker/)), who initiated and developed this project.

The intent of this fork is solely to ensure availability in Maven Central and updating it as necessary to provide support for today's JVMs. The original Macker (Version 0.4.2) was using [BCEL 5.2](http://commons.apache.org/bcel/) to do the bytecode parsing necessary to detect package dependencies. Unfortunately, it is no longer compatible to the Java Class File Format of Java 1.7 and 1.8. This fork eliminates the dependency to BCEL and replaces it with [Javassist](http://www.csg.is.titech.ac.jp/~chiba/javassist/), which seems to work with the newer class file versions.

See also:
* The corresponding [Maven Plugin](https://github.com/andrena/macker-maven-plugin).
