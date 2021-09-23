package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import org.jetbrains.annotations.NotNull;

final class QuestionnaireMapping {

    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V0 = Maps.newHashMap();
    @VisibleForTesting
    static final Map<QuestionnaireKey, String> KEYS_V1 = Maps.newHashMap();

    static {
        KEYS_V0.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, null);
        KEYS_V0.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, null);
        KEYS_V0.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, null);
        KEYS_V0.put(QuestionnaireKey.TUMOR_LOCATION, null);
        KEYS_V0.put(QuestionnaireKey.TUMOR_TYPE, null);

        KEYS_V1.put(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR, "Treatment history current tumor");
        KEYS_V1.put(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY, "Other oncological history");
        KEYS_V1.put(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY, "Non-oncological history");
        KEYS_V1.put(QuestionnaireKey.TUMOR_LOCATION, "Tumor location");
        KEYS_V1.put(QuestionnaireKey.TUMOR_TYPE, "Tumor type");
    }

    @NotNull
    public static Map<QuestionnaireKey, String> mapping(@NotNull QuestionnaireEntry questionnaire) {
        QuestionnaireVersion version = QuestionnaireVersion.version(questionnaire);
        switch (version) {
            case V0:
                return KEYS_V0;
            case V1:
                return KEYS_V1;
            default:
                throw new IllegalStateException("Questionnaire version not supported for mapping: " + version);
        }
    }
}
