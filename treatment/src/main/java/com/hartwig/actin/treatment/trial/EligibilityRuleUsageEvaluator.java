package com.hartwig.actin.treatment.trial;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.input.composite.CompositeRules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class EligibilityRuleUsageEvaluator {

    private static final Logger LOGGER = LogManager.getLogger(EligibilityRuleUsageEvaluator.class);

    private static final Set<EligibilityRule> UNUSED_RULES_TO_KEEP = Sets.newHashSet();

    static {
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_LYMPHOCYTES_CELLS_PER_MM3_OF_AT_LEAST_X);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_QTC_OF_AT_MOST_X);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_HLA_TYPE_X);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_CURRENT_TRIAL);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_UNRESECTABLE_STAGE_III_CANCER);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_PHOSPHORUS_ULN_OF_AT_MOST_X);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_POTENTIAL_HYPOKALEMIA);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_POTENTIAL_HYPOMAGNESEMIA);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_POTENTIAL_HYPOCALCEMIA);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_RAPIDLY_DETERIORATING_CONDITION);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_HAD_PARTIAL_RESECTION);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_PROMOTER_OF_GENE_X);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_JTC_OF_AT_LEAST_X);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_D_DIMER_OUTSIDE_REF_UPPER_LIMIT);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_SECONDARY_GLIOBLASTOMA);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_MRI_SCAN_DOCUMENTING_STABLE_DISEASE);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_EXTRACRANIAL_METASTASES);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_INTOLERANCE_TO_TAXANE);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_HAD_COMPLETE_RESECTION);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.PD_L1_STATUS_MUST_BE_AVAILABLE);
        UNUSED_RULES_TO_KEEP.add(EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR);
    }

    private EligibilityRuleUsageEvaluator() {
    }

    public static void evaluate(@NotNull List<Trial> trials) {
        Set<EligibilityRule> unusedRules = Sets.newHashSet(EligibilityRule.values());

        for (Trial trial : trials) {
            unusedRules.removeAll(extractRules(trial.generalEligibility()));

            for (Cohort cohort : trial.cohorts()) {
                unusedRules.removeAll(extractRules(cohort.eligibility()));
            }
        }

        Set<EligibilityRule> unusedRulesThatAreUsed = Sets.newHashSet();
        for (EligibilityRule rule : UNUSED_RULES_TO_KEEP) {
            if (!unusedRules.contains(rule)) {
                unusedRulesThatAreUsed.add(rule);
            }
        }

        if (!unusedRulesThatAreUsed.isEmpty()) {
            LOGGER.warn(" Found {} eligibility rules that are used while they are configured as unused.", unusedRulesThatAreUsed.size());
            for (EligibilityRule rule : unusedRulesThatAreUsed) {
                LOGGER.warn("  '{}' used in at least one trial or cohort but configured as unused", rule.toString());
            }
        }

        unusedRules.removeAll(UNUSED_RULES_TO_KEEP);
        if (!unusedRules.isEmpty()) {
            LOGGER.warn(" Found {} unused eligibility rules.", unusedRules.size());
            for (EligibilityRule rule : unusedRules) {
                LOGGER.warn("  '{}' not used in any trial or cohort", rule.toString());
            }
        } else {
            LOGGER.info(" Found no unused eligibility rules to curate.");
        }
    }

    @NotNull
    private static Collection<EligibilityRule> extractRules(@NotNull List<Eligibility> eligibilities) {
        Collection<EligibilityRule> rules = Lists.newArrayList();
        for (Eligibility eligibility : eligibilities) {
            rules.addAll(extract(eligibility.function()));
        }
        return rules;
    }

    @NotNull
    private static List<EligibilityRule> extract(@NotNull EligibilityFunction function) {
        List<EligibilityRule> rules = Lists.newArrayList(function.rule());
        if (CompositeRules.isComposite(function.rule())) {
            for (Object param : function.parameters()) {
                rules.addAll(extract((EligibilityFunction) param));
            }
        }
        return rules;
    }
}
