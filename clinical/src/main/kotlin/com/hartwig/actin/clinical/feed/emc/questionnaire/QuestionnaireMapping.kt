package com.hartwig.actin.clinical.feed.emc.questionnaire

internal object QuestionnaireMapping {
    private val NEW_KEYS: Map<QuestionnaireKey, String?> =
        listOf(QuestionnaireKey.FAMILY_HISTORY).associateWith { null }

    private val KEYS_INTRODUCED_AFTER_V1_3: Map<QuestionnaireKey, String?> =
        listOf(
            QuestionnaireKey.SECONDARY_PRIMARY,
            QuestionnaireKey.PDL1_TEST_RESULTS,
            QuestionnaireKey.IHC_TEST_RESULTS
        ).associateWith { null } + NEW_KEYS

    val KEYS_V0_1: Map<QuestionnaireKey, String?> = mapOf(
        QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR to null,
        QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY to null,
        QuestionnaireKey.SECONDARY_PRIMARY to null,
        QuestionnaireKey.NON_ONCOLOGICAL_HISTORY to "Other",
        QuestionnaireKey.PRIMARY_TUMOR_LOCATION to "Oncological",
        QuestionnaireKey.PRIMARY_TUMOR_TYPE to null,
        QuestionnaireKey.BIOPSY_LOCATION to null,
        QuestionnaireKey.STAGE to null,
        QuestionnaireKey.HAS_MEASURABLE_DISEASE to "Has measurable lesion (RECIST) yes/no",
        QuestionnaireKey.HAS_CNS_LESIONS to "CNS lesions yes/no/unknown",
        QuestionnaireKey.HAS_BRAIN_LESIONS to "Brain lesions yes/no/unknown",
        QuestionnaireKey.HAS_BONE_LESIONS to "Bone lesions yes/no/unknown",
        QuestionnaireKey.HAS_LIVER_LESIONS to "Liver lesions yes/no/unknown",
        QuestionnaireKey.OTHER_LESIONS to null,
        QuestionnaireKey.PDL1_TEST_RESULTS to null,
        QuestionnaireKey.WHO_STATUS to "WHO status",
        QuestionnaireKey.UNRESOLVED_TOXICITIES to "Unresolved toxicities from prior anti-tumor therapy grade => 2",
        QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION to "Significant current infection",
        QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG to "Significant aberration on latest ECG",
        QuestionnaireKey.COMPLICATIONS to "Other (e.g. Osteoporosis, Pleural effusion)",
    ) + KEYS_INTRODUCED_AFTER_V1_3

    val KEYS_V0_2: Map<QuestionnaireKey, String?> = mapOf(
        QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR to "Treatment history current tumor",
        QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY to "Other oncological history (e.g. second primary)",
        QuestionnaireKey.NON_ONCOLOGICAL_HISTORY to "Non-oncological history",
        QuestionnaireKey.PRIMARY_TUMOR_LOCATION to "Tumor location",
        QuestionnaireKey.PRIMARY_TUMOR_TYPE to "Tumor type",
        QuestionnaireKey.BIOPSY_LOCATION to "Biopsy location",
        QuestionnaireKey.STAGE to "Stage (I/II/III/IV)",
        QuestionnaireKey.HAS_MEASURABLE_DISEASE to "Measurable disease (RECIST) yes/no/unknown",
        QuestionnaireKey.HAS_CNS_LESIONS to "CNS lesions yes/no/unknown",
        QuestionnaireKey.HAS_BRAIN_LESIONS to "Brain lesions yes/no/unknown",
        QuestionnaireKey.HAS_BONE_LESIONS to "Bone lesions yes/no/unknown",
        QuestionnaireKey.HAS_LIVER_LESIONS to "Liver lesions yes/no/unknown",
        QuestionnaireKey.OTHER_LESIONS to null,
        QuestionnaireKey.WHO_STATUS to "WHO status",
        QuestionnaireKey.UNRESOLVED_TOXICITIES to "Unresolved toxicities grade => 2",
        QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION to "Significant current infection",
        QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG to "Significant aberration on latest ECG",
        QuestionnaireKey.COMPLICATIONS to "Other (e.g. pleural effusion)",
    ) + KEYS_INTRODUCED_AFTER_V1_3

    val KEYS_V1_0: Map<QuestionnaireKey, String?> = mapOf(
        QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR to "Treatment history current tumor",
        QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY to "Other oncological history",
        QuestionnaireKey.NON_ONCOLOGICAL_HISTORY to "Non-oncological history",
        QuestionnaireKey.PRIMARY_TUMOR_LOCATION to "Primary tumor location",
        QuestionnaireKey.PRIMARY_TUMOR_TYPE to "Primary tumor type",
        QuestionnaireKey.BIOPSY_LOCATION to "Biopsy location",
        QuestionnaireKey.STAGE to "Stage",
        QuestionnaireKey.HAS_MEASURABLE_DISEASE to "Measurable disease (RECIST) yes/no",
        QuestionnaireKey.HAS_CNS_LESIONS to "CNS lesions yes/no/unknown",
        QuestionnaireKey.HAS_BRAIN_LESIONS to "Brain lesions yes/no/unknown",
        QuestionnaireKey.HAS_BONE_LESIONS to "Bone lesions yes/no/unknown",
        QuestionnaireKey.HAS_LIVER_LESIONS to "Liver lesions yes/no/unknown",
        QuestionnaireKey.OTHER_LESIONS to "Other lesions (e.g. lymph node, pulmonal)",
        QuestionnaireKey.WHO_STATUS to "WHO status",
        QuestionnaireKey.UNRESOLVED_TOXICITIES to "Unresolved toxicities grade => 2",
        QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION to "Significant current infection",
        QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG to "Significant aberration on latest ECG",
        QuestionnaireKey.COMPLICATIONS to "Cancer-related complications (e.g. pleural effusion)",
    ) + KEYS_INTRODUCED_AFTER_V1_3

    val KEYS_V1_1: Map<QuestionnaireKey, String?> = KEYS_V1_0

    val KEYS_V1_2: Map<QuestionnaireKey, String?> = KEYS_V1_1 + mapOf(
        QuestionnaireKey.HAS_MEASURABLE_DISEASE to "Measurable disease (RECIST)",
        QuestionnaireKey.HAS_CNS_LESIONS to "CNS lesions",
        QuestionnaireKey.HAS_BRAIN_LESIONS to "Brain lesions",
        QuestionnaireKey.HAS_BONE_LESIONS to "Bone lesions",
        QuestionnaireKey.HAS_LIVER_LESIONS to "Liver lesions",
    )

    val KEYS_V1_3: Map<QuestionnaireKey, String?> = KEYS_V1_2

    val KEYS_V1_4: Map<QuestionnaireKey, String?> = KEYS_V1_2 + (QuestionnaireKey.IHC_TEST_RESULTS to "Previous Molecular tests")

    val KEYS_V1_5: Map<QuestionnaireKey, String?> = KEYS_V1_2 + mapOf(
        QuestionnaireKey.SECONDARY_PRIMARY to "Secondary primary",
        QuestionnaireKey.HAS_MEASURABLE_DISEASE to "Measurable disease",
        QuestionnaireKey.IHC_TEST_RESULTS to "IHC test results",
        QuestionnaireKey.PDL1_TEST_RESULTS to "PD L1 test results",
    ) + NEW_KEYS

    val KEYS_V1_6: Map<QuestionnaireKey, String?> = KEYS_V1_5

    val KEYS_V1_7: Map<QuestionnaireKey, String?> = KEYS_V1_6 + (QuestionnaireKey.FAMILY_HISTORY to "family history")

    fun mapping(entry: QuestionnaireEntry): Map<QuestionnaireKey, String?> {
        return when (QuestionnaireVersion.version(entry)) {
            QuestionnaireVersion.V1_7 -> KEYS_V1_7
            QuestionnaireVersion.V1_6 -> KEYS_V1_6
            QuestionnaireVersion.V1_5 -> KEYS_V1_5
            QuestionnaireVersion.V1_4 -> KEYS_V1_4
            QuestionnaireVersion.V1_3 -> KEYS_V1_3
            QuestionnaireVersion.V1_2 -> KEYS_V1_2
            QuestionnaireVersion.V1_1 -> KEYS_V1_1
            QuestionnaireVersion.V1_0 -> KEYS_V1_0
            QuestionnaireVersion.V0_2 -> KEYS_V0_2
            QuestionnaireVersion.V0_1 -> KEYS_V0_1
        }
    }

    fun keyStrings(entry: QuestionnaireEntry): List<String> {
        val versionSpecificAdditionalStrings: List<String> = when (QuestionnaireVersion.version(entry)) {
            QuestionnaireVersion.V0_1 -> listOf(
                "TNM criteria",
                "Information",
                "Significant aberration on latest lung function test",
                "Latest LVEF (%)",
                "Has bioptable lesion"
            )

            QuestionnaireVersion.V0_2 -> listOf("Biopsy amenable yes/no/unknown")
            else -> emptyList()
        }
        return listOf(
            mapping(entry).values.filterNotNull(),
            versionSpecificAdditionalStrings,
            listOf("Last date of active treatment", "Active", "Symptomatic")
        ).flatten()
    }
}