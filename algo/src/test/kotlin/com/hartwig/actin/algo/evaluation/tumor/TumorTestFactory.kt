package com.hartwig.actin.algo.evaluation.tumor

import com.google.common.collect.Sets
import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory

internal object TumorTestFactory {
    fun builder(): ImmutableTumorDetails.Builder {
        return ImmutableTumorDetails.builder()
    }

    fun withDoids(vararg doids: String): PatientRecord {
        return withDoids(Sets.newHashSet(*doids))
    }

    fun withDoidsAndAmplification(doids: Set<String>, amplifiedGene: String): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder()
            .from(base)
            .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).tumor(builder().doids(doids).build()).build())
            .molecular(
                ImmutableMolecularRecord.builder()
                    .from(base.molecular())
                    .characteristics(
                        ImmutableMolecularCharacteristics.builder()
                            .from(base.molecular().characteristics())
                            .ploidy(2.0)
                            .build()
                    )
                    .drivers(
                        ImmutableMolecularDrivers.builder()
                            .from(base.molecular().drivers())
                            .addCopyNumbers(
                                TestCopyNumberFactory.builder()
                                    .isReportable(true)
                                    .gene(amplifiedGene)
                                    .geneRole(GeneRole.ONCO)
                                    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                                    .type(CopyNumberType.FULL_GAIN)
                                    .minCopies(20)
                                    .maxCopies(20)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    fun withDoidAndSubLocation(doid: String, primaryTumorSubLocation: String?): PatientRecord {
        return withTumorDetails(builder().addDoids(doid).primaryTumorSubLocation(primaryTumorSubLocation).build())
    }

    fun withDoidAndDetails(doid: String, extraDetails: String): PatientRecord {
        return withTumorDetails(builder().addDoids(doid).primaryTumorExtraDetails(extraDetails).build())
    }

    fun withDoids(doids: Set<String>?): PatientRecord {
        return withTumorDetails(builder().doids(doids).build())
    }

    fun withTumorStage(stage: TumorStage?): PatientRecord {
        return withTumorDetails(builder().stage(stage).build())
    }

    fun withTumorStageAndDoid(stage: TumorStage?, doid: String?): PatientRecord {
        var doids: Set<String>? = null
        if (doid != null) {
            doids = Sets.newHashSet(doid)
        }
        return withTumorDetails(builder().stage(stage).doids(doids).build())
    }

    fun withMeasurableDisease(hasMeasurableDisease: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasMeasurableDisease(hasMeasurableDisease).build())
    }

    fun withMeasurableDiseaseAndDoid(hasMeasurableDisease: Boolean?, doid: String): PatientRecord {
        return withTumorDetails(builder().hasMeasurableDisease(hasMeasurableDisease).addDoids(doid).build())
    }

    fun withBrainAndCnsLesions(hasBrainLesions: Boolean?, hasCnsLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasBrainLesions(hasBrainLesions).hasCnsLesions(hasCnsLesions).build())
    }

    fun withActiveBrainAndCnsLesionStatus(
        hasBrainLesions: Boolean?,
        hasActiveBrainLesions: Boolean?, hasCnsLesions: Boolean?, hasActiveCnsLesions: Boolean?
    ): PatientRecord {
        return withTumorDetails(
            builder().hasBrainLesions(hasBrainLesions)
                .hasActiveBrainLesions(hasActiveBrainLesions)
                .hasCnsLesions(hasCnsLesions)
                .hasActiveCnsLesions(hasActiveCnsLesions)
                .build()
        )
    }

    fun withBrainLesions(hasBrainLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasBrainLesions(hasBrainLesions).build())
    }

    fun withBrainLesionStatus(hasBrainLesions: Boolean?, hasActiveBrainLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasBrainLesions(hasBrainLesions).hasActiveBrainLesions(hasActiveBrainLesions).build())
    }

    fun withCnsLesions(hasCnsLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasCnsLesions(hasCnsLesions).build())
    }

    fun withBoneLesions(hasBoneLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasBoneLesions(hasBoneLesions).build())
    }

    fun withBoneAndLiverLesions(hasBoneLesions: Boolean?, hasLiverLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasBoneLesions(hasBoneLesions).hasLiverLesions(hasLiverLesions).build())
    }

    fun withBoneAndOtherLesions(hasBoneLesions: Boolean?, otherLesions: List<String>): PatientRecord {
        return withTumorDetails(builder().hasBoneLesions(hasBoneLesions).otherLesions(otherLesions).build())
    }

    fun withLiverLesions(hasLiverLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasLiverLesions(hasLiverLesions).build())
    }

    fun withLungLesions(hasLungLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasLungLesions(hasLungLesions).build())
    }

    fun withLymphNodeLesions(hasLymphNodeLesions: Boolean?): PatientRecord {
        return withTumorDetails(builder().hasLymphNodeLesions(hasLymphNodeLesions).build())
    }

    fun withOtherLesions(otherLesions: List<String>?): PatientRecord {
        return withTumorDetails(builder().otherLesions(otherLesions).build())
    }

    fun withTumorDetails(tumor: TumorDetails): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .tumor(tumor)
                    .build()
            )
            .build()
    }

    fun withMolecularExperimentType(type: ExperimentType): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder()
            .from(base)
            .molecular(ImmutableMolecularRecord.builder().from(base.molecular()).type(type).build())
            .build()
    }
}