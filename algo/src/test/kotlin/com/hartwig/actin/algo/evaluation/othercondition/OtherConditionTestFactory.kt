package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.Toxicity
import org.apache.logging.log4j.util.Strings

internal object OtherConditionTestFactory {
    fun withPriorOtherCondition(condition: PriorOtherCondition): PatientRecord {
        return withPriorOtherConditions(listOf(condition))
    }

    fun withPriorOtherConditions(conditions: List<PriorOtherCondition>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .priorOtherConditions(conditions)
                    .build()
            )
            .build()
    }

    fun builder(): ImmutablePriorOtherCondition.Builder {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(true)
    }

    fun withComplications(complications: List<Complication>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .complications(complications)
                    .build()
            )
            .build()
    }

    fun withToxicities(toxicities: List<Toxicity>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .toxicities(toxicities)
                    .build()
            )
            .build()
    }

    fun withIntolerances(intolerances: List<Intolerance>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .intolerances(intolerances)
                    .build()
            )
            .build()
    }

    fun withMedications(medications: List<Medication>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .medications(medications)
                    .build()
            )
            .build()
    }

    fun withWHO(who: Int?): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder()
            .from(base)
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(base.clinical())
                    .clinicalStatus(ImmutableClinicalStatus.builder().from(base.clinical().clinicalStatus()).who(who).build())
                    .build()
            )
            .build()
    }
}