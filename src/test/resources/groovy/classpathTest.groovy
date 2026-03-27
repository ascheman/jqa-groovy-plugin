#!/usr/bin/env groovy

logger.info("Classpath-loaded Groovy script executing!")
logger.info("Creating report for concept '{}' in '{}'", concept, reportDirectory)

File output = new File(reportDirectory, "classpath-test.txt")
output.delete()
output.append("Loaded from classpath!")
