# Justify

## Building from Source

### Maven

The following tools are required to build this software.
* [JDK] 11
* [Apache Maven] 3.6.0

The commands below build the software and install it into your local Maven repository.

```bash
$ git clone https://github.com/leadpony/justify.git
$ cd justify/justify/
$ mvn clean install
```

### Eclipse

The following Maven profiles should be activated or deactivated manually after importing the project into your workspace.

* `jdk8` (activate)
* `jdk9-or-higher` (deactivate)
* `verbose` (activate)

[JDK]: https://jdk.java.net/
[Apache Maven]: https://maven.apache.org/
