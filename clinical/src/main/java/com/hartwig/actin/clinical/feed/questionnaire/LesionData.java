package com.hartwig.actin.clinical.feed.questionnaire;

import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toOption;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class LesionData {

    @Nullable
    private final Boolean present;
    @Nullable
    private final Boolean active;

    public LesionData(@Nullable final Boolean present, @Nullable final Boolean active) {
        this.present = present;
        this.active = active;
    }

    @NotNull
    static LesionData fromString(@NotNull String presentInput, @NotNull String activeInput) {
        Boolean present = toOption(presentInput);
        Boolean active = null;
        if (present != null) {
            Boolean activeOption = toOption(activeInput);
            if (activeOption != null) {
                active = present ? activeOption : false;
            }
        }

        return new LesionData(present, active);
    }

    @Nullable
    public Boolean present() {
        return present;
    }

    @Nullable
    public Boolean active() {
        return active;
    }
}
