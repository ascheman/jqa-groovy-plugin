#!/usr/bin/env groovy

logger.info ("Here we go, Groovy!")
logger.info ("Creating report for concept '{}' with result '{}' in '{}'", concept, result, reportDirectory)

File output = new File(reportDirectory, "dummy.txt")
output.delete()
output.append("Bin there, done nothing!")
