package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.orange.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.orange.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import java.time.LocalDate

data class MolecularRecord(
    val patientId: String,
    val sampleId: String,
    val refGenomeVersion: RefGenomeVersion,
    val externalTrialSource: String,
    val containsTumorCells: Boolean,
    val hasSufficientQualityAndPurity: Boolean,
    val hasSufficientQuality: Boolean,
    val immunology: MolecularImmunology,
    val pharmaco: Set<PharmacoEntry>,
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val drivers: MolecularDrivers,
    override val characteristics: MolecularCharacteristics,
    override val evidenceSource: String,
) : MolecularTest<MolecularDrivers> {
    override fun testsGene(gene: String) = true
}
