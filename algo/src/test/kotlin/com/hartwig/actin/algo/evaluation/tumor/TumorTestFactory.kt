package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory

internal object TumorTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val baseMolecular = TestMolecularFactory.createMinimalTestMolecularRecord()

    fun withDoids(vararg doids: String): PatientRecord {
        return withDoids(setOf(*doids))
    }

    fun withDoidsAndAmplification(doids: Set<String>, amplifiedGene: String): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(doids = doids),
            molecularHistory = MolecularHistory.fromInputs(
                listOf(
                    baseMolecular.copy(
                        characteristics = baseMolecular.characteristics.copy(ploidy = 2.0),
                        drivers = baseMolecular.drivers.copy(
                    copyNumbers = setOf(
                        TestCopyNumberFactory.createMinimal().copy(
                            isReportable = true,
                            gene = amplifiedGene,
                            geneRole = GeneRole.ONCO,
                            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                            type = CopyNumberType.FULL_GAIN,
                            minCopies = 20,
                            maxCopies = 20
                        )
                    )
                )
                    )
                ), emptyList()
            )
        )
    }

    fun withDoidsAndAmplificationAndPriorMolecularTest(
        doids: Set<String>, amplifiedGene: String, priorMolecularTests: List<PriorMolecularTest>
    ): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(doids = doids),
            molecularHistory = MolecularHistory.fromInputs(
                listOf(
                    baseMolecular.copy(
                        characteristics = baseMolecular.characteristics.copy(ploidy = 2.0),
                        drivers = baseMolecular.drivers.copy(
                    copyNumbers = setOf(
                        TestCopyNumberFactory.createMinimal().copy(
                            isReportable = true,
                            gene = amplifiedGene,
                            geneRole = GeneRole.ONCO,
                            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                            type = CopyNumberType.FULL_GAIN,
                            minCopies = 20,
                            maxCopies = 20
                        )
                    )
                )
                    )
                ),
                priorMolecularTests
            )
        )
    }

    fun withDoidAndType(doid: String, primaryTumorType: String?): PatientRecord {
        return withTumorDetails(TumorDetails(doids = setOf(doid), primaryTumorType = primaryTumorType))
    }

    fun withDoidAndSubLocation(doid: String, primaryTumorSubLocation: String?): PatientRecord {
        return withTumorDetails(TumorDetails(doids = setOf(doid), primaryTumorSubLocation = primaryTumorSubLocation))
    }

    fun withDoidAndDetails(doid: String, extraDetails: String): PatientRecord {
        return withTumorDetails(TumorDetails(doids = setOf(doid), primaryTumorExtraDetails = extraDetails))
    }

    fun withDoidAndTypeAndDetails(doid: String, type: String, extraDetails: String): PatientRecord {
        return withTumorDetails(TumorDetails(doids = setOf(doid), primaryTumorType = type, primaryTumorExtraDetails = extraDetails))
    }

    fun withDoids(doids: Set<String>?): PatientRecord {
        return withTumorDetails(TumorDetails(doids = doids))
    }

    fun withTumorStage(stage: TumorStage?): PatientRecord {
        return withTumorDetails(TumorDetails(stage = stage))
    }

    fun withTumorStageAndDoid(stage: TumorStage?, doid: String?): PatientRecord {
        val doids = doid?.let(::setOf)
        return withTumorDetails(TumorDetails(stage = stage, doids = doids))
    }

    fun withMeasurableDisease(hasMeasurableDisease: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasMeasurableDisease = hasMeasurableDisease))
    }

    fun withMeasurableDiseaseAndDoid(hasMeasurableDisease: Boolean?, doid: String): PatientRecord {
        return withTumorDetails(TumorDetails(hasMeasurableDisease = hasMeasurableDisease, doids = setOf(doid)))
    }

    fun withBrainAndCnsLesions(hasBrainLesions: Boolean?, hasCnsLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasBrainLesions = hasBrainLesions, hasCnsLesions = hasCnsLesions))
    }

    fun withActiveBrainAndCnsLesionStatus(
        hasBrainLesions: Boolean?,
        hasActiveBrainLesions: Boolean?, hasCnsLesions: Boolean?, hasActiveCnsLesions: Boolean?
    ): PatientRecord {
        return withTumorDetails(
            TumorDetails(
                hasBrainLesions = hasBrainLesions,
                hasActiveBrainLesions = hasActiveBrainLesions,
                hasCnsLesions = hasCnsLesions,
                hasActiveCnsLesions = hasActiveCnsLesions
            )
        )
    }

    fun withBrainLesions(hasBrainLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasBrainLesions = hasBrainLesions))
    }

    fun withBrainLesionStatus(hasBrainLesions: Boolean?, hasActiveBrainLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasBrainLesions = hasBrainLesions, hasActiveBrainLesions = hasActiveBrainLesions))
    }

    fun withCnsOrBrainLesionsAndOncologicalHistory(
        hasCnsLesions: Boolean?,
        hasBrainLesions: Boolean?,
        oncologicalHistoryEntry: TreatmentHistoryEntry
    ): PatientRecord {
        return base.copy(
            oncologicalHistory = listOf(oncologicalHistoryEntry),
            tumor = TumorDetails(hasCnsLesions = hasCnsLesions, hasBrainLesions = hasBrainLesions)
        )
    }

    fun withCnsLesions(hasCnsLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasCnsLesions = hasCnsLesions))
    }

    fun withBoneLesions(hasBoneLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasBoneLesions = hasBoneLesions))
    }

    fun withBoneAndLiverLesions(hasBoneLesions: Boolean?, hasLiverLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasBoneLesions = hasBoneLesions, hasLiverLesions = hasLiverLesions))
    }

    fun withBoneAndOtherLesions(hasBoneLesions: Boolean?, otherLesions: List<String>): PatientRecord {
        return withTumorDetails(TumorDetails(hasBoneLesions = hasBoneLesions, otherLesions = otherLesions))
    }

    fun withLiverAndOtherLesions(hasLiverLesions: Boolean?, otherLesions: List<String>): PatientRecord {
        return withTumorDetails(TumorDetails(hasLiverLesions = hasLiverLesions, otherLesions = otherLesions))
    }

    fun withLiverLesions(hasLiverLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasLiverLesions = hasLiverLesions))
    }

    fun withLungLesions(hasLungLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasLungLesions = hasLungLesions))
    }

    fun withLymphNodeLesions(hasLymphNodeLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasLymphNodeLesions = hasLymphNodeLesions))
    }

    fun withOtherLesions(otherLesions: List<String>?): PatientRecord {
        return withTumorDetails(TumorDetails(otherLesions = otherLesions))
    }

    fun withDoidsAndLiverLesions(doids: Set<String>?, hasLiverLesions: Boolean?): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(doids = doids, hasLiverLesions = hasLiverLesions)
        )
    }

    fun withTumorDetails(tumor: TumorDetails): PatientRecord {
        return base.copy(tumor = tumor)
    }

    fun withMolecularExperimentType(type: ExperimentType): PatientRecord {
        return base.copy(molecularHistory = MolecularHistory.fromInputs(listOf(baseMolecular.copy(type = type)), emptyList()))
    }

    fun withPriorMolecularTestsAndDoids(priorMolecularTests: List<PriorMolecularTest>, doids: Set<String>?): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(doids = doids),
            molecularHistory = MolecularHistory.fromInputs(listOf(baseMolecular), priorMolecularTests)

        )
    }
}