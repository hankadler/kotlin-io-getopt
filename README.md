# kotlin-io-getopt

C-style parser for command-line options.

Emulates `getopt` module from Python with minor differences.
 > See [getopt](https://docs.python.org/3.8/library/getopt.html)

## Table of Contents
- [Setup](#setup)
- [Examples](#examples)
- [License](#license)

## Setup

Add to build.gradle.kts:

```kotlin
repositories {
    maven {
        setUrl("https://dl.bintray.com/hankadler/kotlin-io")
    }
}

dependencies {
    implementation("com.hankadler.io:getopt:0.1.0")
}
```

## Examples

Source:
```kotlin
// ConsoleExample.kt

import com.hankadler.io.GetOpt

fun main(argv: Array<String>) {
    println("Command-line argv:")
    argv.forEach { println("\t$it") }

    val (opts, args) = GetOpt.getOpt(argv, "hu:p:", listOf("username=", "password="))

    println("\nParsed args:")
    println("\topts: $opts")
    println("\targs: ${args.toList()}")
}

```

Run:
```
Command-line argv:
    login
    -u
    John Smith
    -p
    abcdefg

Parsed args:
    opts: {-u=John Smith, -p=abcdefg}
    args: [login]
```

## License
[MIT](LICENSE) © Hank Adler
