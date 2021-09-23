package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import org.jetbrains.annotations.NotNull;

final class QuestionnaireMapping {

    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V1_0B = Maps.newHashMap();
    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V1_0A = Maps.newHashMap();
    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V0 = Maps.newHashMap();

    static {
        KEYS_V1_0B.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V1_0B.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history");
        KEYS_V1_0B.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V1_0B.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, "Tumor location");
        KEYS_V1_0B.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, "Tumor type");
        KEYS_V1_0B.put(QuestionnaireKey.STAGE, "Stage");
        KEYS_V1_0B.put(QuestionnaireKey.HAS_CNS_LESIONS, "CNS lesions yes/no/unknown");
        KEYS_V1_0B.put(QuestionnaireKey.HAS_BRAIN_LESIONS, "Brain lesions yes/no/unknown");
        KEYS_V1_0B.put(QuestionnaireKey.HAS_BONE_LESIONS, "Bone lesions yes/no/unknown");
        KEYS_V1_0B.put(QuestionnaireKey.HAS_LIVER_LESIONS, "Liver lesions yes/no/unknown");
        KEYS_V1_0B.put(QuestionnaireKey.OTHER_LESIONS, "Other lesions (e.g. lymph node, pulmonal)");
        KEYS_V1_0B.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, "Measurable disease (RECIST) yes/no");
        KEYS_V1_0B.put(QuestionnaireKey.WHO_STATUS, "WHO status");
        KEYS_V1_0B.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, "Unresolved toxicities grade => 2");
        KEYS_V1_0B.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, "Significant current infection");
        KEYS_V1_0B.put(QuestionnaireKey.SIGNIFICANT_CURRENT_ABERRATION_LATEST_ECG, "Significant aberration on latest ECG");
        KEYS_V1_0B.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, "Cancer-related complications (e.g. pleural effusion)");

        KEYS_V1_0A.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V1_0A.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history");
        KEYS_V1_0A.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V1_0A.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, "Primary tumor location");
        KEYS_V1_0A.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, "Primary tumor type");
        KEYS_V1_0A.put(QuestionnaireKey.STAGE, "Stage");
        KEYS_V1_0A.put(QuestionnaireKey.HAS_CNS_LESIONS, "CNS lesions yes/no/unknown");
        KEYS_V1_0A.put(QuestionnaireKey.HAS_BRAIN_LESIONS, "Brain lesions yes/no/unknown");
        KEYS_V1_0A.put(QuestionnaireKey.HAS_BONE_LESIONS, "Bone lesions yes/no/unknown");
        KEYS_V1_0A.put(QuestionnaireKey.HAS_LIVER_LESIONS, "Liver lesions yes/no/unknown");
        KEYS_V1_0A.put(QuestionnaireKey.OTHER_LESIONS, "Other lesions (e.g. lymph node, pulmonal)");
        KEYS_V1_0A.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, "Measurable disease (RECIST) yes/no");
        KEYS_V1_0A.put(QuestionnaireKey.WHO_STATUS, "WHO status");
        KEYS_V1_0A.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, "Unresolved toxicities grade => 2");
        KEYS_V1_0A.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, "Significant current infection");
        KEYS_V1_0A.put(QuestionnaireKey.SIGNIFICANT_CURRENT_ABERRATION_LATEST_ECG, "Significant aberration on latest ECG");
        KEYS_V1_0A.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, "Cancer-related complications (e.g. pleural effusion)");

        KEYS_V0.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, null);
        KEYS_V0.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, null);
        KEYS_V0.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, null);
        KEYS_V0.put(QuestionnaireKey.PRIMARY_TUMOR_LOCATION, null);
        KEYS_V0.put(QuestionnaireKey.PRIMARY_TUMOR_TYPE, null);
        KEYS_V0.put(QuestionnaireKey.STAGE, null);
        KEYS_V0.put(QuestionnaireKey.HAS_CNS_LESIONS, null);
        KEYS_V0.put(QuestionnaireKey.HAS_BRAIN_LESIONS, null);
        KEYS_V0.put(QuestionnaireKey.HAS_BONE_LESIONS, null);
        KEYS_V0.put(QuestionnaireKey.HAS_LIVER_LESIONS, null);
        KEYS_V0.put(QuestionnaireKey.OTHER_LESIONS, null);
        KEYS_V0.put(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST, null);
        KEYS_V0.put(QuestionnaireKey.WHO_STATUS, null);
        KEYS_V0.put(QuestionnaireKey.UNRESOLVED_TOXICITIES, null);
        KEYS_V0.put(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION, null);
        KEYS_V0.put(QuestionnaireKey.SIGNIFICANT_CURRENT_ABERRATION_LATEST_ECG, null);
        KEYS_V0.put(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS, null);
    }

    @NotNull
    public static Map<QuestionnaireKey, String> mapping(@NotNull QuestionnaireEntry questionnaire) {
        QuestionnaireVersion version = QuestionnaireVersion.version(questionnaire);
        switch (version) {
            case V1_0B:
                return KEYS_V1_0B;
            case V1_0A:
                return KEYS_V1_0A;
            case V0:
                return KEYS_V0;
            default:
                throw new IllegalStateException("Questionnaire version not supported for mapping: " + version);
        }
    }
}
