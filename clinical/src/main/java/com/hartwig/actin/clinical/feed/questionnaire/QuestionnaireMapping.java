package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import org.jetbrains.annotations.NotNull;

final class QuestionnaireMapping {

    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V1_3 = Maps.newHashMap();
    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V1_2 = Maps.newHashMap();
    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V1_1 = Maps.newHashMap();
    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V1_0 = Maps.newHashMap();
    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V0_2 = Maps.newHashMap();
    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V0_1 = Maps.newHashMap();

    static {
        KEYS_V1_3.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V1_3.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history");
        KEYS_V1_3.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V1_3.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, "Primary tumor location");
        KEYS_V1_3.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, "Primary tumor type");
        KEYS_V1_3.put(QuestionnaireKey.BIOPSY_LOCATION, "Biopsy location");
        KEYS_V1_3.put(QuestionnaireKey.STAGE, "Stage");
        KEYS_V1_3.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, "Measurable disease (RECIST)");
        KEYS_V1_3.put(QuestionnaireKey.HAS_CNS_LESIONS, "CNS lesions");
        KEYS_V1_3.put(QuestionnaireKey.HAS_BRAIN_LESIONS, "Brain lesions");
        KEYS_V1_3.put(QuestionnaireKey.HAS_BONE_LESIONS, "Bone lesions");
        KEYS_V1_3.put(QuestionnaireKey.HAS_LIVER_LESIONS, "Liver lesions");
        KEYS_V1_3.put(QuestionnaireKey.OTHER_LESIONS, "Other lesions (e.g. lymph node, pulmonal)");
        KEYS_V1_3.put(QuestionnaireKey.WHO_STATUS, "WHO status");
        KEYS_V1_3.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, "Unresolved toxicities grade => 2");
        KEYS_V1_3.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, "Significant current infection");
        KEYS_V1_3.put(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG, "Significant aberration on latest ECG");
        KEYS_V1_3.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, "Cancer-related complications (e.g. pleural effusion)");

        KEYS_V1_2.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V1_2.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history");
        KEYS_V1_2.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V1_2.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, "Primary tumor location");
        KEYS_V1_2.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, "Primary tumor type");
        KEYS_V1_2.put(QuestionnaireKey.BIOPSY_LOCATION, "Biopsy location");
        KEYS_V1_2.put(QuestionnaireKey.STAGE, "Stage");
        KEYS_V1_2.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, "Measurable disease (RECIST)");
        KEYS_V1_2.put(QuestionnaireKey.HAS_CNS_LESIONS, "CNS lesions");
        KEYS_V1_2.put(QuestionnaireKey.HAS_BRAIN_LESIONS, "Brain lesions");
        KEYS_V1_2.put(QuestionnaireKey.HAS_BONE_LESIONS, "Bone lesions");
        KEYS_V1_2.put(QuestionnaireKey.HAS_LIVER_LESIONS, "Liver lesions");
        KEYS_V1_2.put(QuestionnaireKey.OTHER_LESIONS, "Other lesions (e.g. lymph node, pulmonal)");
        KEYS_V1_2.put(QuestionnaireKey.WHO_STATUS, "WHO status");
        KEYS_V1_2.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, "Unresolved toxicities grade => 2");
        KEYS_V1_2.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, "Significant current infection");
        KEYS_V1_2.put(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG, "Significant aberration on latest ECG");
        KEYS_V1_2.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, "Cancer-related complications (e.g. pleural effusion)");

        KEYS_V1_1.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V1_1.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history");
        KEYS_V1_1.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V1_1.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, "Primary tumor location");
        KEYS_V1_1.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, "Primary tumor type");
        KEYS_V1_1.put(QuestionnaireKey.BIOPSY_LOCATION, "Biopsy location");
        KEYS_V1_1.put(QuestionnaireKey.STAGE, "Stage");
        KEYS_V1_1.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, "Measurable disease (RECIST) yes/no");
        KEYS_V1_1.put(QuestionnaireKey.HAS_CNS_LESIONS, "CNS lesions yes/no/unknown");
        KEYS_V1_1.put(QuestionnaireKey.HAS_BRAIN_LESIONS, "Brain lesions yes/no/unknown");
        KEYS_V1_1.put(QuestionnaireKey.HAS_BONE_LESIONS, "Bone lesions yes/no/unknown");
        KEYS_V1_1.put(QuestionnaireKey.HAS_LIVER_LESIONS, "Liver lesions yes/no/unknown");
        KEYS_V1_1.put(QuestionnaireKey.OTHER_LESIONS, "Other lesions (e.g. lymph node, pulmonal)");
        KEYS_V1_1.put(QuestionnaireKey.WHO_STATUS, "WHO status");
        KEYS_V1_1.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, "Unresolved toxicities grade => 2");
        KEYS_V1_1.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, "Significant current infection");
        KEYS_V1_1.put(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG, "Significant aberration on latest ECG");
        KEYS_V1_1.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, "Cancer-related complications (e.g. pleural effusion)");

        KEYS_V1_0.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V1_0.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history");
        KEYS_V1_0.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V1_0.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, "Primary tumor location");
        KEYS_V1_0.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, "Primary tumor type");
        KEYS_V1_0.put(QuestionnaireKey.BIOPSY_LOCATION, "Biopsy location");
        KEYS_V1_0.put(QuestionnaireKey.STAGE, "Stage");
        KEYS_V1_0.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, "Measurable disease (RECIST) yes/no");
        KEYS_V1_0.put(QuestionnaireKey.HAS_CNS_LESIONS, "CNS lesions yes/no/unknown");
        KEYS_V1_0.put(QuestionnaireKey.HAS_BRAIN_LESIONS, "Brain lesions yes/no/unknown");
        KEYS_V1_0.put(QuestionnaireKey.HAS_BONE_LESIONS, "Bone lesions yes/no/unknown");
        KEYS_V1_0.put(QuestionnaireKey.HAS_LIVER_LESIONS, "Liver lesions yes/no/unknown");
        KEYS_V1_0.put(QuestionnaireKey.OTHER_LESIONS, "Other lesions (e.g. lymph node, pulmonal)");
        KEYS_V1_0.put(QuestionnaireKey.WHO_STATUS, "WHO status");
        KEYS_V1_0.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, "Unresolved toxicities grade => 2");
        KEYS_V1_0.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, "Significant current infection");
        KEYS_V1_0.put(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG, "Significant aberration on latest ECG");
        KEYS_V1_0.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, "Cancer-related complications (e.g. pleural effusion)");

        KEYS_V0_2.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V0_2.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history (e.g. second primary)");
        KEYS_V0_2.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V0_2.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, "Tumor location");
        KEYS_V0_2.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, "Tumor type");
        KEYS_V0_2.put(QuestionnaireKey.BIOPSY_LOCATION, "Biopsy location");
        KEYS_V0_2.put(QuestionnaireKey.STAGE, "Stage (I/II/III/IV)");
        KEYS_V0_2.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, "Measurable disease (RECIST) yes/no/unknown");
        KEYS_V0_2.put(QuestionnaireKey.HAS_CNS_LESIONS, "CNS lesions yes/no/unknown");
        KEYS_V0_2.put(QuestionnaireKey.HAS_BRAIN_LESIONS, "Brain lesions yes/no/unknown");
        KEYS_V0_2.put(QuestionnaireKey.HAS_BONE_LESIONS, "Bone lesions yes/no/unknown");
        KEYS_V0_2.put(QuestionnaireKey.HAS_LIVER_LESIONS, "Liver lesions yes/no/unknown");
        KEYS_V0_2.put(QuestionnaireKey.OTHER_LESIONS, null);
        KEYS_V0_2.put(QuestionnaireKey.WHO_STATUS, "WHO status");
        KEYS_V0_2.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, "Unresolved toxicities grade => 2");
        KEYS_V0_2.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, "Significant current infection");
        KEYS_V0_2.put(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG, "Significant aberration on latest ECG");
        KEYS_V0_2.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, "Other (e.g. pleural effusion)");

        KEYS_V0_1.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, null);
        KEYS_V0_1.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, null);
        KEYS_V0_1.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Other");
        KEYS_V0_1.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, "Oncological");
        KEYS_V0_1.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, null);
        KEYS_V0_1.put(QuestionnaireKey.BIOPSY_LOCATION, null);
        KEYS_V0_1.put(QuestionnaireKey.STAGE, null);
        KEYS_V0_1.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, "Has measurable lesion (RECIST) yes/no");
        KEYS_V0_1.put(QuestionnaireKey.HAS_CNS_LESIONS, "CNS lesions yes/no/unknown");
        KEYS_V0_1.put(QuestionnaireKey.HAS_BRAIN_LESIONS, "Brain lesions yes/no/unknown");
        KEYS_V0_1.put(QuestionnaireKey.HAS_BONE_LESIONS, "Bone lesions yes/no/unknown");
        KEYS_V0_1.put(QuestionnaireKey.HAS_LIVER_LESIONS, "Liver lesions yes/no/unknown");
        KEYS_V0_1.put(QuestionnaireKey.OTHER_LESIONS, null);
        KEYS_V0_1.put(QuestionnaireKey.WHO_STATUS, "WHO status");
        KEYS_V0_1.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, "Unresolved toxicities from prior anti-tumor therapy grade => 2");
        KEYS_V0_1.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, "Significant current infection");
        KEYS_V0_1.put(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG, "Significant aberration on latest ECG");
        KEYS_V0_1.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, "Other (e.g. Osteoporosis, Pleural effusion)");
    }

    @NotNull
    public static Map<QuestionnaireKey, String> mapping(@NotNull QuestionnaireEntry entry) {
        QuestionnaireVersion version = QuestionnaireVersion.version(entry);
        switch (version) {
            case V1_3:
                return KEYS_V1_3;
            case V1_2:
                return KEYS_V1_2;
            case V1_1:
                return KEYS_V1_1;
            case V1_0:
                return KEYS_V1_0;
            case V0_2:
                return KEYS_V0_2;
            case V0_1:
                return KEYS_V0_1;
            default:
                throw new IllegalStateException("Questionnaire version not supported for mapping: " + version);
        }
    }
}
