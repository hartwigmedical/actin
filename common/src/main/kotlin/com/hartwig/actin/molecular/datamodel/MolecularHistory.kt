package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

private const val PD_L1 = "PD-L1"
private const val IHC = "IHC"

data class MolecularHistory(
    val molecularTests: List<MolecularTest>
) {
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
        fun empty(): MolecularHistory {
            return MolecularHistory(emptyList())
        }

        fun fromWGSandIHC(wgs: MolecularRecord?, ihc: List<PriorMolecularTest>): MolecularHistory {
            return MolecularHistory(
                (if (wgs != null) {
                    listOf(MolecularTest.fromWGS(wgs))
                } else {
                    emptyList()
                }) + MolecularTest.fromIHC(ihc)
            )
        }
    }
}
