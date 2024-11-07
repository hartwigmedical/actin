package com.hartwig.actin.datamodel.molecular

import java.time.LocalDate

interface MolecularTest {
    val experimentType: ExperimentType
    val testTypeDisplay: String?
    val date: LocalDate?
    val drivers: Drivers
    val characteristics: MolecularCharacteristics
    val evidenceSource: String
    val hasSufficientQuality: Boolean?

    fun testsGene(gene: String): Boolean
}

const val NO_EVIDENCE_SOURCE = "none"
