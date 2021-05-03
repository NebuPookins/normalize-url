# normalize-url

normalize-url is a Java library for normalizing URLs.

## Installation

Maven:

```xml
<dependency>
  <groupId>net.nebupookins</groupId>
  <artifactId>normalize-url</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle Groovy:

```groovy
implementation 'net.nebupookins:normalize-url:1.0.0'
```

Gradle Kotlin
```kotlin
implementation("net.nebupookins:normalize-url:1.0.0")
```

SBT
```scala
libraryDependencies += "net.nebupookins" % "normalize-url" % "1.0.0"
```

## Usage

```java
Function<String, String> normalizer = UrlNormalizer.semanticPreservingNormalizer();
String normalizedUrl = normalizer.apply("HTTP://Example.COM:80/bar/../%7Efoo%2a");
assertThat(normalizedUrl, is("http://example.com/~foo%2A"));
```

## Roadmap

I would like to eventually add in support for other Url normalizers besides
the "Semantic Preserving" one. See https://en.wikipedia.org/wiki/URI_normalization
for some ideas of normalizers that do "more dangerous" normalizations.

## Contributing

Easiest way to contribute would probably be to submit examples of
normalizations that you think should have happened but didn't (basically show
me what the input URL was, and what you think the output URL should be). Note
that I think "validating" the input URL is out of scope for this library, so
if your "input URL" isn't actually a valid URL, then I think either throwing
an exception or returning non-sensical output is okay and not a bug.

Pull requests are welcome. For major changes, please open an issue first to
discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)
