package com.hartwig.actin.molecular.datamodel

import java.time.LocalDate

interface MolecularTest {
    val experimentType: ExperimentType
    val testTypeDisplay: String?
    val date: LocalDate?
    val drivers: Drivers
    val characteristics: MolecularCharacteristics
    val evidenceSource: String

    fun testsGene(gene: String): Boolean
}

const val NO_EVIDENCE_SOURCE = "none"
