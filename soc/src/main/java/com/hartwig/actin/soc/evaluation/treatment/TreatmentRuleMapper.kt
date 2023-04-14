package com.hartwig.actin.soc.evaluation.treatment

import com.google.common.collect.Maps

class TreatmentRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        val map: MutableMap<EligibilityRule, FunctionCreator> = Maps.newHashMap()
        map[EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT] = isEligibleForCurativeTreatmentCreator
        map[EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS] = hasExhaustedSOCTreatmentsCreator()
        map[EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X] = isEligibleForOnLabelTreatmentCreator
        map[EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES] = hasHadSomeApprovedTreatmentCreator()
        map[EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES] = hasHadSomeSystemicTreatmentCreator()
        map[EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES] = hasHadLimitedSystemicTreatmentsCreator()
        map[EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT] = hasHadAnyCancerTreatmentCreator()
        map[EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORIES_WITH_NAME_X] = hasHadAnyCancerTreatmentIgnoringCategoriesAndNamesCreator()
        map[EligibilityRule.HAS_HAD_TREATMENT_NAME_X] = hasHadSpecificTreatmentCreator()
        map[EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS] = hasHadSpecificTreatmentWithinWeeksCreator()
        map[EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS] = hasHadCombinedTreatmentNamesWithinWeeksCreator()
        map[EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES] = hasHadCombinedTreatmentNamesWithCyclesCreator()
        map[EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT] = hasHadTreatmentWithCategoryCreator()
        map[EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y] = hasHadTreatmentCategoryOfTypesCreator()
        map[EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y] = hasHadFirstLineTreatmentCategoryOfTypesCreator()
        map[EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS] = hasHadTreatmentCategoryOfTypesWithinWeeksCreator()
        map[EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y] = hasHadTreatmentCategoryIgnoringTypesCreator()
        map[EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES] = hasHadSomeTreatmentsOfCategoryCreator()
        map[EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES] = hasHadLimitedTreatmentsOfCategoryCreator()
        map[EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES] = hasHadSomeTreatmentsOfCategoryWithTypesCreator()
        map[EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES] = hasHadLimitedTreatmentsOfCategoryWithTypesCreator()
        map[EligibilityRule.HAS_RECEIVED_HER2_TARGETING_ADC] = hasReceivedHER2TargetingADCCreator()
        map[EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT] = hasProgressiveDiseaseFollowingTreatmentNameCreator()
        map[EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT] = hasProgressiveDiseaseFollowingTreatmentCategoryCreator()
        map[EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y] = hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator()
        map[EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS] = hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator()
        map[EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES] = hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator()
        map[EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES] = hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator()
        map[EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES] = hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator()
        map[EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE] = hasRadiologicalProgressionFollowingLatestTreatmentLineCreator()
        map[EligibilityRule.HAS_HAD_COMPLETE_RESECTION] = hasHadCompleteResectionCreator()
        map[EligibilityRule.HAS_HAD_PARTIAL_RESECTION] = hasHadPartialResectionCreator()
        map[EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS] = hasHadResectionWithinWeeksCreator()
        map[EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS] = hasHadLocalHepaticTherapyWithinWeeksCreator()
        map[EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT] = hasHadIntratumoralInjectionTreatmentCreator()
        map[EligibilityRule.HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT] = hasLimitedCumulativeAnthracyclineExposureCreator()
        map[EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_CURRENT_TRIAL] = hasPreviouslyParticipatedInCurrentTrialCreator()
        map[EligibilityRule.IS_PARTICIPATING_IN_ANOTHER_TRIAL] = participatesInAnotherTrialCreator()
        return map
    }

    private val isEligibleForCurativeTreatmentCreator: FunctionCreator
        private get() = FunctionCreator { function -> IsEligibleForCurativeTreatment() }

    private fun hasExhaustedSOCTreatmentsCreator(): FunctionCreator {
        return FunctionCreator { function -> HasExhaustedSOCTreatments() }
    }

    private val isEligibleForOnLabelTreatmentCreator: FunctionCreator
        private get() = FunctionCreator { function -> IsEligibleForOnLabelTreatment() }

    private fun hasHadSomeApprovedTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val minApprovedTreatments: Int = functionInputResolver().createOneIntegerInput(function)
            HasHadSomeApprovedTreatments(minApprovedTreatments)
        }
    }

    private fun hasHadSomeSystemicTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val minSystemicTreatments: Int = functionInputResolver().createOneIntegerInput(function)
            HasHadSomeSystemicTreatments(minSystemicTreatments)
        }
    }

    private fun hasHadLimitedSystemicTreatmentsCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val maxSystemicTreatments: Int = functionInputResolver().createOneIntegerInput(function)
            HasHadLimitedSystemicTreatments(maxSystemicTreatments)
        }
    }

    private fun hasHadAnyCancerTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHadAnyCancerTreatment() }
    }

    private fun hasHadAnyCancerTreatmentIgnoringCategoriesAndNamesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHadAnyCancerTreatmentIgnoringCategoriesAndNames() }
    }

    private fun hasHadSpecificTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val treatment: String = functionInputResolver().createOneStringInput(function)
            HasHadSomeSpecificTreatments(Sets.newHashSet(treatment), null, 1)
        }
    }

    private fun hasHadSpecificTreatmentWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneIntegerOneString = functionInputResolver().createOneStringOneIntegerInput(function)
            val minDate: LocalDate = referenceDateProvider().date().minusWeeks(input.integer())
            HasHadSpecificTreatmentSinceDate(input.string(), minDate)
        }
    }

    private fun hasHadCombinedTreatmentNamesWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHadCombinedTreatmentNamesWithinWeeks() }
    }

    private fun hasHadCombinedTreatmentNamesWithCyclesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: TwoIntegersManyStrings = functionInputResolver().createManyStringsTwoIntegersInput(function)
            HasHadCombinedTreatmentNamesWithCycles(input.strings(), input.integer1(), input.integer2())
        }
    }

    private fun hasHadTreatmentWithCategoryCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val treatment: TreatmentInput = functionInputResolver().createOneTreatmentInput(function)
            if (treatment.mappedNames() == null) {
                return@FunctionCreator HasHadSomeTreatmentsWithCategory(treatment.mappedCategory(), 1)
            } else {
                return@FunctionCreator HasHadSomeSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), 1)
            }
        }
    }

    private fun hasHadTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTypedTreatmentManyStrings = functionInputResolver().createOneTypedTreatmentManyStringsInput(function)
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category(), input.strings(), 1)
        }
    }

    private fun hasHadFirstLineTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHadFirstLineTreatmentCategoryOfTypes() }
    }

    private fun hasHadTreatmentCategoryOfTypesWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTypedTreatmentManyStringsOneInteger = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function)
            val minDate: LocalDate = referenceDateProvider().date().minusWeeks(input.integer())
            HasHadTreatmentWithCategoryOfTypesRecently(input.category(), input.strings(), minDate)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringTypesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTypedTreatmentManyStrings = functionInputResolver().createOneTypedTreatmentManyStringsInput(function)
            HasHadTreatmentWithCategoryButNotOfTypes(input.category(), input.strings())
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTreatmentOneInteger = functionInputResolver().createOneTreatmentOneIntegerInput(function)
            val treatment: TreatmentInput = input.treatment()
            if (treatment.mappedNames() == null) {
                return@FunctionCreator HasHadSomeTreatmentsWithCategory(treatment.mappedCategory(), input.integer())
            } else {
                return@FunctionCreator HasHadSomeSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), input.integer())
            }
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTreatmentOneInteger = functionInputResolver().createOneTreatmentOneIntegerInput(function)
            val treatment: TreatmentInput = input.treatment()
            if (treatment.mappedNames() == null) {
                return@FunctionCreator HasHadLimitedTreatmentsWithCategory(treatment.mappedCategory(), input.integer())
            } else {
                return@FunctionCreator HasHadLimitedSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), input.integer())
            }
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTypedTreatmentManyStringsOneInteger = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function)
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category(), input.strings(), input.integer())
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTypedTreatmentManyStringsOneInteger = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function)
            HasHadLimitedTreatmentsWithCategoryOfTypes(input.category(), input.strings(), input.integer())
        }
    }

    private fun hasReceivedHER2TargetingADCCreator(): FunctionCreator {
        return FunctionCreator { function -> HasReceivedHER2TargetingADC() }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentNameCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val nameToFind: String = functionInputResolver().createOneStringInput(function)
            HasHadPDFollowingSpecificTreatment(Sets.newHashSet(nameToFind), null)
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentCategoryCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val treatment: TreatmentInput = functionInputResolver().createOneTreatmentInput(function)
            if (treatment.mappedNames() == null) {
                return@FunctionCreator HasHadPDFollowingTreatmentWithCategory(treatment.mappedCategory())
            } else {
                return@FunctionCreator HasHadPDFollowingSpecificTreatment(treatment.mappedNames(), treatment.mappedCategory())
            }
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTypedTreatmentManyStrings = functionInputResolver().createOneTypedTreatmentManyStringsInput(function)
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category(), input.strings(), null, null)
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTypedTreatmentManyStringsOneInteger = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function)
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category(),
                    input.strings(),
                    input.integer(),
                    null)
        }
    }

    //TODO: Check implementation
    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneTypedTreatmentManyStringsOneInteger = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function)
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category(),
                    input.strings(),
                    null,
                    input.integer())
        }
    }

    private fun hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val minSystemicTreatments: Int = functionInputResolver().createOneIntegerInput(function)
            HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, false)
        }
    }

    private fun hasRadiologicalProgressionFollowingLatestTreatmentLineCreator(): FunctionCreator {
        return FunctionCreator { function -> HasRadiologicalProgressionFollowingLatestTreatmentLine() }
    }

    //TODO: Check implementation
    private fun hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val minSystemicTreatments: Int = functionInputResolver().createOneIntegerInput(function)
            HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, true)
        }
    }

    private fun hasHadCompleteResectionCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHadCompleteResection() }
    }

    private fun hasHadPartialResectionCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHadPartialResection() }
    }

    private fun hasHadResectionWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val maxWeeksAgo: Int = functionInputResolver().createOneIntegerInput(function)
            val minDate: LocalDate = referenceDateProvider().date().minusWeeks(maxWeeksAgo)
            HasHadRecentResection(minDate)
        }
    }

    private fun hasHadLocalHepaticTherapyWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHadLocalHepaticTherapyWithinWeeks() }
    }

    private fun hasHadIntratumoralInjectionTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHadIntratumoralInjectionTreatment() }
    }

    private fun hasLimitedCumulativeAnthracyclineExposureCreator(): FunctionCreator {
        return FunctionCreator { function -> HasLimitedCumulativeAnthracyclineExposure(doidModel()) }
    }

    private fun hasPreviouslyParticipatedInCurrentTrialCreator(): FunctionCreator {
        return FunctionCreator { function -> HasPreviouslyParticipatedInCurrentTrial() }
    }

    private fun participatesInAnotherTrialCreator(): FunctionCreator {
        return FunctionCreator { function -> ParticipatesInAnotherTrial() }
    }
}