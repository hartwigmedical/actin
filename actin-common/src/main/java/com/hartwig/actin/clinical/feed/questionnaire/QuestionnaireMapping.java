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
        KEYS_V1_0B.put(QuestionnaireKey.TUMOR_LOCATION, "Tumor location");
        KEYS_V1_0B.put(QuestionnaireKey.TUMOR_TYPE, "Tumor type");

        KEYS_V1_0A.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V1_0A.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history");
        KEYS_V1_0A.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V1_0A.put(QuestionnaireKey.TUMOR_LOCATION, "Primary tumor location");
        KEYS_V1_0A.put(QuestionnaireKey.TUMOR_TYPE, "Primary tumor type");

        KEYS_V0.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, null);
        KEYS_V0.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, null);
        KEYS_V0.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, null);
        KEYS_V0.put(QuestionnaireKey.TUMOR_LOCATION, null);
        KEYS_V0.put(QuestionnaireKey.TUMOR_TYPE, null);
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
