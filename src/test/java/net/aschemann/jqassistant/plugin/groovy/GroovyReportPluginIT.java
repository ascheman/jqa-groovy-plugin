package net.aschemann.jqassistant.plugin.groovy;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Report;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.plugin.common.impl.report.AbstractReportPluginTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GroovyReportPluginIT extends AbstractReportPluginTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyReportPluginIT.class);

    public GroovyReportPluginIT() {
        super(new GroovyReportPlugin());
    }

    @Test
    void testCreateDummyFile() throws RuleException {
        Properties reportProperties = new Properties();
        reportProperties.setProperty("scriptname", "src/test/resources/groovy/createDummyFile.groovy");
        Report groovyReport = Report.builder().properties(reportProperties).build();
        Concept tst =
            Concept.builder().id("groovy-integration-test:CreateDummyFile").severity(Severity.MINOR).report(groovyReport).build();
        Map<String, Object> properties = Collections.emptyMap();
        plugin.configure(reportContext, properties);
        plugin.begin();
        apply(tst, SUCCESS);
        plugin.end();

        File groovyReportDirectory = reportContext.getReportDirectory("groovy-reports");
        assertThat(groovyReportDirectory.exists()).isTrue();
        File reportFile = new File(groovyReportDirectory, "dummy.txt");
        assertThat(reportFile.exists()).isTrue();
        assertThat(reportFile).hasContent("Bin there, done nothing!");
    }

    @Override
    protected <T extends ExecutableRule<?>> Result<T> getResult(T rule, Result.Status status) {
        return Result.<T> builder().rule(rule).severity(rule.getSeverity()).status(status).columnNames(null).rows(null).build();
    }
}
