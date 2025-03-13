package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.FunctionInput
import com.hartwig.actin.datamodel.trial.Trial
import java.nio.file.Files
import java.nio.file.Path

class ProteinList(val outputDirectory: String) {
    fun writeListOfIhcProteins(trials: List<Trial>) {
        var functions = trials.flatMap { it.generalEligibility }.map { it.function }
            .filter { it.rule.input in (listOf(FunctionInput.ONE_PROTEIN, FunctionInput.ONE_PROTEIN_ONE_INTEGER)) }
        Files.write(Path.of(outputDirectory, "ihc_proteins.list"), functions.map { it.parameters.first().toString() })
    }
}