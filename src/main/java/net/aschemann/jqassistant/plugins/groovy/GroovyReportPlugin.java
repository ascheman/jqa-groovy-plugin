package net.aschemann.jqassistant.plugins.groovy;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Group;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A report plugin implementation that executes Groovy scripts for processing jQAssistant analysis results.
 * This plugin allows for custom report generation using Groovy scripting capabilities.
 * It processes concepts, constraints, and groups, providing access to results through Groovy bindings.
 */
public class GroovyReportPlugin implements ReportPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyReportPlugin.class);

    private static final String DEFAULT_REPORT_DIRECTORY = "groovy-reports";
    private static final String PROPERTY_DIRECTORY = "groovy.report.directory";
    private static final String REPORT_PROPERTY_SCRIPTNAME_PREFIX = "scriptname";

    private ReportContext reportContext;

    private File reportDirectory;
    private Map<Concept,Result> concepts = new HashMap<>();
    private Concept currentConcept;
    private Constraint currentConstraint;
    private Group currentGroup;

    /**
     * Initializes the Groovy report plugin.
     *
     * @throws ReportException if initialization fails
     */
    @Override
    public void initialize() throws ReportException {
        LOGGER.info("Starting Groovy based jQA reporting");
    }

    /**
     * Configures the plugin with the given report context and properties.
     *
     * @param reportContext the context providing report infrastructure
     * @param properties    configuration properties for the plugin
     */
    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        this.reportContext = reportContext;
        File defaultReportDirectory = reportContext.getReportDirectory(DEFAULT_REPORT_DIRECTORY);
        this.reportDirectory = getFile(PROPERTY_DIRECTORY, defaultReportDirectory, properties).getAbsoluteFile();
        LOGGER.info("Using Groovy Report directory '{}'", this.reportDirectory.getAbsolutePath());
    }

    /**
     * Retrieves a File instance based on the provided property or returns the default value.
     *
     * @param property     the property name to look up
     * @param defaultValue the default file to return if property is not found
     * @param properties   the properties map to search in
     * @return the File instance based on the property value or the default value
     */
    private File getFile(String property, File defaultValue, Map<String, Object> properties) {
        String directoryName = (String) properties.get(property);
        return directoryName != null ? new File(directoryName) : defaultValue;
    }

    /**
     * Called when report generation begins.
     *
     * @throws ReportException if an error occurs during initialization
     */
    @Override
    public void begin() throws ReportException {
        LOGGER.debug("Beginning Groovy Reports");
    }

    /**
     * Called when report generation ends. Processes all collected concepts and executes
     * their associated Groovy scripts.
     *
     * @throws ReportException if an error occurs during report finalization
     */
    @Override
    public void end() throws ReportException {
        LOGGER.debug("Finishing Groovy Report with '{}' concepts", concepts.size());
        concepts.forEach((concept, result) -> {
            LOGGER.debug("Processing Groovy concept '{}'", concept);
            concept.getReport().getProperties().forEach((key, value) -> {
                LOGGER.debug ("Checking Groovy report property '{}' = '{}'", key, value);
                if (key.toString().startsWith(REPORT_PROPERTY_SCRIPTNAME_PREFIX)) {
                    execute(value.toString(), concept, result);
                } else {
                    LOGGER.warn("Unknown Groovy report property '{}' = '{}'", key, value);
                }
            });
        });
    }

    /**
     * Executes a Groovy script with the given parameters.
     *
     * @param scriptName the name of the script to execute
     * @param concept    the concept associated with the script
     * @param result     the result to be processed
     */
    private void execute(final String scriptName, final Concept concept, final Result result) {
        LOGGER.debug("Executing Groovy Script '{}'", scriptName);
        // tag::groovy-lang-bindings[]
        Binding binding = new Binding();
        binding.setVariable("logger", LOGGER);
        binding.setVariable("concept", concept);
        binding.setVariable("reportDirectory", reportDirectory);
        binding.setVariable("result", result);
        binding.setVariable("store", reportContext.getStore());
        // end::groovy-lang-bindings[]
        GroovyShell groovyShell = new GroovyShell(binding);
        File script = new File(scriptName);
        try {
            groovyShell.evaluate(script);
        } catch (IOException e) {
            LOGGER.error("Error executing '{}': {}", script, e.getMessage());
        }

    }

    /**
     * Called when processing of a concept begins.
     *
     * @param concept the concept being processed
     * @throws ReportException if an error occurs
     */
    @Override
    public void beginConcept(Concept concept) throws ReportException {
        LOGGER.debug("Beginning Groovy concept '{}'", concept);
        currentConcept = concept;
        concepts.put(concept, null);
    }

    /**
     * Called when processing of the current concept ends.
     *
     * @throws ReportException if an error occurs
     */
    @Override
    public void endConcept() throws ReportException {
        LOGGER.debug("Finishing Groovy concept '{}'", currentConcept);
        currentConcept = null;
    }

    /**
     * Called when processing of a group begins.
     *
     * @param group the group being processed
     * @throws ReportException if an error occurs
     */
    @Override
    public void beginGroup(Group group) throws ReportException {
        LOGGER.debug("Beginning Groovy group '{}'", group);
        currentGroup = group;
    }

    /**
     * Called when processing of the current group ends.
     *
     * @throws ReportException if an error occurs
     */
    @Override
    public void endGroup() throws ReportException {
        LOGGER.debug("Finishing Groovy group '{}'", currentGroup);
        currentGroup = null;
    }

    /**
     * Called when processing of a constraint begins.
     *
     * @param constraint the constraint being processed
     * @throws ReportException if an error occurs
     */
    @Override
    public void beginConstraint(Constraint constraint) throws ReportException {
        LOGGER.debug("Beginning Groovy constraint '{}'", constraint);
        currentConstraint = constraint;
    }

    /**
     * Called when processing of the current constraint ends.
     *
     * @throws ReportException if an error occurs
     */
    @Override
    public void endConstraint() throws ReportException {
        LOGGER.debug("Finishing Groovy constraint '{}'", currentConstraint);
        currentConstraint = null;
    }

    /**
     * Sets the result for the current executable rule.
     *
     * @param result the result to be set
     * @throws ReportException if an error occurs while setting the result
     */
    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        LOGGER.debug("Setting result '{}' for concept '{}'", result, currentConcept);
        if (concepts.containsKey(currentConcept)) {
            if (null != concepts.get(currentConcept)) {
                LOGGER.error("Concept '{}' is already associated with result '{}'",
                    currentConstraint, concepts.get(currentConcept));
            } else {
                concepts.put(currentConcept, result);
            }
        }
    }
}
