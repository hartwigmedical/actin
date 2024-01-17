package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory

internal object TumorTestFactory {
    private val base = TestDataFactory.createMinimalTestPatientRecord()
    
    fun withDoids(vararg doids: String): PatientRecord {
        return withDoids(setOf(*doids))
    }

    fun withDoidsAndAmplification(doids: Set<String>, amplifiedGene: String): PatientRecord {
        return base.copy(
            clinical = base.clinical.copy(tumor = base.clinical.tumor.copy(doids = doids)),
            molecular = base.molecular.copy(
                characteristics = base.molecular.characteristics.copy(ploidy = 2.0),
                drivers = base.molecular.drivers.copy(
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

    fun withTumorDetails(tumor: TumorDetails): PatientRecord {
        return base.copy(clinical = base.clinical.copy(tumor = tumor))
    }

    fun withMolecularExperimentType(type: ExperimentType): PatientRecord {
        return base.copy(molecular = base.molecular.copy(type = type))
    }
}