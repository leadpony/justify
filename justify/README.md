# Justify

## Building from Source

### Maven

The following tools are required to build this software.
* [JDK 9] or higher
* [Apache Maven] 3.6.0 or higher

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

[JDK 9]: https://jdk.java.net/archive/
[Apache Maven]: https://maven.apache.org/
