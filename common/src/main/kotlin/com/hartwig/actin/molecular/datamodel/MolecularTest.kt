package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

data class MolecularTest(
    val type: ExperimentType,
    val date: LocalDate?,
    val result: Any
) {

    companion object {
        fun fromWGS(result: MolecularRecord): MolecularTest {
            return MolecularTest(result.type, result.date, result)
        }

        fun fromIHC(result: PriorMolecularTest): MolecularTest {
            return MolecularTest(ExperimentType.IHC, date = null, result)
        }

        fun fromIHC(priorMolecularTests: List<PriorMolecularTest>): List<MolecularTest> {
            return priorMolecularTests.map { MolecularTest.fromIHC(it) }
        }
    }
}