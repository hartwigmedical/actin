package com.hartwig.actin.clinical.feed.questionnaire;

import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

final class QuestionnaireUtil {

    private static final Logger LOGGER = LogManager.getLogger(QuestionnaireUtil.class);

    private QuestionnaireUtil() {
    }

    @Nullable
    public static TumorStage parseStage(@Nullable String stage) {
        if (stage == null || stage.isEmpty()) {
            return null;
        }

        switch (stage) {
            case "II":
            case "2":
                return TumorStage.II;
            case "IIb":
                return TumorStage.IIB;
            case "III":
            case "3":
                return TumorStage.III;
            case "IIIc":
                return TumorStage.IIIC;
            case "IV":
            case "4":
                return TumorStage.IV;
            default: {
                LOGGER.warn("Unrecognized questionnaire tumor stage: '{}'", stage);
                return null;
            }
        }
    }

    @Nullable
    public static Boolean parseOption(@Nullable String option) {
        if (option == null || option.isEmpty()) {
            return null;
        }

        switch (option.toLowerCase()) {
            case "no":
                return false;
            case "yes":
                return true;
            case "n.v.t.":
            case "nvt":
            case "nvt.":
            case "unknown":
            case "-":
                return null;
            default: {
                LOGGER.warn("Unrecognized questionnaire option: '{}'", option);
                return null;
            }
        }
    }
}
