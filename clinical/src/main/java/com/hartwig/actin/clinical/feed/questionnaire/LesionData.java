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
        String[] tokens = input.split(".{0,2}Active.*:");
        Boolean present = tokens.length > 0 ? toOption(tokens[0].trim()) : null;
        Boolean active = null;
        if (present != null && tokens.length >= 2) {
            String remainingText = tokens[1].trim();
            String activeOptionText = remainingText.contains(" ") ? remainingText.substring(0, remainingText.indexOf(" ")) : remainingText;
            Boolean activeOption = toOption(activeOptionText);
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
