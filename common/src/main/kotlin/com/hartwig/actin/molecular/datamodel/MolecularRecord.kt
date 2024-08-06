package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.orange.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import java.time.LocalDate

data class MolecularRecord(
    val patientId: String,
    val sampleId: String,
    val refGenomeVersion: RefGenomeVersion,
    val externalTrialSource: String,
    val containsTumorCells: Boolean,
    val isContaminated: Boolean,
    val hasSufficientPurity: Boolean,
    val immunology: MolecularImmunology,
    val pharmaco: Set<PharmacoEntry>,
    val hasSufficientQuality: Boolean? = null,
    override val testTypeDisplay: String? = null,
    override val experimentType: ExperimentType,
    override val date: LocalDate?,
    override val drivers: Drivers,
    override val characteristics: MolecularCharacteristics,
    override val evidenceSource: String,
) : MolecularTest {
    override fun testsGene(gene: String) =
        if (experimentType == ExperimentType.HARTWIG_TARGETED) drivers.copyNumbers.any { gene == it.gene } else true

    fun hasSufficientQualityAndPurity(): Boolean {
        return hasSufficientQuality == true && hasSufficientPurity
    }

    fun hasSufficientQualityButLowPurity(): Boolean {
        return hasSufficientQuality == true && !hasSufficientPurity
    }
}