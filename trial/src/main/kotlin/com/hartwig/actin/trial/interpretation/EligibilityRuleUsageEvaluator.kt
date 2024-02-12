package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.Trial
import com.hartwig.actin.trial.input.composite.CompositeRules
import org.apache.logging.log4j.LogManager

object EligibilityRuleUsageEvaluator {

    private val LOGGER = LogManager.getLogger(EligibilityRuleUsageEvaluator::class.java)

    private val UNUSED_RULES_TO_KEEP = setOf(
        EligibilityRule.HAS_LYMPHOCYTES_CELLS_PER_MM3_OF_AT_LEAST_X,
        EligibilityRule.HAS_HLA_TYPE_X,
        EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X,
        EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_CURRENT_TRIAL,
        EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS,
        EligibilityRule.HAS_UNRESECTABLE_STAGE_III_CANCER,
        EligibilityRule.HAS_PHOSPHORUS_ULN_OF_AT_MOST_X,
        EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY,
        EligibilityRule.HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT,
        EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X,
        EligibilityRule.HAS_POTENTIAL_HYPOKALEMIA,
        EligibilityRule.HAS_POTENTIAL_HYPOMAGNESEMIA,
        EligibilityRule.HAS_POTENTIAL_HYPOCALCEMIA,
        EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z,
        EligibilityRule.HAS_HAD_PARTIAL_RESECTION,
        EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_PROMOTER_OF_GENE_X,
        EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES,
        EligibilityRule.HAS_JTC_OF_AT_LEAST_X,
        EligibilityRule.HAS_SECONDARY_GLIOBLASTOMA,
        EligibilityRule.HAS_MRI_SCAN_DOCUMENTING_STABLE_DISEASE,
        EligibilityRule.HAS_EXTRACRANIAL_METASTASES,
        EligibilityRule.HAS_INTOLERANCE_TO_TAXANE,
        EligibilityRule.HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI,
        EligibilityRule.HAS_HAD_COMPLETE_RESECTION,
        EligibilityRule.MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY,
        EligibilityRule.PD_L1_STATUS_MUST_BE_AVAILABLE,
        EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR,
        EligibilityRule.HAS_LUNG_METASTASES,
        EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS,
        EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES,
        EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y,
        EligibilityRule.HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS,
        EligibilityRule.PROTEIN_X_IS_WILD_TYPE_BY_IHC,
        EligibilityRule.HAS_URINE_PROTEIN_TO_CREATININE_RATIO_MG_PER_MG_OF_AT_MOST_X,
        EligibilityRule.HAS_THYROXINE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
        EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION,
        EligibilityRule.HAS_KNOWN_TUBERCULOSIS_INFECTION,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_BCRP,
        EligibilityRule.HAS_ANY_COMPLICATION,
        EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X,
        EligibilityRule.HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA,
        EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_BRAIN_METASTASES,
        EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_LIVER_METASTASES,
        EligibilityRule.HAS_PRESENCE_OF_LESIONS_IN_AT_LEAST_X_SITES,
        EligibilityRule.HAS_INJECTION_AMENABLE_LESION,
        EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_LINES_X,
        EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT,
        EligibilityRule.HAS_PT_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
        EligibilityRule.HAS_APTT_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
        EligibilityRule.HAS_NON_SQUAMOUS_NSCLC,
        EligibilityRule.HAS_KNOWN_HEPATITIS_A_INFECTION,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_ANY_CYP,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AS_MOST_RECENT_LINE,
        EligibilityRule.HAS_UGT1A1_HAPLOTYPE_X,
        EligibilityRule.HAS_HOMOZYGOUS_DPYD_DEFICIENCY,
        EligibilityRule.HAS_UNRESECTABLE_PERITONEAL_METASTASES,
        EligibilityRule.NON_EXPRESSION_OF_GENE_X,
        EligibilityRule.HAS_HAD_CYTOREDUCTIVE_SURGERY,
        EligibilityRule.HAS_EXTENSIVE_SYSTEMIC_METASTASES_PREDOMINANTLY_DETERMINING_PROGNOSIS,
        EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X_WITHIN_Y_MONTHS,
        EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X,
        EligibilityRule.HAS_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS,
        EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_GENE_X,
        EligibilityRule.HAS_POTENTIAL_UNCONTROLLED_TUMOR_RELATED_PAIN,
        EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y,
        EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS,
        EligibilityRule.HAS_PHOSPHORUS_MMOL_PER_L_OF_AT_MOST_X,
        EligibilityRule.HAS_FAMILY_HISTORY_OF_LONG_QT_SYNDROME,
        EligibilityRule.HAS_IRRADIATION_AMENABLE_LESION,
        EligibilityRule.HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X,
        EligibilityRule.CURRENTLY_GETS_HERBAL_MEDICATION,
        EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORY_X,
        EligibilityRule.NSCLC_DRIVER_GENE_STATUSES_MUST_BE_AVAILABLE,
        EligibilityRule.HAS_RECEIVED_POTENTIAL_SYSTEMIC_TREATMENT_FOR_BRAIN_METASTASES,
        EligibilityRule.HAS_POTENTIAL_RESPIRATORY_COMPROMISE,
        EligibilityRule.PD_L1_SCORE_TPS_OF_AT_LEAST_X,
    )

    fun evaluate(trials: List<Trial>) {
        val usedRules = trials.flatMap {
            extractRules(it.generalEligibility) + it.cohorts.flatMap { cohort ->
                extractRules(cohort.eligibility)
            }
        }.toSet()
        val unusedRules = EligibilityRule.values().toSet() - usedRules

        val unusedRulesThatAreUsed = UNUSED_RULES_TO_KEEP - unusedRules
        if (unusedRulesThatAreUsed.isNotEmpty()) {
            LOGGER.warn(" Found {} eligibility rules that are used while they are configured as unused.", unusedRulesThatAreUsed.size)
            for (rule in unusedRulesThatAreUsed) {
                LOGGER.warn("  '{}' used in at least one trial or cohort but configured as unused", rule.toString())
            }
        }

        val unexpectedUnusedRules = unusedRules - UNUSED_RULES_TO_KEEP
        if (unexpectedUnusedRules.isNotEmpty()) {
            LOGGER.warn(" Found {} unused eligibility rules.", unexpectedUnusedRules.size)
            for (rule in unexpectedUnusedRules) {
                LOGGER.warn("  '{}' not used in any trial or cohort", rule.toString())
            }
        } else {
            LOGGER.info(" Found no unused eligibility rules to curate.")
        }
    }

    private fun extractRules(eligibilities: List<Eligibility>): Collection<EligibilityRule> {
        return eligibilities.flatMap { extract(listOf(it.function), emptyList()) }
    }

    private tailrec fun extract(functions: List<EligibilityFunction>, accumulated: List<EligibilityRule>): List<EligibilityRule> {
        if (functions.isEmpty()) {
            return accumulated
        }
        val function = functions.first()
        val functionsToAdd = if (CompositeRules.isComposite(function.rule)) {
            function.parameters.map { it as EligibilityFunction }
        } else emptyList()

        return extract(functionsToAdd + functions.drop(1), accumulated + function.rule)
    }
}