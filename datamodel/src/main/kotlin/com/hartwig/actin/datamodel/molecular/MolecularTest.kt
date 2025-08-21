package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import java.time.LocalDate
import java.util.function.Predicate

data class MolecularTest(
    val date: LocalDate?,
    val sampleId: String?,
    val reportHash: String?,
    val experimentType: ExperimentType,
    val testTypeDisplay: String?,
    val targetSpecification: PanelTargetSpecification?,
    val refGenomeVersion: RefGenomeVersion,
    val containsTumorCells: Boolean,
    val hasSufficientPurity: Boolean,
    val hasSufficientQuality: Boolean,
    val isContaminated: Boolean,
    val drivers: Drivers,
    val characteristics: MolecularCharacteristics,
    val immunology: MolecularImmunology,
    val pharmaco: Set<PharmacoEntry>,
    val evidenceSource: String,
    val externalTrialSource: String
) {
    
    fun testsGene(gene: String, targets: Predicate<List<MolecularTestTarget>>): Boolean {
        if (experimentType == ExperimentType.HARTWIG_WHOLE_GENOME) return true

        if (targetSpecification == null) {
            throw IllegalStateException(
                "If experiment type is not ${ExperimentType.HARTWIG_WHOLE_GENOME} then a panel specification must be included"
            )
        } else {
            return targetSpecification.testsGene(gene, targets)
        }
    }

    fun hasSufficientQualityAndPurity(): Boolean {
        return hasSufficientQuality && hasSufficientPurity
    }

    fun hasSufficientQualityButLowPurity(): Boolean {
        return hasSufficientQuality && !hasSufficientPurity
    }
}
