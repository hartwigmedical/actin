package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.TumorStage;

final class QuestionnaireConstants {

    public static final Map<String, Boolean> OPTION_MAPPING = Maps.newHashMap();
    public static final Map<String, TumorStage> STAGE_MAPPING = Maps.newHashMap();

    static {
        OPTION_MAPPING.put("no", false);
        OPTION_MAPPING.put("yes", true);
        OPTION_MAPPING.put("n.v.t.", null);
        OPTION_MAPPING.put("nvt", null);
        OPTION_MAPPING.put("nvt.", null);
        OPTION_MAPPING.put("n.v.t", null);
        OPTION_MAPPING.put("unknown", null);
        OPTION_MAPPING.put("-", null);
        OPTION_MAPPING.put("na", null);

        STAGE_MAPPING.put("II", TumorStage.II);
        STAGE_MAPPING.put("2", TumorStage.II);
        STAGE_MAPPING.put("IIb", TumorStage.IIB);
        STAGE_MAPPING.put("III", TumorStage.III);
        STAGE_MAPPING.put("3", TumorStage.III);
        STAGE_MAPPING.put("IIIc", TumorStage.IIIC);
        STAGE_MAPPING.put("IV", TumorStage.IV);
        STAGE_MAPPING.put("4", TumorStage.IV);
    }

    private QuestionnaireConstants() {
    }
}
