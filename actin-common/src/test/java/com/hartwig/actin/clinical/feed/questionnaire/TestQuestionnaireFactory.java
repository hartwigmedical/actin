package com.hartwig.actin.clinical.feed.questionnaire;

import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;

public final class TestQuestionnaireFactory {

    private TestQuestionnaireFactory() {
    }

    @NotNull
    public static QuestionnaireEntry createTestQuestionnaireEntry() {
        return ImmutableQuestionnaireEntry.builder()
                .subject("TEST-01-01-0001")
                .parentIdentifierValue("XX")
                .authoredDateTime(LocalDate.of(2020, 8, 28))
                .questionnaireQuestionnaireValue("A")
                .description("Description")
                .itemText("ItemText")
                .itemAnswerValueValueString(createTestQuestionnaireValueV1())
                .build();
    }

    @NotNull
    private static String createTestQuestionnaireValueV1() {
        return "ACTIN Questionnaire\n"
                + "Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. "
                + "Please make sure that these fields never contain non-anonimized data!\n" + "\n" + "Relevant clinical history\n"
                + "Treatment history current tumor:Resectie 2020, geen systemische behandeling\n"
                + " Other oncological history(e.g.second primary):NA\n" + "Non-oncological history: Migraine\n" + "\n" + "Tumor details\n"
                + "Tumor location:lung\n" + "Tumor type:small - cell carcinoma\n" + "Biopsy location:liver\n"
                + "Stage(I / II / III / IV):IV\n" + "CNS lesions yes / no / unknown:NO\n" + "Active? : \n" + "Symptomatic? : \n"
                + "Brain lesions yes / no / unknown:Unknown\n" + "Active? : \n" + "Symptomatic? : \n"
                + "Bone lesions yes / no / unknown:NO\n" + "Liver lesions yes / no / unknown:NO\n"
                + "Biopsy amenable yes / no / unknown:UNKNOWN\n" + "Measurable disease (RECIST) yes / no / unknown:YES\n" + "\n"
                + "Clinical information\n" + "WHO status:0\n" + "Unresolved toxicities grade =>2:NA\n"
                + "Significant current infection: NO\n" + "Significant aberration on latest ECG: UNKNOWN\n"
                + "Other(e.g.pleural effusion):chronisch diarree (ws ziektegerelateerd)";
    }
}
