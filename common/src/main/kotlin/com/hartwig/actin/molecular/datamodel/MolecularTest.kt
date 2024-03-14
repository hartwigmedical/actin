package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

interface MolecularTest {
    val type: ExperimentType
    val date: LocalDate?
    val result: Any
}

data class WGSMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: MolecularRecord
) : MolecularTest {

    companion object {
        fun fromMolecularRecord(result: MolecularRecord): WGSMolecularTest {
            return WGSMolecularTest(result.type, result.date, result)
        }
    }
}

data class IHCMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: PriorMolecularTest
) : MolecularTest {

    companion object {
        fun fromPriorMolecularTest(result: PriorMolecularTest): IHCMolecularTest {
            return IHCMolecularTest(ExperimentType.IHC, date = null, result)
        }

        fun fromPriorMolecularTests(priorMolecularTests: List<PriorMolecularTest>): List<IHCMolecularTest> {
            return priorMolecularTests.map { IHCMolecularTest.fromPriorMolecularTest(it) }
        }
    }
}


