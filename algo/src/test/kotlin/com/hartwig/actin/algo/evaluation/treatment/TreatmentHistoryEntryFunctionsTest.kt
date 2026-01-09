package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.evaluateIfDrugHadPDResponse
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import java.time.LocalDate

private val TARGET_DRUG_SET = setOf(Drug("Osimertinib", setOf(DrugType.EGFR_INHIBITOR), TreatmentCategory.TARGETED_THERAPY))
private val TARGET_DRUG_TREATMENT = drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR))
private val WRONG_DRUG_TREATMENT = drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
private val DRUG_TREATMENT_WITH_TARGET_CATEGORY = drugTreatment("Target therapy trial drug", TreatmentCategory.TARGETED_THERAPY, emptySet())
private val TRIAL_DRUG_TREATMENT_NO_CATEGORY = drugTreatment("Some trial drug", TreatmentCategory.TARGETED_THERAPY, emptySet())

@RunWith(Enclosed::class)
class TreatmentHistoryEntryFunctionsTest {

    class EvaluateIfDrugHadPDResponseTest {

        @Test
        fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when treatment history is empty`() {
            assertThat(evaluateIfDrugHadPDResponse(emptyList(), TARGET_DRUG_SET)).isEqualTo(
                TreatmentHistoryEntryFunctions.TreatmentHistoryEvaluation(
                    matchingDrugsWithPD = emptySet(),
                    matchingDrugs = emptySet(),
                    matchesWithUnclearPD = false,
                    possibleTrialMatch = false,
                    matchesWithToxicity = false
                )
            )
        }

        @Test
        fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when target drug set is empty`() {
            val treatmentHistory = treatmentHistoryEntry(treatments = setOf(TARGET_DRUG_TREATMENT))
            assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), emptySet())).isEqualTo(
                TreatmentHistoryEntryFunctions.TreatmentHistoryEvaluation(
                    matchingDrugsWithPD = emptySet(),
                    matchingDrugs = emptySet(),
                    matchesWithUnclearPD = false,
                    possibleTrialMatch = false,
                    matchesWithToxicity = false
                )
            )
        }

        @Test
        fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when target drugs not in history`() {
            val treatmentHistory = treatmentHistoryEntry(treatments = setOf(WRONG_DRUG_TREATMENT))
            assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), emptySet())).isEqualTo(
                TreatmentHistoryEntryFunctions.TreatmentHistoryEvaluation(
                    matchingDrugsWithPD = emptySet(),
                    matchingDrugs = emptySet(),
                    matchesWithUnclearPD = false,
                    possibleTrialMatch = false,
                    matchesWithToxicity = false
                )
            )
        }

        @Test
        fun `Should correctly return all matching drugs with PD as matchingDrugsWithPD`() {
            val treatmentHistory = treatmentHistoryEntry(
                treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT),
                stopReason = StopReason.PROGRESSIVE_DISEASE
            )
            assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).matchingDrugsWithPD).containsAll(
                TARGET_DRUG_TREATMENT.drugs
            )
        }

        @Test
        fun `Should return empty set for matchingDrugsWithPD if no PD occurred with any matching drug`() {
            val treatmentHistory = treatmentHistoryEntry(
                treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT),
                stopReason = StopReason.TOXICITY
            )
            assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).matchingDrugsWithPD).isEmpty()
        }

        @Test
        fun `Should return true for matchesWithUnclearPD if all matching drugs have stop reason null`() {
            val treatmentHistory = treatmentHistoryEntry(treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT), stopReason = null)
            assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).matchesWithUnclearPD).isTrue()
        }

        @Test
        fun `Should return true for possible trial match when target drug is trial and correct category`() {
            val treatmentHistory = treatmentHistoryEntry(treatments = setOf(DRUG_TREATMENT_WITH_TARGET_CATEGORY), isTrial = true)
            assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).possibleTrialMatch).isTrue
        }

        @Test
        fun `Should return true for possible trial match when target drug is trial and no category configured`() {
            val treatmentHistory = treatmentHistoryEntry(treatments = setOf(TRIAL_DRUG_TREATMENT_NO_CATEGORY), isTrial = true)
            assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).possibleTrialMatch).isTrue
        }

        @Test
        fun `Should return true for matchesWithToxicity when target drug has stop reason toxicity`() {
            val treatmentHistory = treatmentHistoryEntry(treatments = setOf(TARGET_DRUG_TREATMENT), stopReason = StopReason.TOXICITY)
            assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).matchesWithToxicity).isTrue
        }
    }

    class PortionOfTreatmentHistoryEntryMatchingPredicateTest {

        private val predicate: (Treatment) -> Boolean = { it.categories().contains(TreatmentCategory.CHEMOTHERAPY) }

        @Test
        fun `Should return unmodified entry for matching single-stage treatment`() {
            val entry = treatmentHistoryEntry(setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)))
            assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate)).isEqualTo(entry)
        }

        @Test
        fun `Should return single-stage entry with aggregated treatments and cycles for matching multi-stage treatment`() {
            val switchToTreatment = treatmentStage(drugTreatment("switch treatment", TreatmentCategory.CHEMOTHERAPY), cycles = 3)
            val maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY))
            val entry = treatmentHistoryEntry(
                setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
                switchToTreatments = listOf(switchToTreatment),
                maintenanceTreatment = maintenanceTreatment,
                numCycles = 2
            )
            assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
                .isEqualTo(
                    entry.copy(
                        treatments = entry.treatments + switchToTreatment.treatment + maintenanceTreatment.treatment,
                        treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                            cycles = 5, switchToTreatments = emptyList(), maintenanceTreatment = null
                        )
                    )
                )
        }

        @Test
        fun `Should override stop date in treatment details when later treatment stages do not match`() {
            val switchToTreatment = treatmentStage(
                drugTreatment("switch treatment", TreatmentCategory.TARGETED_THERAPY), startYear = 2020, startMonth = 1, cycles = 3
            )
            val entry = treatmentHistoryEntry(
                setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
                switchToTreatments = listOf(switchToTreatment),
                maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT)),
                numCycles = 2
            )
            assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
                .isEqualTo(
                    entry.copy(
                        treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                            stopYear = switchToTreatment.startYear,
                            stopMonth = switchToTreatment.startMonth,
                            switchToTreatments = emptyList(),
                            maintenanceTreatment = null
                        )
                    )
                )
        }

        @Test
        fun `Should return single-stage entry with aggregated treatments and cycles for partially matching multi-stage treatment`() {
            val switchToTreatment = treatmentStage(
                drugTreatment("switch treatment", TreatmentCategory.CHEMOTHERAPY), startYear = 2020, startMonth = 1, cycles = 3
            )
            val maintenanceTreatment = treatmentStage(
                drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT), startYear = 2021, startMonth = 4
            )
            val entry = treatmentHistoryEntry(
                setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
                switchToTreatments = listOf(switchToTreatment),
                maintenanceTreatment = maintenanceTreatment,
                numCycles = 2,
                stopYear = 2021,
                stopMonth = 10
            )
            assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
                .isEqualTo(
                    entry.copy(
                        treatments = entry.treatments + switchToTreatment.treatment,
                        treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                            cycles = 5,
                            switchToTreatments = emptyList(),
                            maintenanceTreatment = null,
                            stopYear = maintenanceTreatment.startYear,
                            stopMonth = maintenanceTreatment.startMonth
                        )
                    )
                )
        }

        @Test
        fun `Should not alter stop date when intermediate stage does not match`() {
            val switchToTreatment = treatmentStage(
                drugTreatment("switch treatment", TreatmentCategory.TARGETED_THERAPY), startYear = 2020, startMonth = 1, cycles = 3
            )
            val maintenanceTreatment = treatmentStage(
                drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY), startYear = 2021, startMonth = 4
            )
            val entry = treatmentHistoryEntry(
                setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
                switchToTreatments = listOf(switchToTreatment),
                maintenanceTreatment = maintenanceTreatment,
                numCycles = 2,
                stopYear = 2021,
                stopMonth = 10
            )
            assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
                .isEqualTo(
                    entry.copy(
                        treatments = entry.treatments + maintenanceTreatment.treatment,
                        treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                            cycles = 2,
                            switchToTreatments = emptyList(),
                            maintenanceTreatment = null
                        )
                    )
                )
        }

        @Test
        fun `Should return entry representing matching stages when base treatment does not match`() {
            val switchToTreatment = treatmentStage(
                drugTreatment("switch treatment", TreatmentCategory.CHEMOTHERAPY), startYear = 2020, startMonth = 1, cycles = 3
            )
            val maintenanceTreatment = treatmentStage(
                drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT), startYear = 2021, startMonth = 4
            )
            val entry = treatmentHistoryEntry(
                setOf(drugTreatment("test treatment", TreatmentCategory.TARGETED_THERAPY)),
                switchToTreatments = listOf(switchToTreatment),
                maintenanceTreatment = maintenanceTreatment,
                numCycles = 2,
                startYear = 2019,
                startMonth = 8,
                stopYear = 2021,
                stopMonth = 10
            )
            assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
                .isEqualTo(
                    entry.copy(
                        treatments = setOf(switchToTreatment.treatment),
                        startYear = switchToTreatment.startYear,
                        startMonth = switchToTreatment.startMonth,
                        treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                            cycles = 3,
                            switchToTreatments = emptyList(),
                            maintenanceTreatment = null,
                            stopYear = maintenanceTreatment.startYear,
                            stopMonth = maintenanceTreatment.startMonth,
                        )
                    )
                )
        }

        @Test
        fun `Should return null when no stages of entry match`() {
            val switchToTreatment = treatmentStage(
                drugTreatment("switch treatment", TreatmentCategory.TARGETED_THERAPY)
            )
            val maintenanceTreatment = treatmentStage(
                drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT)
            )
            val entry = treatmentHistoryEntry(
                setOf(drugTreatment("test treatment", TreatmentCategory.TARGETED_THERAPY)),
                switchToTreatments = listOf(switchToTreatment),
                maintenanceTreatment = maintenanceTreatment,
                numCycles = 2,
                stopYear = 2021,
                stopMonth = 10
            )
            assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate)).isNull()
        }
    }

    class FullTreatmentDisplayTest {

        @Test
        fun `Should display switch and maintenance treatments when present`() {
            val switchToTreatment = treatmentStage(drugTreatment("switch treatment", TreatmentCategory.CHEMOTHERAPY), cycles = 3)
            val maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY))
            val entry = treatmentHistoryEntry(
                setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
                switchToTreatments = listOf(switchToTreatment),
                maintenanceTreatment = maintenanceTreatment,
                numCycles = 2
            )
            assertThat(TreatmentHistoryEntryFunctions.fullTreatmentDisplay(entry)).isEqualTo(
                "Test treatment with switch to Switch treatment continued with Maintenance treatment maintenance"
            )
        }

        @Test
        fun `Should display base treatment when no switch and maintenance treatments present`() {
            val entry = treatmentHistoryEntry(setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)), numCycles = 2)
            assertThat(TreatmentHistoryEntryFunctions.fullTreatmentDisplay(entry)).isEqualTo("Test treatment")
        }
    }

    class EvaluateTreatmentTimingRelativeToNextLineTest {
        private val maxMonthsBeforeNextLine = 3
        private val referenceDate = LocalDate.of(2025, 12, 1)

        @Test
        fun `Should return empty list when treatment history is empty`() {
            assertThat(
                TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
                    history = emptyList(),
                    maxMonthsBeforeNextLine = maxMonthsBeforeNextLine,
                    referenceDate = referenceDate
                ).isEmpty()
            )
        }

        @Test
        fun `Should mark treatment as WITHIN when stopped less than max months before next line starts`() {
            val history = listOf(
                treatmentHistoryEntry(
                    treatments = setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - (maxMonthsBeforeNextLine - 1)
                ),
                treatmentHistoryEntry(
                    treatments = setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )

            assertThat(TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
                history,
                maxMonthsBeforeNextLine,
                referenceDate
            ).first().timing).isEqualTo(TreatmentHistoryEntryFunctions.TreatmentTiming.WITHIN)
        }

        @Test
        fun `Should mark treatment as WITHIN when stopped equal to max months before next line starts`() {
            val history = listOf(
                treatmentHistoryEntry(
                    treatments = setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - maxMonthsBeforeNextLine
                ),
                treatmentHistoryEntry(
                    treatments = setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )

            assertThat(TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
                history,
                maxMonthsBeforeNextLine,
                referenceDate
            ).first().timing).isEqualTo(TreatmentHistoryEntryFunctions.TreatmentTiming.WITHIN)
        }

        @Test
        fun `Should mark treatment as WITHIN when no next line present and stopped less than max months before reference date`() {
            val history = listOf(
                treatmentHistoryEntry(
                    treatments = setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - 2
                )
            )

            assertThat(TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
                history,
                maxMonthsBeforeNextLine,
                referenceDate
            ).first().timing).isEqualTo(TreatmentHistoryEntryFunctions.TreatmentTiming.WITHIN)
        }

        @Test
        fun `Should mark treatment as OUTSIDE when stopped more than max months before next line start`() {
            val history = listOf(
                treatmentHistoryEntry(
                    treatments = setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - (maxMonthsBeforeNextLine + 1)
                ),
                treatmentHistoryEntry(
                    treatments = setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )

            assertThat(TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
                history,
                maxMonthsBeforeNextLine,
                referenceDate
            ).first().timing).isEqualTo(TreatmentHistoryEntryFunctions.TreatmentTiming.OUTSIDE)
        }

        @Test
        fun `Should mark treatment as OUTSIDE when stopped more than max months before reference date and no next line present`() {
            val history = listOf(
                treatmentHistoryEntry(
                    treatments = setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - (maxMonthsBeforeNextLine + 1)
                )
            )

            assertThat(TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
                history,
                maxMonthsBeforeNextLine,
                referenceDate
            ).first().timing).isEqualTo(TreatmentHistoryEntryFunctions.TreatmentTiming.OUTSIDE)
        }

//        @Test
//        fun `Should mark treatment as AMBIGUOUS when stop month is missing and date range overlaps threshold`() {
//            val history = listOf(
//                TreatmentTestFactory.treatmentHistoryEntry(
//                    treatments = setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
//                    stopYear = referenceDate.year,
//                    stopMonth = null
//                ),
//                TreatmentTestFactory.treatmentHistoryEntry(
//                    treatments = setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
//                    startYear = referenceDate.year,
//                    startMonth = referenceDate.monthValue
//                )
//            )
//
//            val result = TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
//                history,
//                MAX_MONTHS_BEFORE_NEXT_LINE,
//                referenceDate
//            )
//
//            assert(result.first().timing == TreatmentHistoryEntryFunctions.TreatmentTiming.AMBIGUOUS)
//        }
//
//        @Test
//        fun `Should mark treatment as UNKNOWN when both start and stop dates are missing`() {
//            val history = listOf(
//                TreatmentTestFactory.treatmentHistoryEntry(
//                    treatments = setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
//                    startYear = null,
//                    startMonth = null,
//                    stopYear = null,
//                    stopMonth = null
//                ),
//                TreatmentTestFactory.treatmentHistoryEntry(
//                    treatments = setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
//                    startYear = referenceDate.year,
//                    startMonth = referenceDate.monthValue
//                )
//            )
//
//            val result = TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
//                history,
//                MAX_MONTHS_BEFORE_NEXT_LINE,
//                referenceDate
//            )
//
//            assert(result.first().timing == TreatmentHistoryEntryFunctions.TreatmentTiming.UNKNOWN)
//        }
//
//        @Test
//        fun `Should preserve chronological order after sorting by start date`() {
//            val older = TreatmentTestFactory.treatmentHistoryEntry(
//                treatments = setOf(TreatmentTestFactory.treatment("older", isSystemic = true)),
//                startYear = referenceDate.year - 2
//            )
//            val newer = TreatmentTestFactory.treatmentHistoryEntry(
//                treatments = setOf(TreatmentTestFactory.treatment("newer", isSystemic = true)),
//                startYear = referenceDate.year
//            )
//
//            val history = listOf(newer, older)
//
//            val result = TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
//                history,
//                MAX_MONTHS_BEFORE_NEXT_LINE,
//                referenceDate
//            )
//
//            assert(result.first().entry == older)
//            assert(result.last().entry == newer)
//        }
    }
}