package io.github.krandalf75.translate.plugin;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.service.OpenAiService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import nu.studer.java.util.OrderedProperties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which translates a properties file to different locales.
 */
@Mojo( name = "translate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class TranslateMojo extends AbstractMojo {

    /**
     * Location of the properties file.
     */
    @Parameter(property = "propertiesFile", defaultValue = "path/to/your/properties/file" )
    private File propertiesFile;

    /**
     * Locale of the source properties file.
     */
    @Parameter(property="sourceLocale", defaultValue="en" )
    private String sourceLocale;

    /**
     * Locales to translate the properties file to.
     */
    @Parameter(property="locales", defaultValue="en,fr,de" )
    private String locales;

    /**
     * OpenAI API key.
     */
    @Parameter(property="openai.api.key", defaultValue="YOUR_OPENAI_API_KEY" )
    private String apiKey;

    /**
     * Execute plugin
     * @throws MojoExecutionException exception
     */
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Translating properties file: " + propertiesFile.getPath() + " to locales: " + locales);

        OrderedProperties properties = loadPropertiesFromFile(propertiesFile);

        if (apiKey == null || apiKey.isEmpty()) {
            throw new MojoExecutionException("OpenAI API key not found. Please provide the API key.");
        }

        OpenAiService service = new OpenAiService(apiKey);

        String[] localeArray = locales.split(",");
        for (String locale : localeArray) {
            File translatedFile = getTranslatedFile(locale);
            OrderedProperties translatedProperties = loadTranslatedProperties(translatedFile);

            try (Writer writer = new FileWriter(translatedFile)) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    String key = entry.getKey();
                    if (!translatedProperties.containsProperty(key)) {
                        String value = entry.getValue();
                        String localizedValue = localizeValue(service, key, value, locale, sourceLocale); // Implement your localization logic here
                        translatedProperties.setProperty(key, localizedValue);
                    }
                }
                storeTranslatedProperties(translatedProperties, writer, locale);
            } catch (IOException ex) {
                throw new MojoExecutionException("Error creating translated file " + translatedFile, ex);
            }
        }
    }

    private OrderedProperties loadPropertiesFromFile(File file) throws MojoExecutionException {
        OrderedProperties properties = new OrderedProperties();
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error reading properties file: " + file.getPath(), ex);
        }
        return properties;
    }

    private File getTranslatedFile(String locale) {
        String fileName = propertiesFile.getName();
        String parentPath = propertiesFile.getParent();
        String baseName = fileName.substring(0, fileName.lastIndexOf("."));
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = baseName + "_" + locale + extension;
        File parentDirectory = new File(parentPath);
        return new File(parentDirectory, newFileName);
    }

    private OrderedProperties loadTranslatedProperties(File translatedFile) {
        OrderedProperties.OrderedPropertiesBuilder builder = new OrderedProperties.OrderedPropertiesBuilder();
        builder.withSuppressDateInComment(true);
        OrderedProperties translatedProperties = builder.build();
        if (translatedFile.exists()) {
            try (InputStream translatedInput = new FileInputStream(translatedFile)) {
                translatedProperties.load(translatedInput);
            } catch (IOException ex) {
                getLog().warn("Error reading translated file: " + translatedFile.getPath());
            }
        }
        return translatedProperties;
    }

    /**
     * Localize value using OpenAi Service.
     *
     * @param service
     * @param value
     * @param locale
     * @param sourceLocale
     * @return
     * @throws MojoExecutionException
     */
    private String localizeValue(OpenAiService service, String key, String value, String locale, String sourceLocale) throws MojoExecutionException {

        if (sourceLocale.equals(locale)) {
            return value;
        }

        String prompt = "On the next line, I will pass a <key>=<value> string. Translate the value from locale '" + sourceLocale + "' to locale '" + locale + "'. Answer only with format <key>=<value>. Ensure that the translations are accurate and maintain the <key>=<value> structure.\n" + key + "=" + value;
        getLog().info("Prompt:" + prompt);

        CompletionRequest request = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt(prompt)
                .echo(false)
                .user("hhh")
                .maxTokens(1024)
                .n(1)
                .build();

        CompletionResult result = service.createCompletion(request);
        getLog().info("model:" + result.getModel());

        List<CompletionChoice> choices = result.getChoices();

        if (choices != null && !choices.isEmpty()) {
            String text = choices.get(0).getText();
            getLog().info("translation:" + text);
            text = text.substring(text.indexOf("=") + 1).trim();
            getLog().info("text:" + text);
            return text;
        } else {
            getLog().info("Response empty");
            return "";
        }
    }

    private void storeTranslatedProperties(OrderedProperties properties, Writer writer, String locale) throws IOException {
        properties.store(writer, "Translated properties for locale: " + locale);
    }
}
