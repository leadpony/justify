# Justify CLI

## Building from Source

The following tools are required to build this software.
* [JDK] 11
* [Apache Maven] 3.6.0 or higher


1. Obtain the source code from this repository.

  ```bash
  $ git clone https://github.com/leadpony/justify.git
  $ cd justify
  ```   

2. Build the underlying library and install it into your local Maven repository.

  ```bash
  $ cd justify
  $ mvn install -P release
  $ cd ..
  ```

3. Build and package this software.

  ```bash
  $ cd justify-cli
  $ mvn package
  ```

[JDK]: https://jdk.java.net/
[Apache Maven]: https://maven.apache.org/
