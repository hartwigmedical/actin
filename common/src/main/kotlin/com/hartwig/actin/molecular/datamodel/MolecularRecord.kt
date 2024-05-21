package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import java.time.LocalDate

data class MolecularRecord(
    override val type: ExperimentType,
    override val date: LocalDate?,
    val patientId: String,
    val sampleId: String,
    val refGenomeVersion: RefGenomeVersion,
    val evidenceSource: String,
    val externalTrialSource: String,
    val containsTumorCells: Boolean,
    val hasSufficientQualityAndPurity: Boolean,
    val hasSufficientQuality: Boolean,
    val characteristics: MolecularCharacteristics,
    val drivers: MolecularDrivers,
    val immunology: MolecularImmunology,
    val pharmaco: Set<PharmacoEntry>
) : MolecularTest
