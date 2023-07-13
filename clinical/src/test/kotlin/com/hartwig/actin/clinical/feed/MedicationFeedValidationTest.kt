package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.curation.ANATOMICAL
import com.hartwig.actin.clinical.curation.ATC_CODE
import com.hartwig.actin.clinical.curation.CHEMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL_SUBSTANCE
import com.hartwig.actin.clinical.curation.PHARMACOLOGICAL
import com.hartwig.actin.clinical.curation.THERAPEUTIC
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDate

const val INCORRECT_VALUE = "incorrect"

class MedicationFeedValidationTest {
    @Test
    fun shouldPassValidityCheckForTrialAtcCodes() {
        Assertions.assertThat(MedicationFeedValidation(TestAtcFactory.createMinimalAtcModel()).validate(medicationEntry("123"))).isEmpty()
    }

    @Test
    fun shouldPassValidityCheckIfAtcClassificationMatches() {
        Assertions.assertThat(
            isValid(
                medicationEntry(
                    code5ATCCode = ATC_CODE,
                    anatomicalMainGroupDisplay = ANATOMICAL,
                    therapeuticSubgroupDisplay = THERAPEUTIC,
                    pharmacologicalSubgroupDisplay = PHARMACOLOGICAL,
                    chemicalSubgroupDisplay = CHEMICAL,
                    code5ATCDisplay = CHEMICAL_SUBSTANCE,
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldFailValidityCheckIfAtcClassificationMatches() {

        val permutations = listOf(
            medicationEntry(
                code5ATCCode = ATC_CODE,
                anatomicalMainGroupDisplay = INCORRECT_VALUE,
                therapeuticSubgroupDisplay = THERAPEUTIC,
                pharmacologicalSubgroupDisplay = PHARMACOLOGICAL,
                chemicalSubgroupDisplay = CHEMICAL,
                code5ATCDisplay = CHEMICAL_SUBSTANCE,
            ),
            medicationEntry(
                code5ATCCode = ATC_CODE,
                anatomicalMainGroupDisplay = ANATOMICAL,
                therapeuticSubgroupDisplay = INCORRECT_VALUE,
                pharmacologicalSubgroupDisplay = PHARMACOLOGICAL,
                chemicalSubgroupDisplay = CHEMICAL,
                code5ATCDisplay = CHEMICAL_SUBSTANCE,
            ),
            medicationEntry(
                code5ATCCode = ATC_CODE,
                anatomicalMainGroupDisplay = ANATOMICAL,
                therapeuticSubgroupDisplay = THERAPEUTIC,
                pharmacologicalSubgroupDisplay = INCORRECT_VALUE,
                chemicalSubgroupDisplay = CHEMICAL,
                code5ATCDisplay = CHEMICAL_SUBSTANCE,
            ),
            medicationEntry(
                code5ATCCode = ATC_CODE,
                anatomicalMainGroupDisplay = ANATOMICAL,
                therapeuticSubgroupDisplay = THERAPEUTIC,
                pharmacologicalSubgroupDisplay = PHARMACOLOGICAL,
                chemicalSubgroupDisplay = INCORRECT_VALUE,
                code5ATCDisplay = CHEMICAL_SUBSTANCE,
            ),
            medicationEntry(
                code5ATCCode = ATC_CODE,
                anatomicalMainGroupDisplay = ANATOMICAL,
                therapeuticSubgroupDisplay = THERAPEUTIC,
                pharmacologicalSubgroupDisplay = PHARMACOLOGICAL,
                chemicalSubgroupDisplay = CHEMICAL,
                code5ATCDisplay = INCORRECT_VALUE,
            ),
        )
        permutations.forEach { Assertions.assertThat(isValid(it)).isFalse() }
    }

    private fun isValid(entry: MedicationEntry): Boolean {
        return MedicationFeedValidation(TestAtcFactory.createProperAtcModel()).validate(entry).isEmpty()
    }

    private fun medicationEntry(
        code5ATCCode: String,
        code5ATCDisplay: String = "",
        chemicalSubgroupDisplay: String = "",
        pharmacologicalSubgroupDisplay: String = "",
        therapeuticSubgroupDisplay: String = "",
        anatomicalMainGroupDisplay: String = ""
    ): MedicationEntry {
        return MedicationEntry(
            status = Strings.EMPTY,
            dosageInstructionText = Strings.EMPTY,
            periodOfUseValuePeriodStart = LocalDate.of(2023, 7, 13),
            periodOfUseValuePeriodEnd = LocalDate.of(2023, 7, 13),
            active = true,
            subject = Strings.EMPTY,
            codeText = Strings.EMPTY,
            code5ATCCode = code5ATCCode,
            code5ATCDisplay = code5ATCDisplay,
            chemicalSubgroupDisplay = chemicalSubgroupDisplay,
            pharmacologicalSubgroupDisplay = pharmacologicalSubgroupDisplay,
            therapeuticSubgroupDisplay = therapeuticSubgroupDisplay,
            anatomicalMainGroupDisplay = anatomicalMainGroupDisplay,
            dosageInstructionRouteDisplay = Strings.EMPTY,
            dosageInstructionDoseQuantityUnit = Strings.EMPTY,
            dosageInstructionDoseQuantityValue = 0.0,
            dosageInstructionFrequencyUnit = Strings.EMPTY,
            dosageInstructionFrequencyValue = 0.0,
            dosageInstructionMaxDosePerAdministration = 0.0,
            dosageInstructionPatientInstruction = Strings.EMPTY,
            dosageInstructionAsNeededDisplay = Strings.EMPTY,
            dosageInstructionPeriodBetweenDosagesUnit = Strings.EMPTY,
            dosageInstructionPeriodBetweenDosagesValue = 0.0,
            dosageDoseValue = Strings.EMPTY,
            dosageRateQuantityUnit = Strings.EMPTY,
            dosageDoseUnitDisplayOriginal = Strings.EMPTY,
            stopTypeDisplay = Strings.EMPTY
        )
    }
}