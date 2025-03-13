package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.FunctionInput
import com.hartwig.actin.datamodel.trial.Trial
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Path

class ProteinList(val outputDirectory: String) {
    val LOGGER: Logger = LogManager.getLogger(ProteinList::class.java)
    fun writeListOfIhcProteins(trials: List<Trial>) {
        LOGGER.info("Dumping list of referenced proteins")
        var functions = trials.flatMap { it.generalEligibility }.map { it.function }
            .filter { it.rule.input in (listOf(FunctionInput.ONE_PROTEIN, FunctionInput.ONE_PROTEIN_ONE_INTEGER)) }
        functions.forEach { f -> LOGGER.info("IHC: ${f.rule.name} params: ${f.parameters}") }
        Files.write(Path.of(outputDirectory, "ihc_proteins.list"), functions.map { it.parameters.first().toString() })
        LOGGER.info("Proteins list dumped")
    }
}