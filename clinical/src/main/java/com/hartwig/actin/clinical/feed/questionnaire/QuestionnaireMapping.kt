package com.hartwig.actin.clinical.feed.questionnaire

import com.google.common.collect.Maps
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

internal object QuestionnaireMapping {
    @JvmField
    val KEYS_V1_6: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    @JvmField
    val KEYS_V1_5: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    @JvmField
    val KEYS_V1_4: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    @JvmField
    val KEYS_V1_3: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    @JvmField
    val KEYS_V1_2: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    @JvmField
    val KEYS_V1_1: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    @JvmField
    val KEYS_V1_0: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    @JvmField
    val KEYS_V0_2: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    @JvmField
    val KEYS_V0_1: MutableMap<QuestionnaireKey, String?> = Maps.newHashMap()

    init {
        KEYS_V1_6[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = "Treatment history current tumor"
        KEYS_V1_6[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = "Other oncological history"
        KEYS_V1_6[QuestionnaireKey.SECONDARY_PRIMARY] = "Secondary primary"
        KEYS_V1_6[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Non-oncological history"
        KEYS_V1_6[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Primary tumor location"
        KEYS_V1_6[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = "Primary tumor type"
        KEYS_V1_6[QuestionnaireKey.BIOPSY_LOCATION] = "Biopsy location"
        KEYS_V1_6[QuestionnaireKey.STAGE] = "Stage"
        KEYS_V1_6[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Measurable disease"
        KEYS_V1_6[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions"
        KEYS_V1_6[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions"
        KEYS_V1_6[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions"
        KEYS_V1_6[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions"
        KEYS_V1_6[QuestionnaireKey.OTHER_LESIONS] = "Other lesions (e.g. lymph node, pulmonal)"
        KEYS_V1_6[QuestionnaireKey.IHC_TEST_RESULTS] = "IHC test results"
        KEYS_V1_6[QuestionnaireKey.PDL1_TEST_RESULTS] = "PD L1 test results"
        KEYS_V1_6[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V1_6[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities grade => 2"
        KEYS_V1_6[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V1_6[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V1_6[QuestionnaireKey.COMPLICATIONS] = "Cancer-related complications (e.g. pleural effusion)"
        KEYS_V1_6[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = "GENAYA subjectno"
        KEYS_V1_5[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = "Treatment history current tumor"
        KEYS_V1_5[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = "Other oncological history"
        KEYS_V1_5[QuestionnaireKey.SECONDARY_PRIMARY] = "Secondary primary"
        KEYS_V1_5[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Non-oncological history"
        KEYS_V1_5[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Primary tumor location"
        KEYS_V1_5[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = "Primary tumor type"
        KEYS_V1_5[QuestionnaireKey.BIOPSY_LOCATION] = "Biopsy location"
        KEYS_V1_5[QuestionnaireKey.STAGE] = "Stage"
        KEYS_V1_5[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Measurable disease"
        KEYS_V1_5[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions"
        KEYS_V1_5[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions"
        KEYS_V1_5[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions"
        KEYS_V1_5[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions"
        KEYS_V1_5[QuestionnaireKey.OTHER_LESIONS] = "Other lesions (e.g. lymph node, pulmonal)"
        KEYS_V1_5[QuestionnaireKey.IHC_TEST_RESULTS] = "IHC test results"
        KEYS_V1_5[QuestionnaireKey.PDL1_TEST_RESULTS] = "PD L1 test results"
        KEYS_V1_5[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V1_5[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities grade => 2"
        KEYS_V1_5[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V1_5[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V1_5[QuestionnaireKey.COMPLICATIONS] = "Cancer-related complications (e.g. pleural effusion)"
        KEYS_V1_5[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = null
        KEYS_V1_4[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = "Treatment history current tumor"
        KEYS_V1_4[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = "Other oncological history"
        KEYS_V1_4[QuestionnaireKey.SECONDARY_PRIMARY] = null
        KEYS_V1_4[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Non-oncological history"
        KEYS_V1_4[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Primary tumor location"
        KEYS_V1_4[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = "Primary tumor type"
        KEYS_V1_4[QuestionnaireKey.BIOPSY_LOCATION] = "Biopsy location"
        KEYS_V1_4[QuestionnaireKey.STAGE] = "Stage"
        KEYS_V1_4[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Measurable disease (RECIST)"
        KEYS_V1_4[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions"
        KEYS_V1_4[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions"
        KEYS_V1_4[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions"
        KEYS_V1_4[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions"
        KEYS_V1_4[QuestionnaireKey.OTHER_LESIONS] = "Other lesions (e.g. lymph node, pulmonal)"
        KEYS_V1_4[QuestionnaireKey.IHC_TEST_RESULTS] = "Previous Molecular tests"
        KEYS_V1_4[QuestionnaireKey.PDL1_TEST_RESULTS] = null
        KEYS_V1_4[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V1_4[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities grade => 2"
        KEYS_V1_4[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V1_4[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V1_4[QuestionnaireKey.COMPLICATIONS] = "Cancer-related complications (e.g. pleural effusion)"
        KEYS_V1_4[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = null
        KEYS_V1_3[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = "Treatment history current tumor"
        KEYS_V1_3[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = "Other oncological history"
        KEYS_V1_3[QuestionnaireKey.SECONDARY_PRIMARY] = null
        KEYS_V1_3[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Non-oncological history"
        KEYS_V1_3[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Primary tumor location"
        KEYS_V1_3[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = "Primary tumor type"
        KEYS_V1_3[QuestionnaireKey.BIOPSY_LOCATION] = "Biopsy location"
        KEYS_V1_3[QuestionnaireKey.STAGE] = "Stage"
        KEYS_V1_3[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Measurable disease (RECIST)"
        KEYS_V1_3[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions"
        KEYS_V1_3[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions"
        KEYS_V1_3[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions"
        KEYS_V1_3[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions"
        KEYS_V1_3[QuestionnaireKey.OTHER_LESIONS] = "Other lesions (e.g. lymph node, pulmonal)"
        KEYS_V1_3[QuestionnaireKey.IHC_TEST_RESULTS] = null
        KEYS_V1_3[QuestionnaireKey.PDL1_TEST_RESULTS] = null
        KEYS_V1_3[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V1_3[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities grade => 2"
        KEYS_V1_3[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V1_3[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V1_3[QuestionnaireKey.COMPLICATIONS] = "Cancer-related complications (e.g. pleural effusion)"
        KEYS_V1_3[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = null
        KEYS_V1_2[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = "Treatment history current tumor"
        KEYS_V1_2[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = "Other oncological history"
        KEYS_V1_2[QuestionnaireKey.SECONDARY_PRIMARY] = null
        KEYS_V1_2[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Non-oncological history"
        KEYS_V1_2[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Primary tumor location"
        KEYS_V1_2[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = "Primary tumor type"
        KEYS_V1_2[QuestionnaireKey.BIOPSY_LOCATION] = "Biopsy location"
        KEYS_V1_2[QuestionnaireKey.STAGE] = "Stage"
        KEYS_V1_2[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Measurable disease (RECIST)"
        KEYS_V1_2[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions"
        KEYS_V1_2[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions"
        KEYS_V1_2[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions"
        KEYS_V1_2[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions"
        KEYS_V1_2[QuestionnaireKey.OTHER_LESIONS] = "Other lesions (e.g. lymph node, pulmonal)"
        KEYS_V1_2[QuestionnaireKey.IHC_TEST_RESULTS] = null
        KEYS_V1_2[QuestionnaireKey.PDL1_TEST_RESULTS] = null
        KEYS_V1_2[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V1_2[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities grade => 2"
        KEYS_V1_2[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V1_2[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V1_2[QuestionnaireKey.COMPLICATIONS] = "Cancer-related complications (e.g. pleural effusion)"
        KEYS_V1_2[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = null
        KEYS_V1_1[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = "Treatment history current tumor"
        KEYS_V1_1[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = "Other oncological history"
        KEYS_V1_1[QuestionnaireKey.SECONDARY_PRIMARY] = null
        KEYS_V1_1[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Non-oncological history"
        KEYS_V1_1[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Primary tumor location"
        KEYS_V1_1[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = "Primary tumor type"
        KEYS_V1_1[QuestionnaireKey.BIOPSY_LOCATION] = "Biopsy location"
        KEYS_V1_1[QuestionnaireKey.STAGE] = "Stage"
        KEYS_V1_1[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Measurable disease (RECIST) yes/no"
        KEYS_V1_1[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions yes/no/unknown"
        KEYS_V1_1[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions yes/no/unknown"
        KEYS_V1_1[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions yes/no/unknown"
        KEYS_V1_1[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions yes/no/unknown"
        KEYS_V1_1[QuestionnaireKey.OTHER_LESIONS] = "Other lesions (e.g. lymph node, pulmonal)"
        KEYS_V1_1[QuestionnaireKey.IHC_TEST_RESULTS] = null
        KEYS_V1_1[QuestionnaireKey.PDL1_TEST_RESULTS] = null
        KEYS_V1_1[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V1_1[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities grade => 2"
        KEYS_V1_1[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V1_1[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V1_1[QuestionnaireKey.COMPLICATIONS] = "Cancer-related complications (e.g. pleural effusion)"
        KEYS_V1_1[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = null
        KEYS_V1_0[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = "Treatment history current tumor"
        KEYS_V1_0[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = "Other oncological history"
        KEYS_V1_0[QuestionnaireKey.SECONDARY_PRIMARY] = null
        KEYS_V1_0[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Non-oncological history"
        KEYS_V1_0[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Primary tumor location"
        KEYS_V1_0[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = "Primary tumor type"
        KEYS_V1_0[QuestionnaireKey.BIOPSY_LOCATION] = "Biopsy location"
        KEYS_V1_0[QuestionnaireKey.STAGE] = "Stage"
        KEYS_V1_0[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Measurable disease (RECIST) yes/no"
        KEYS_V1_0[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions yes/no/unknown"
        KEYS_V1_0[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions yes/no/unknown"
        KEYS_V1_0[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions yes/no/unknown"
        KEYS_V1_0[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions yes/no/unknown"
        KEYS_V1_0[QuestionnaireKey.OTHER_LESIONS] = "Other lesions (e.g. lymph node, pulmonal)"
        KEYS_V1_0[QuestionnaireKey.IHC_TEST_RESULTS] = null
        KEYS_V1_0[QuestionnaireKey.PDL1_TEST_RESULTS] = null
        KEYS_V1_0[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V1_0[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities grade => 2"
        KEYS_V1_0[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V1_0[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V1_0[QuestionnaireKey.COMPLICATIONS] = "Cancer-related complications (e.g. pleural effusion)"
        KEYS_V1_0[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = null
        KEYS_V0_2[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = "Treatment history current tumor"
        KEYS_V0_2[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = "Other oncological history (e.g. second primary)"
        KEYS_V0_2[QuestionnaireKey.SECONDARY_PRIMARY] = null
        KEYS_V0_2[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Non-oncological history"
        KEYS_V0_2[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Tumor location"
        KEYS_V0_2[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = "Tumor type"
        KEYS_V0_2[QuestionnaireKey.BIOPSY_LOCATION] = "Biopsy location"
        KEYS_V0_2[QuestionnaireKey.STAGE] = "Stage (I/II/III/IV)"
        KEYS_V0_2[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Measurable disease (RECIST) yes/no/unknown"
        KEYS_V0_2[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions yes/no/unknown"
        KEYS_V0_2[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions yes/no/unknown"
        KEYS_V0_2[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions yes/no/unknown"
        KEYS_V0_2[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions yes/no/unknown"
        KEYS_V0_2[QuestionnaireKey.OTHER_LESIONS] = null
        KEYS_V0_2[QuestionnaireKey.IHC_TEST_RESULTS] = null
        KEYS_V0_2[QuestionnaireKey.PDL1_TEST_RESULTS] = null
        KEYS_V0_2[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V0_2[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities grade => 2"
        KEYS_V0_2[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V0_2[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V0_2[QuestionnaireKey.COMPLICATIONS] = "Other (e.g. pleural effusion)"
        KEYS_V0_2[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = null
        KEYS_V0_1[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR] = null
        KEYS_V0_1[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY] = null
        KEYS_V0_1[QuestionnaireKey.SECONDARY_PRIMARY] = null
        KEYS_V0_1[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY] = "Other"
        KEYS_V0_1[QuestionnaireKey.PRIMARY_TUMOR_LOCATION] = "Oncological"
        KEYS_V0_1[QuestionnaireKey.PRIMARY_TUMOR_TYPE] = null
        KEYS_V0_1[QuestionnaireKey.BIOPSY_LOCATION] = null
        KEYS_V0_1[QuestionnaireKey.STAGE] = null
        KEYS_V0_1[QuestionnaireKey.HAS_MEASURABLE_DISEASE] = "Has measurable lesion (RECIST) yes/no"
        KEYS_V0_1[QuestionnaireKey.HAS_CNS_LESIONS] = "CNS lesions yes/no/unknown"
        KEYS_V0_1[QuestionnaireKey.HAS_BRAIN_LESIONS] = "Brain lesions yes/no/unknown"
        KEYS_V0_1[QuestionnaireKey.HAS_BONE_LESIONS] = "Bone lesions yes/no/unknown"
        KEYS_V0_1[QuestionnaireKey.HAS_LIVER_LESIONS] = "Liver lesions yes/no/unknown"
        KEYS_V0_1[QuestionnaireKey.OTHER_LESIONS] = null
        KEYS_V0_1[QuestionnaireKey.IHC_TEST_RESULTS] = null
        KEYS_V0_1[QuestionnaireKey.PDL1_TEST_RESULTS] = null
        KEYS_V0_1[QuestionnaireKey.WHO_STATUS] = "WHO status"
        KEYS_V0_1[QuestionnaireKey.UNRESOLVED_TOXICITIES] = "Unresolved toxicities from prior anti-tumor therapy grade => 2"
        KEYS_V0_1[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION] = "Significant current infection"
        KEYS_V0_1[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG] = "Significant aberration on latest ECG"
        KEYS_V0_1[QuestionnaireKey.COMPLICATIONS] = "Other (e.g. Osteoporosis, Pleural effusion)"
        KEYS_V0_1[QuestionnaireKey.GENAYA_SUBJECT_NUMBER] = null
    }

    @JvmStatic
    fun mapping(entry: QuestionnaireEntry): Map<QuestionnaireKey, String?> {
        val version: QuestionnaireVersion = QuestionnaireVersion.Companion.version(entry)
        return when (version) {
            QuestionnaireVersion.V1_6 -> KEYS_V1_6
            QuestionnaireVersion.V1_5 -> KEYS_V1_5
            QuestionnaireVersion.V1_4 -> KEYS_V1_4
            QuestionnaireVersion.V1_3 -> KEYS_V1_3
            QuestionnaireVersion.V1_2 -> KEYS_V1_2
            QuestionnaireVersion.V1_1 -> KEYS_V1_1
            QuestionnaireVersion.V1_0 -> KEYS_V1_0
            QuestionnaireVersion.V0_2 -> KEYS_V0_2
            QuestionnaireVersion.V0_1 -> KEYS_V0_1
            else -> throw IllegalStateException("Questionnaire version not supported for mapping: $version")
        }
    }

    fun keyStrings(entry: QuestionnaireEntry): List<String?> {
        val versionSpecificAdditionalStrings: Stream<String?>
        versionSpecificAdditionalStrings = when (QuestionnaireVersion.Companion.version(entry)) {
            QuestionnaireVersion.V0_1 -> Stream.of(
                "TNM criteria",
                "Information",
                "Significant aberration on latest lung function test",
                "Latest LVEF (%)",
                "Has bioptable lesion"
            )

            QuestionnaireVersion.V0_2 -> Stream.of("Biopsy amenable yes/no/unknown")
            else -> Stream.empty()
        }
        return Stream.of(
            mapping(entry).values.stream().filter { obj: String? -> Objects.nonNull(obj) },
            versionSpecificAdditionalStrings,
            Stream.of("Last date of active treatment", "Active", "Symptomatic")
        )
            .flatMap(Function.identity())
            .collect(Collectors.toList())
    }
}