# Translate Plugin

The Translate Plugin is a Maven plugin that facilitates the translation of a properties file to different locales using the OpenAI API.

## Usage

The Translate Plugin provides the following goals:

- **translate**: Translates the properties file to specified locales.

To use the plugin, include the necessary configuration in your `pom.xml` as shown in the example below:

```xml
<project>
  <!-- ... -->
  <build>
    <plugins>
      <plugin>
        <groupId>org.krandalf75</groupId>
        <artifactId>translate-plugin</artifactId>
        <version>1.0</version>
        <executions>
          <execution>
            <goals>
              <goal>translate</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
            <propertiesFile>src/main/resources/messages.properties</propertiesFile>
            <sourceLocale>en</sourceLocale>
            <locales>es,fr,de</locales> <!-- Customize the locales as needed -->
            <openai.api.key>OpenAI API key.</openai.api.key>
        </configuration>
      </plugin>
    </plugins>
  </build>
  <!-- ... -->
</project>
```

## Configuration

The Translate Plugin can be configured with the following parameters:

- `propertiesFile`: Location of the properties file.
- `sourceLocale`: Locale of the source properties file.
- `locales`: Locales to translate the properties file to.
- `openai.api.key`: OpenAI API key.

Make sure to provide the required information to ensure the smooth functioning of the plugin.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.


## Acknowledgements

The Translate Plugin is developed with the support of the following libraries:

- [OpenAI-Java library](https://github.com/TheoKanning/openai-java) by Theo Kanning. Licensed under MIT License.
- [java-ordered-properties](https://github.com/etiennestuder/java-ordered-properties) by Etienne Studer. Licensed under the Apache License 2.0.

