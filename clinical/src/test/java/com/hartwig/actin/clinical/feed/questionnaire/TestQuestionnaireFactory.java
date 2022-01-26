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
                .authored(LocalDate.of(2020, 8, 28))
                .parentIdentifierValue("XX")
                .questionnaireQuestionnaireValue("A")
                .description("Description")
                .itemText("ItemText")
                .itemAnswerValueValueString(createTestQuestionnaireValueV1_3())
                .build();
    }

    @NotNull
    static String createTestQuestionnaireValueV1_3() {
        // @formatter:off
        return "ACTIN Questionnaire V1.0"
                + "Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. "
                + "Please make sure that these fields never contain non-anonymized data!\n"
                + "\n"
                + "Relevant patient history\n"
                + "Treatment history current tumor: cisplatin; nivolumab\n"
                + "Other oncological history (e.g. radiotherapy, second primary): surgery\n"
                + "Non-oncological history: diabetes\n"
                + "\n"
                + "Tumor details\n"
                + "Primary tumor location: ovary\n"
                + "Primary tumor type: serous\n"
                + "Biopsy location: Lymph node\n"
                + "Stage: 3\n"
                + "CNS lesions:\n"
                + "-Active:\n"
                + "-Symptomatic:\n"
                + "Brain lesions:\n"
                + "-Active:\n"
                + "-Symptomatic:\n"
                + "Bone lesions: NO\n"
                + "Liver lesions: NO\n"
                + "Other lesions (e.g. lymph node, pulmonal): pulmonal\n"
                + "Measurable disease (RECIST): YES\n"
                + "\n"
                + "Clinical details\n"
                + "WHO status: 0\n"
                + "Unresolved toxicities grade => 2:\n"
                + "Significant current infection: No\n"
                + "Significant aberration on latest ECG: Sinus\n"
                + "Cancer-related complications (e.g. pleural effusion): nausea";
        // @formatter:on
    }

    @NotNull
    static String createTestQuestionnaireValueV1_2() {
        // @formatter:off
        return "ACTIN Questionnaire V1.0"
                + "Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. "
                + "Please make sure that these fields never contain non-anonymized data!\n"
                + "\n"
                + "Relevant patient history\n"
                + "Treatment history current tumor: cisplatin; nivolumab\n"
                + "Other oncological history (e.g. radiotherapy, second primary): surgery\n"
                + "Non-oncological history: diabetes\n"
                + "\n"
                + "Tumor details\n"
                + "Primary tumor location: ovary\n"
                + "Primary tumor type: serous\n"
                + "Biopsy location: Lymph node\n"
                + "Stage: 3\n"
                + "CNS lesions yes/no/unknown:\n"
                + "-Active:\n"
                + "-Symptomatic:\n"
                + "Brain lesions yes/no/unknown:\n"
                + "-Active:\n"
                + "-Symptomatic:\n"
                + "Bone lesions yes/no/unknown: NO\n"
                + "Liver lesions yes/no/unknown: NO\n"
                + "Other lesions (e.g. lymph node, pulmonal): pulmonal\n"
                + "Measurable disease (RECIST) yes/no: YES\n"
                + "\n"
                + "Clinical details\n"
                + "WHO status: 0\n"
                + "Unresolved toxicities grade => 2:\n"
                + "Significant current infection: No\n"
                + "Significant aberration on latest ECG: Sinus\n"
                + "Cancer-related complications (e.g. pleural effusion): nausea";
        // @formatter:on
    }

    @NotNull
    static String createTestQuestionnaireValueV1_1() {
        // @formatter:off
        return "ACTIN Questionnaire V1.0"
                + "Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. "
                + "Please make sure that these fields never contain non-anonymized data!\n"
                + "\n"
                + "Relevant patient history\n"
                + "Treatment history current tumor: cisplatin; nivolumab\n"
                + "Other oncological history (e.g. radiotherapy, second primary): surgery\n"
                + "Non-oncological history: diabetes\n"
                + "\n"
                + "Tumor details\n"
                + "Primary tumor location: ovary\n"
                + "Primary tumor type: serous\n"
                + "Biopsy location: Lymph node\n"
                + "Stage: 3\n"
                + "CNS lesions yes/no/unknown:\n"
                + "- Active:\n"
                + "- Symptomatic:\n"
                + "Brain lesions yes/no/unknown:\n"
                + "- Active:\n"
                + "- Symptomatic:\n"
                + "Bone lesions yes/no/unknown: NO\n"
                + "Liver lesions yes/no/unknown: NO\n"
                + "Other lesions (e.g. lymph node, pulmonal): pulmonal\n"
                + "Measurable disease (RECIST) yes/no: YES\n"
                + "\n"
                + "Clinical details\n"
                + "WHO status: 0\n"
                + "Unresolved toxicities grade => 2:\n"
                + "Significant current infection: No\n"
                + "Significant aberration on latest ECG: Sinus\n"
                + "Cancer-related complications (e.g. pleural effusion): nausea";
        // @formatter:on
    }

    @NotNull
    static String createTestQuestionnaireValueV1_0() {
        // @formatter:off
        return "ACTIN Questionnaire V1.0\n"
                + "Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. "
                + "Please make sure that these fields never contain non-anonymized data!\n"
                + "\n"
                + "Relevant patient history\n"
                + "Treatment history current tumor: ; capecitabine JAN 2020- JUL 2021;\n"
                + "Other oncological history (e.g. radiotherapy, second primary): ; surgery JUN 2021;\n"
                + "Non-oncological history:  NO\n"
                + "\n"
                + "Tumor details\n"
                + "Primary tumor location: lung\n"
                + "Primary tumor type: small-cell carcinoma\n"
                + "Biopsy location: Liver\n"
                + "Stage: IV\n"
                + "CNS lesions yes/no/unknown: No\n"
                + "\\tab Active: yes/no}\\li0\\ri0\\sa0\\sb0\\fi0\\ql\\par}\n"
                + "{\\f2 {\\ltrch Symptomatic:  yes/no\n"
                + "Brain lesions yes/no/unknown: UNKNOWN\n"
                + "\\tab Active: yes/no}\\li0\\ri0\\sa0\\sb0\\fi0\\ql\\par}\n"
                + "{\\f2 {\\ltrch Symptomatic: yes/no\n"
                + "Bone lesions yes/no/unknown: NO\n"
                + "Liver lesions yes/no/unknown: NO\n"
                + "Other lesions (e.g. lymph node, pulmonal): peritoneal; lymph nodes, lung;\n"
                + "Measurable disease (RECIST) yes/no: YES\n"
                + "\n"
                + "Clinical details\n"
                + "WHO status: 1\n"
                + "Unresolved toxicities grade => 2: NA\n"
                + "Significant current infection: NO\n"
                + "Significant aberration on latest ECG: NA\n"
                + "Cancer-related complications (e.g. pleural effusion): ascites";
        // @formatter:on
    }

    @NotNull
    static String createTestQuestionnaireValueV0_2() {
        // @formatter:off
        return "ACTIN Questionnaire\n"
                + "Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. "
                + "Please make sure that these fields never contain non-anonimized data!\n"
                + "\n"
                + "Relevant clinical history\n"
                + "Treatment history current tumor: capecitabine\n"
                + "Other oncological history (e.g. second primary): radiotherapy\n"
                + "Non-oncological history: NA\n"
                + "\n"
                + "Tumor details\n"
                + "Tumor location: cholangio\n"
                + "Tumor type: carcinoma\n"
                + "Biopsy location: liver\n"
                + "Stage (I/II/III/IV): IV\n"
                + "CNS lesions yes/no/unknown:\n"
                + "Active? :\n"
                + "Symptomatic? :\n"
                + "Brain lesions yes/no/unknown:\n"
                + "Active? :\n"
                + "Symptomatic? :\n"
                + "Bone lesions yes/no/unknown: No\n"
                + "Liver lesions yes/no/unknown: No\n"
                + "Biopsy amenable yes/no/unknown:\n"
                + "Measurable disease (RECIST) yes/no/unknown: YES\n"
                + "\n"
                + "Clinical information\n"
                + "WHO status: 2\n"
                + "Unresolved toxicities grade => 2:\n"
                + "Significant current infection: No\n"
                + "Significant aberration on latest ECG:\n"
                + "Other (e.g. pleural effusion): pleural effusion";
        // @formatter:on
    }

    @NotNull
    static String createTestQuestionnaireValueV0_1() {
        // @formatter:off
        return "ACTIN Questionnaire\n"
                + "Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. "
                + "Please make sure that these fields never contain non-anonimized data!\n"
                + "\n"
                + "Patient history\n"
                + "Oncological: Cholangiocarcinoom (lever, lymph retroperitoneaal)\n"
                + "Other: Diabetes Mellitus type 2\n"
                + "\n"
                + "Information\n"
                + "WHO status: 1\n"
                + "TNM criteria: T N M : Unknown\n"
                + "CNS lesions yes/no/unknown: unknown\n"
                + "- Active yes/no: n.v.t.\n"
                + "- Symptomatic yes/no: n.v.t\n"
                + "Brain lesions yes/no/unknown: unknown\n"
                + "- Active yes/no: n.v.t\n"
                + "- Symptomatic yes/no: n.v.t.\n"
                + "Bone lesions yes/no/unknown: Yes\n"
                + "Liver lesions yes/no/unknown: yes\n"
                + "Has bioptable lesion yes/no: yes\n"
                + "Has measurable lesion (RECIST) yes/no: yes\n"
                + "Significant current infection: No\n"
                + "Significant aberration on latest ECG: No\n"
                + "Significant aberration on latest lung function test:\n"
                + "Latest LVEF (%): Unknown\n"
                + "Unresolved toxicities from prior anti-tumor therapy grade => 2: Neuropathy GR3\n"
                + "Other (e.g. Osteoporosis, Pleural effusion):";
        // @formatter:on
    }
}
