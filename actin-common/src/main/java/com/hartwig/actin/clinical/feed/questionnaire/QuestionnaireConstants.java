package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TumorStage;

final class QuestionnaireConstants {

    public static final Map<String, Boolean> OPTION_MAPPING = Maps.newHashMap();
    public static final Map<String, TumorStage> STAGE_MAPPING = Maps.newHashMap();

    public static final Set<String> TERMS_TO_CLEAN = Sets.newHashSet();

    static {
        OPTION_MAPPING.put("no", false);
        OPTION_MAPPING.put("No", false);
        OPTION_MAPPING.put("NO", false);
        OPTION_MAPPING.put("n.v.t.", null);
        OPTION_MAPPING.put("n.v.t", null);
        OPTION_MAPPING.put("nvt", null);
        OPTION_MAPPING.put("nvt.", null);
        OPTION_MAPPING.put("NA", null);
        OPTION_MAPPING.put("yes", true);
        OPTION_MAPPING.put("Yes", true);
        OPTION_MAPPING.put("YES", true);
        OPTION_MAPPING.put("unknown", null);
        OPTION_MAPPING.put("Unknown", null);
        OPTION_MAPPING.put("UNKNOWN", null);
        OPTION_MAPPING.put("-", null);
        OPTION_MAPPING.put("yes/no", null);

        STAGE_MAPPING.put("II", TumorStage.II);
        STAGE_MAPPING.put("2", TumorStage.II);
        STAGE_MAPPING.put("IIb", TumorStage.IIB);
        STAGE_MAPPING.put("III", TumorStage.III);
        STAGE_MAPPING.put("3", TumorStage.III);
        STAGE_MAPPING.put("IIIc", TumorStage.IIIC);
        STAGE_MAPPING.put("IV", TumorStage.IV);
        STAGE_MAPPING.put("4", TumorStage.IV);

        TERMS_TO_CLEAN.add("{");
        TERMS_TO_CLEAN.add("}");

        TERMS_TO_CLEAN.add("\\tab");
        TERMS_TO_CLEAN.add("\\li0");
        TERMS_TO_CLEAN.add("\\ri0");
        TERMS_TO_CLEAN.add("\\sa0");
        TERMS_TO_CLEAN.add("\\sb0");
        TERMS_TO_CLEAN.add("\\fi0");
        TERMS_TO_CLEAN.add("\\ql");
        TERMS_TO_CLEAN.add("\\par");
        TERMS_TO_CLEAN.add("\\f2");
        TERMS_TO_CLEAN.add("\\ltrch");

    }

    private QuestionnaireConstants() {
    }
}
