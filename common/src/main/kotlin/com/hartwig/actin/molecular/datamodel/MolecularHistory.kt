package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

data class MolecularHistory(
    val molecularTests: List<MolecularTest>,
) {
    fun allPriorMolecularTests(): List<PriorMolecularTest> {
        return molecularTests.filter { it.type == ExperimentType.IHC }
            .map { it.result as PriorMolecularTest }
    }

    fun mostRecentMolecularRecord(): MolecularRecord? {
        return molecularTests.filter { it.type == ExperimentType.WHOLE_GENOME || it.type == ExperimentType.TARGETED }
            .maxByOrNull { it.date ?: LocalDate.MIN }
            ?.result as MolecularRecord?
    }

    companion object {
        fun empty(): MolecularHistory {
            return MolecularHistory(emptyList())
        }

        fun fromInputs(molecularRecord: MolecularRecord?, priorMolecularTests: List<PriorMolecularTest>): MolecularHistory {
            return MolecularHistory(
                (if (molecularRecord != null) {
                    listOf(WGSMolecularTest.fromMolecularRecord(molecularRecord))
                } else {
                    emptyList()
                }) + IHCMolecularTest.fromPriorMolecularTests(priorMolecularTests)
            )
        }
    }
}