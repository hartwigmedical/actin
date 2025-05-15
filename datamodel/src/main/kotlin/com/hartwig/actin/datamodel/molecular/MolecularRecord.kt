package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import java.time.LocalDate
import java.util.function.Predicate

data class MolecularRecord(
    val sampleId: String,
    val refGenomeVersion: RefGenomeVersion,
    val externalTrialSource: String,
    val containsTumorCells: Boolean,
    val isContaminated: Boolean,
    val immunology: MolecularImmunology,
    val pharmaco: Set<PharmacoEntry>,
    val specification: PanelSpecification?,
    override val hasSufficientPurity: Boolean,
    override val hasSufficientQuality: Boolean,
    override val testTypeDisplay: String? = null,
    override val experimentType: ExperimentType,
    override val date: LocalDate?,
    override val drivers: Drivers,
    override val characteristics: MolecularCharacteristics,
    override val evidenceSource: String,
) : MolecularTest {

    override fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>) =
        if (experimentType == ExperimentType.HARTWIG_TARGETED) isTestedInTargetedPanel(gene, molecularTestTargets) else true

    private fun isTestedInTargetedPanel(
        gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>
    ) = specification?.testsGene(gene, molecularTestTargets)
        ?: throw IllegalStateException("If experiment type is ${ExperimentType.HARTWIG_TARGETED} then a panel specification must be included")

    override fun hasSufficientQualityAndPurity(): Boolean {
        return hasSufficientQuality && hasSufficientPurity
    }

    override fun hasSufficientQualityButLowPurity(): Boolean {
        return hasSufficientQuality && !hasSufficientPurity
    }
}