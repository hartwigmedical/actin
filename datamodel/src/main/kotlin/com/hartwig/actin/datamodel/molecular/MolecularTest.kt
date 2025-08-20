package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import java.time.LocalDate
import java.util.function.Predicate

data class MolecularTest(
    val sampleId: String,
    val refGenomeVersion: RefGenomeVersion,
    val externalTrialSource: String,
    val containsTumorCells: Boolean,
    val isContaminated: Boolean,
    val immunology: MolecularImmunology,
    val pharmaco: Set<PharmacoEntry>,
    val targetSpecification: PanelTargetSpecification?,
    val hasSufficientPurity: Boolean,
    val hasSufficientQuality: Boolean,
    val testTypeDisplay: String? = null,
    val experimentType: ExperimentType,
    val date: LocalDate?,
    val drivers: Drivers,
    val characteristics: MolecularCharacteristics,
    val evidenceSource: String)
{
    fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>): Boolean

    fun hasSufficientQualityAndPurity(): Boolean {
        return hasSufficientQuality && hasSufficientPurity
    }

    fun hasSufficientQualityButLowPurity(): Boolean {
        return hasSufficientQuality && !hasSufficientPurity
    }
    
}
