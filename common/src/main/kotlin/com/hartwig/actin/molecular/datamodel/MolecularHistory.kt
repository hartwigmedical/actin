package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

private const val PD_L1 = "PD-L1"
private const val IHC = "IHC"

data class MolecularHistory(
    val molecularTests: List<MolecularTest>,
    val patientId: String
) {
    // TODO (kz) do we want helpers like this or just use the list directly?
    fun allPDL1Tests(measureToFind: String): List<PriorMolecularTest> {
        return allIHCTests(allPriorMolecularTests()).filter { it.item == PD_L1 && measureToFind == it.measure }
    }

    fun allIHCTestsForProtein(protein: String): List<PriorMolecularTest> {
        return allIHCTests(allPriorMolecularTests()).filter { it.item == protein }
    }

    private fun allIHCTests(priorMolecularTests: List<PriorMolecularTest>): List<PriorMolecularTest> {
        return priorMolecularTests.filter { it.test == IHC }
    }

    fun allPriorMolecularTests(): List<PriorMolecularTest> {
        return molecularTests.filter { it.type == ExperimentType.IHC }
            .map { it.result as PriorMolecularTest }
    }

    fun mostRecentWGS(): MolecularRecord? {
        return molecularTests.filter { it.type == ExperimentType.WHOLE_GENOME || it.type == ExperimentType.TARGETED }
            .maxByOrNull { it.date ?: LocalDate.MIN }
            ?.result as MolecularRecord?
    }

    companion object {
        fun empty(patientId: String): MolecularHistory {
            return MolecularHistory(emptyList(), patientId)
        }

        // TODO (kz) convenience constructor, but change to make it Lists of specific
        //  molecular test subtypes? see at call sites for what looks better
        fun fromWGSandIHC(molecularRecord: MolecularRecord?, priorMolecularTests: List<PriorMolecularTest>): MolecularHistory {
            return MolecularHistory(
                (if (molecularRecord != null) {
                    listOf(WGSMolecularTest.fromMolecularRecord(molecularRecord))
                } else {
                    emptyList()
                }) + IHCMolecularTest.fromPriorMolecularTests(priorMolecularTests),
                "N/A"  // TODO (kz) what to do here? add to arg, extract for molecular record?
            )
        }
    }
}