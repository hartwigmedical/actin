package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

internal object ToxicityTestFactory {
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

    fun withToxicityThatIsAlsoComplication(toxicity: Toxicity): PatientRecord {
        val complication: Complication? = ImmutableComplication.builder().name(toxicity.name()).build()
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .toxicities(listOf(toxicity))
                    .complications(listOf(complication))
                    .build()
            )
            .build()
    }

    fun toxicity(): ImmutableToxicity.Builder {
        return ImmutableToxicity.builder().name(Strings.EMPTY).evaluatedDate(LocalDate.of(2020, 1, 1)).source(ToxicitySource.EHR)
    }

    fun withIntolerance(intolerance: Intolerance): PatientRecord {
        return withIntolerances(listOf(intolerance))
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

    fun intolerance(): ImmutableIntolerance.Builder {
        return ImmutableIntolerance.builder()
            .name("")
            .category("")
            .type("")
            .clinicalStatus("")
            .verificationStatus("")
            .criticality("")
    }
}