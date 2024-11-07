package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType

internal object TumorTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val baseMolecular = TestMolecularFactory.createMinimalTestMolecularRecord()

    fun withDoids(vararg doids: String): PatientRecord {
        return withDoids(setOf(*doids))
    }

    fun withDoidsAndAmplification(doids: Set<String>, amplifiedGene: String): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(doids = doids),
            molecularHistory = MolecularHistory(
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
                )
            )
        )
    }

    fun withDoidsAndAmplificationAndPriorMolecularTest(
        doids: Set<String>,
        amplifiedGene: String,
        priorIHCTests: List<PriorIHCTest>
    ): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(doids = doids),
            priorIHCTests = priorIHCTests,
            molecularHistory = MolecularHistory(
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

    fun withDoidAndTypeAndDetails(doid: String, type: String, extraDetails: String): PatientRecord {
        return withTumorDetails(TumorDetails(doids = setOf(doid), primaryTumorType = type, primaryTumorExtraDetails = extraDetails))
    }

    fun withDoids(doids: Set<String>?): PatientRecord {
        return withTumorDetails(TumorDetails(doids = doids))
    }

    fun withTumorStage(stage: TumorStage?): PatientRecord {
        return withTumorDetails(TumorDetails(stage = stage))
    }

    fun withTumorStageAndDerivedStages(stage: TumorStage?, derivedStages: Set<TumorStage>? = null): PatientRecord {
        return withTumorDetails(TumorDetails(stage = stage, derivedStages = derivedStages))
    }

    fun withTumorStageAndDoid(stage: TumorStage?, doid: String?): PatientRecord {
        val doids = doid?.let(::setOf)
        return withTumorDetails(TumorDetails(stage = stage, doids = doids))
    }

    fun withTumorStageAndDerivedStagesAndDoid(stage: TumorStage?, derivedStages: Set<TumorStage>, doid: String?): PatientRecord {
        val doids = doid?.let(::setOf)
        return withTumorDetails(TumorDetails(stage = stage, derivedStages = derivedStages, doids = doids))
    }

    fun withMeasurableDisease(hasMeasurableDisease: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasMeasurableDisease = hasMeasurableDisease))
    }

    fun withMeasurableDiseaseAndDoid(hasMeasurableDisease: Boolean?, doid: String): PatientRecord {
        return withTumorDetails(TumorDetails(hasMeasurableDisease = hasMeasurableDisease, doids = setOf(doid)))
    }

    fun withSuspectedLesions(hasSuspectedLesions: Boolean): PatientRecord {
        val otherSuspectedLesions = if (hasSuspectedLesions) listOf("suspected lesion") else emptyList()
        return withTumorDetails(TumorDetails(otherSuspectedLesions = otherSuspectedLesions))
    }

    fun withBrainAndCnsLesions(
        hasBrainLesions: Boolean?,
        hasCnsLesions: Boolean?,
        hasSuspectedBrainLesions: Boolean? = false,
        hasSuspectedCnsLesions: Boolean? = false
    ): PatientRecord {
        return withTumorDetails(
            TumorDetails(
                hasBrainLesions = hasBrainLesions,
                hasCnsLesions = hasCnsLesions,
                hasSuspectedBrainLesions = hasSuspectedBrainLesions,
                hasSuspectedCnsLesions = hasSuspectedCnsLesions
            )
        )
    }

    fun withActiveBrainAndCnsLesionStatus(
        hasBrainLesions: Boolean?,
        hasActiveBrainLesions: Boolean?,
        hasCnsLesions: Boolean?,
        hasActiveCnsLesions: Boolean?,
        hasSuspectedBrainLesions: Boolean? = false,
        hasSuspectedCnsLesions: Boolean? = false
    ): PatientRecord {
        return withTumorDetails(
            TumorDetails(
                hasBrainLesions = hasBrainLesions,
                hasActiveBrainLesions = hasActiveBrainLesions,
                hasCnsLesions = hasCnsLesions,
                hasActiveCnsLesions = hasActiveCnsLesions,
                hasSuspectedBrainLesions = hasSuspectedBrainLesions,
                hasSuspectedCnsLesions = hasSuspectedCnsLesions
            )
        )
    }

    fun withBrainLesions(hasBrainLesions: Boolean?, hasSuspectedBrainLesions: Boolean? = false): PatientRecord {
        return withTumorDetails(TumorDetails(hasBrainLesions = hasBrainLesions, hasSuspectedBrainLesions = hasSuspectedBrainLesions))
    }

    fun withBrainLesionStatus(
        hasBrainLesions: Boolean?,
        hasActiveBrainLesions: Boolean?,
        hasSuspectedBrainLesions: Boolean? = false
    ): PatientRecord {
        return withTumorDetails(
            TumorDetails(
                hasBrainLesions = hasBrainLesions,
                hasActiveBrainLesions = hasActiveBrainLesions,
                hasSuspectedBrainLesions = hasSuspectedBrainLesions
            )
        )
    }

    fun withCnsOrBrainLesionsAndOncologicalHistory(
        hasCnsLesions: Boolean?,
        hasBrainLesions: Boolean?,
        oncologicalHistoryEntry: TreatmentHistoryEntry
    ): PatientRecord {
        return base.copy(
            oncologicalHistory = listOf(oncologicalHistoryEntry),
            tumor = TumorDetails(
                hasCnsLesions = hasCnsLesions,
                hasBrainLesions = hasBrainLesions,
            )
        )
    }

    fun withSuspectedCnsOrBrainLesionsAndOncologicalHistory(
        hasSuspectedCnsLesions: Boolean?,
        hasSuspectedBrainLesions: Boolean?,
        oncologicalHistoryEntry: TreatmentHistoryEntry
    ): PatientRecord {
        return base.copy(
            oncologicalHistory = listOf(oncologicalHistoryEntry),
            tumor = TumorDetails(
                hasCnsLesions = false,
                hasBrainLesions = false,
                hasSuspectedCnsLesions = hasSuspectedCnsLesions,
                hasSuspectedBrainLesions = hasSuspectedBrainLesions
            )
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

    fun withBoneAndSuspectedLiverLesions(hasBoneLesions: Boolean?, hasSuspectedLiverLesions: Boolean?): PatientRecord {
        return withTumorDetails(TumorDetails(hasBoneLesions = hasBoneLesions, hasSuspectedLiverLesions = hasSuspectedLiverLesions))
    }

    fun withBoneAndOtherLesions(hasBoneLesions: Boolean?, otherLesions: List<String>): PatientRecord {
        return withTumorDetails(TumorDetails(hasBoneLesions = hasBoneLesions, otherLesions = otherLesions))
    }

    fun withSuspectedBoneAndOtherLesions(hasSuspectedBoneLesions: Boolean?, otherLesions: List<String>?): PatientRecord {
        return withTumorDetails(TumorDetails(hasSuspectedBoneLesions = hasSuspectedBoneLesions, otherLesions = otherLesions))
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

    fun withLymphNodeLesions(hasLymphNodeLesions: Boolean?, hasSuspectedLymphNodeLesions: Boolean? = null): PatientRecord {
        return withTumorDetails(
            TumorDetails(
                hasLymphNodeLesions = hasLymphNodeLesions,
                hasSuspectedLymphNodeLesions = hasSuspectedLymphNodeLesions
            )
        )
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
        return base.copy(molecularHistory = MolecularHistory(listOf(baseMolecular.copy(experimentType = type))))
    }

    fun withIHCTestsAndDoids(priorIHCTests: List<PriorIHCTest>, doids: Set<String>?): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(doids = doids),
            priorIHCTests = priorIHCTests
        )
    }
}