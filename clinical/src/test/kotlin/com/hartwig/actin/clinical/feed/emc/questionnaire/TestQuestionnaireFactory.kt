package com.hartwig.actin.clinical.feed.emc.questionnaire

import java.time.LocalDate

object TestQuestionnaireFactory {
    fun createTestQuestionnaireEntry(): QuestionnaireEntry {
        return QuestionnaireEntry(
            subject = "TEST-01-01-0001",
            authored = LocalDate.of(2020, 8, 28),
            description = "Description",
            itemText = "ItemText",
            text = createTestQuestionnaireValueV1_6()
        )
    }

    fun entryWithText(text: String): QuestionnaireEntry {
        return createTestQuestionnaireEntry().copy(text = text)
    }

    fun createTestQuestionnaireValueV1_7(): String {
        return createTestQuestionnaireValueV1_6().replace(
            "ACTIN Questionnaire V1.6",
            "ACTIN Questionnaire V1.7"
        ) + "\nfamily history:\n"
    }

    fun createTestQuestionnaireValueV1_6(): String {
        // @formatter:off
        return """
               ACTIN Questionnaire V1.6
               Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. Please make sure that these fields never contain non-anonymized data!
               
               Relevant patient history
               Treatment history current tumor: cisplatin; nivolumab
               Other oncological history (e.g. radiotherapy, surgery): surgery
               Secondary primary: sarcoma
               - Last date of active treatment: Feb 2020
               Non-oncological history: diabetes
               
               Tumor details
               Primary tumor location: ovary
               Primary tumor type: serous
               Biopsy location: lymph node
               Stage: 4
               CNS lesions:
               -Active:
               Brain lesions: YES
               -Active: yes
               Bone lesions: NO
               Liver lesions: NO
               Other lesions (e.g. lymph node, pulmonal): pulmonal, abdominal
               Measurable disease: YES
               
               Previous Molecular tests
               - IHC test results: ERBB2 3+
               - PD L1 test results: Positive
               
               Clinical details
               WHO status: 0
               Unresolved toxicities grade => 2: toxic
               Significant current infection: No
               Significant aberration on latest ECG: Sinus
               Cancer-related complications (e.g. pleural effusion): vomit
               
               GENAYA subjectno: GAYA-01-02-9999
               """.trimIndent()
            // @formatter:on
    }

    fun createTestQuestionnaireValueV1_5(): String {
        // @formatter:off
        return ("ACTIN Questionnaire V1.5\n"
             + "Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. "
             + "Please make sure that these fields never contain non-anonymized data!\n"
             + "\n"
             + "Relevant patient history\n"
             + "Treatment history current tumor: cisplatin; nivolumab\n"
             + "Other oncological history (e.g. radiotherapy, surgery): surgery\n"
             + "Secondary primary: sarcoma\n"
             + "- Last date of active treatment: Feb 2020 \n"
             + "Non-oncological history: diabetes\n"
             + "\n"
             + "Tumor details\n"
             + "Primary tumor location: ovary\n"
             + "Primary tumor type: serous\n"
             + "Biopsy location: lymph node\n"
             + "Stage: 4\n"
             + "CNS lesions:\n"
             + "-Active:\n"
             + "Brain lesions: YES\n"
             + "-Active: yes\n"
             + "Bone lesions: NO\n"
             + "Liver lesions: NO\n"
             + "Other lesions (e.g. lymph node, pulmonal): pulmonal, abdominal\n"
             + "Measurable disease: YES\n"
             + "\n"
             + "Previous Molecular tests\n"
             + "- IHC test results: \\n\\nERBB2 3+\n"
             + "- PD L1 test results: Positive\n"
             + "\n"
             + "Clinical details\n"
             + "WHO status: 0\n"
             + "Unresolved toxicities grade => 2: toxic\n"
             + "Significant current infection: No\n"
             + "Significant aberration on latest ECG: Sinus\n"
             + "Cancer-related complications (e.g. pleural effusion): vomit\n")
            // @formatter:on
    }

    fun createTestQuestionnaireValueV1_4(): String {
        // @formatter:off
        return """
               ACTIN Questionnaire V1.4
               Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. Please make sure that these fields never contain non-anonymized data!
               
               Relevant patient history
               Treatment history current tumor: cisplatin; nivolumab
               Other oncological history (e.g. radiotherapy, second primary): surgery
               Non-oncological history: diabetes
               
               Tumor details
               Primary tumor location: ovary
               Primary tumor type: serous
               Biopsy location: Lymph node
               Stage: 3
               CNS lesions:
               -Active:
               -Symptomatic:
               Brain lesions:
               -Active:
               -Symptomatic:
               Bone lesions: NO
               Liver lesions: NO
               Other lesions (e.g. lymph node, pulmonal): pulmonal
               Measurable disease (RECIST): YES
               Previous Molecular tests: positive for KRAS; p.G12D, CCND1, APC exon 16; TP53; p.D259Y
               
               Clinical details
               WHO status: 0
               Unresolved toxicities grade => 2:
               Significant current infection: No
               Significant aberration on latest ECG: Sinus
               Cancer-related complications (e.g. pleural effusion): nausea
               """.trimIndent()
            // @formatter:on
    }

    fun createTestQuestionnaireValueV1_3(): String {
        // @formatter:off
        return """
               ACTIN Questionnaire V1.0
               Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. Please make sure that these fields never contain non-anonymized data!
               
               Relevant patient history
               Treatment history current tumor: cisplatin; nivolumab
               Other oncological history (e.g. radiotherapy, second primary): surgery
               Non-oncological history: diabetes
               
               Tumor details
               Primary tumor location: ovary
               Primary tumor type: serous
               Biopsy location: Lymph node
               Stage: 3
               CNS lesions:
               -Active:
               -Symptomatic:
               Brain lesions:
               -Active:
               -Symptomatic:
               Bone lesions: NO
               Liver lesions: NO
               Other lesions (e.g. lymph node, pulmonal): pulmonal
               Measurable disease (RECIST): YES
               
               Clinical details
               WHO status: 0
               Unresolved toxicities grade => 2:
               Significant current infection: No
               Significant aberration on latest ECG: Sinus
               Cancer-related complications (e.g. pleural effusion): nausea
               """.trimIndent()
            // @formatter:on
    }

    fun createTestQuestionnaireValueV1_2(): String {
        // @formatter:off
        return """
               ACTIN Questionnaire V1.0
               Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. Please make sure that these fields never contain non-anonymized data!
               
               Relevant patient history
               Treatment history current tumor: cisplatin; nivolumab
               Other oncological history (e.g. radiotherapy, second primary): surgery
               Non-oncological history: diabetes
               
               Tumor details
               Primary tumor location: ovary
               Primary tumor type: serous
               Biopsy location: Lymph node
               Stage: 3
               CNS lesions yes/no/unknown:
               -Active:
               -Symptomatic:
               Brain lesions yes/no/unknown:
               -Active:
               -Symptomatic:
               Bone lesions yes/no/unknown: NO
               Liver lesions yes/no/unknown: NO
               Other lesions (e.g. lymph node, pulmonal): pulmonal
               Measurable disease (RECIST) yes/no: YES
               
               Clinical details
               WHO status: 0
               Unresolved toxicities grade => 2:
               Significant current infection: No
               Significant aberration on latest ECG: Sinus
               Cancer-related complications (e.g. pleural effusion): nausea
               """.trimIndent()
            // @formatter:on
    }

    fun createTestQuestionnaireValueV1_1(): String {
        // @formatter:off
        return """
               ACTIN Questionnaire V1.0
               Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. Please make sure that these fields never contain non-anonymized data!
               
               Relevant patient history
               Treatment history current tumor: cisplatin; nivolumab
               Other oncological history (e.g. radiotherapy, second primary): surgery
               Non-oncological history: diabetes
               
               Tumor details
               Primary tumor location: ovary
               Primary tumor type: serous
               Biopsy location: Lymph node
               Stage: 3
               CNS lesions yes/no/unknown:
               - Active:
               - Symptomatic:
               Brain lesions yes/no/unknown:
               - Active:
               - Symptomatic:
               Bone lesions yes/no/unknown: NO
               Liver lesions yes/no/unknown: NO
               Other lesions (e.g. lymph node, pulmonal): pulmonal
               Measurable disease (RECIST) yes/no: YES
               
               Clinical details
               WHO status: 0
               Unresolved toxicities grade => 2:
               Significant current infection: No
               Significant aberration on latest ECG: Sinus
               Cancer-related complications (e.g. pleural effusion): nausea
               """.trimIndent()
            // @formatter:on
    }

    fun createTestQuestionnaireValueV1_0(): String {
        // @formatter:off
        return """
               ACTIN Questionnaire V1.0
               Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. Please make sure that these fields never contain non-anonymized data!
               
               Relevant patient history
               Treatment history current tumor: ; capecitabine JAN 2020- JUL 2021;
               Other oncological history (e.g. radiotherapy, second primary): ; surgery JUN 2021;
               Non-oncological history:  NO
               
               Tumor details
               Primary tumor location: lung
               Primary tumor type: small-cell carcinoma
               Biopsy location: Liver
               Stage: IV
               CNS lesions yes/no/unknown: No
               \tab Active: yes/no}\li0\ri0\sa0\sb0\u000ci0\ql\par}
               {\u000c2 {\ltrch Symptomatic:  yes/no
               Brain lesions yes/no/unknown: UNKNOWN
               \tab Active: yes/no}\li0\ri0\sa0\sb0\fi0\ql\par}
               {\f2 {\ltrch Symptomatic: yes/no
               Bone lesions yes/no/unknown: NO
               Liver lesions yes/no/unknown: NO
               Other lesions (e.g. lymph node, pulmonal): peritoneal; lymph nodes, lung;
               Measurable disease (RECIST) yes/no: YES
               
               Clinical details
               WHO status: 1
               Unresolved toxicities grade => 2: NA
               Significant current infection: NO
               Significant aberration on latest ECG: NA
               Cancer-related complications (e.g. pleural effusion): ascites
               """.trimIndent()
            // @formatter:on
    }

    fun createTestQuestionnaireValueV0_2(): String {
        // @formatter:off
        return """
               ACTIN Questionnaire
               Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. Please make sure that these fields never contain non-anonimized data!
               
               Relevant clinical history
               Treatment history current tumor: capecitabine
               Other oncological history (e.g. second primary): radiotherapy
               Non-oncological history: NA
               
               Tumor details
               Tumor location: cholangio
               Tumor type: carcinoma
               Biopsy location: liver
               Stage (I/II/III/IV): IV
               CNS lesions yes/no/unknown:
               Active? :
               Symptomatic? :
               Brain lesions yes/no/unknown:
               Active? :
               Symptomatic? :
               Bone lesions yes/no/unknown: No
               Liver lesions yes/no/unknown: No
               Biopsy amenable yes/no/unknown:
               Measurable disease (RECIST) yes/no/unknown: YES
               
               Clinical information
               WHO status: 2
               Unresolved toxicities grade => 2:
               Significant current infection: No
               Significant aberration on latest ECG:
               Other (e.g. pleural effusion): pleural effusion
               """.trimIndent()
            // @formatter:on
    }

    fun createTestQuestionnaireValueV0_1(): String {
        // @formatter:off
        return """
               ACTIN Questionnaire
               Important: The information in these fields will be automatically extracted from the EHR as part of the ACTIN project. Please make sure that these fields never contain non-anonimized data!
               
               Patient history
               Oncological: Cholangiocarcinoom (lever, lymph retroperitoneaal)
               Other: Diabetes Mellitus type 2
               
               Information
               WHO status: 1
               TNM criteria: T N M : Unknown
               CNS lesions yes/no/unknown: unknown
               - Active yes/no: n.v.t.
               - Symptomatic yes/no: n.v.t
               Brain lesions yes/no/unknown: unknown
               - Active yes/no: n.v.t
               - Symptomatic yes/no: n.v.t.
               Bone lesions yes/no/unknown: Yes
               Liver lesions yes/no/unknown: yes
               Has bioptable lesion yes/no: yes
               Has measurable lesion (RECIST) yes/no: yes
               Significant current infection: No
               Significant aberration on latest ECG: No
               Significant aberration on latest lung function test:
               Latest LVEF (%): Unknown
               Unresolved toxicities from prior anti-tumor therapy grade => 2: Neuropathy GR3
               Other (e.g. Osteoporosis, Pleural effusion):
               """.trimIndent()
            // @formatter:on
    }
}