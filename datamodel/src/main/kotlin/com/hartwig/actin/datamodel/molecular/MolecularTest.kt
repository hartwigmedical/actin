package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import java.time.LocalDate
import java.util.function.Predicate

interface MolecularTest {
    val experimentType: ExperimentType
    val testTypeDisplay: String?
    val date: LocalDate?
    val drivers: Drivers
    val characteristics: MolecularCharacteristics
    val evidenceSource: String
    val hasSufficientPurity: Boolean
    val hasSufficientQuality: Boolean

    fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>): Boolean

    fun hasSufficientQualityAndPurity(): Boolean {
        return hasSufficientQuality && hasSufficientPurity
    }

    fun hasSufficientQualityButLowPurity(): Boolean {
        return hasSufficientQuality && !hasSufficientPurity
    }
}
