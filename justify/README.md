# Justify

## Building from Source

### Maven

The following tools are required to build this software.
* [JDK] 11
* [Apache Maven] 3.6.0 or higher

The commands below build the software and install it into your local Maven repository.

```bash
$ git clone --recursive https://github.com/leadpony/justify.git
$ cd justify/justify/
$ mvn clean install
```

### Eclipse

The following Maven profiles should be activated manually after importing the project into your workspace.

* `java8` (activate)
* `verbose` (activate)

[JDK]: https://jdk.java.net/
[Apache Maven]: https://maven.apache.org/
