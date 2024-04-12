package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import org.apache.logging.log4j.LogManager

class TumorDetailsExtractor(
    private val lesionLocationCuration: CurationDatabase<LesionLocationConfig>,
    private val primaryTumorCuration: CurationDatabase<PrimaryTumorConfig>,
    private val tumorStageDeriver: TumorStageDeriver
) {

    private val logger = LogManager.getLogger(TumorDetailsExtractor::class.java)

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<TumorDetails> {
        if (questionnaire == null) {
            return ExtractionResult(TumorDetails(), CurationExtractionEvaluation())
        }
        val lesionsToCheck = ((questionnaire.otherLesions ?: emptyList()) + listOfNotNull(questionnaire.biopsyLocation)).flatMap {
            lesionLocationCuration.find(it).mapNotNull(LesionLocationConfig::category)
        }

        val (primaryTumorDetails, tumorExtractionResult) = curateTumorDetails(
            patientId,
            questionnaire.tumorLocation,
            questionnaire.tumorType
        )
        val (curatedOtherLesions, otherLesionsResult) = curateOtherLesions(patientId, questionnaire.otherLesions)
        val biopsyCuration = questionnaire.biopsyLocation?.let {
            CurationResponse.createFromConfigs(
                lesionLocationCuration.find(it), patientId, CurationCategory.LESION_LOCATION, it, "lesion location", true
            )
        }

        val tumorDetails = primaryTumorDetails.copy(
            biopsyLocation = biopsyCuration?.config()?.location,
            stage = questionnaire.stage,
            hasMeasurableDisease = questionnaire.hasMeasurableDisease,
            hasBrainLesions = determineLesionPresence(lesionsToCheck, LesionLocationCategory.BRAIN, questionnaire.hasBrainLesions),
            hasActiveBrainLesions = questionnaire.hasActiveBrainLesions,
            hasCnsLesions = determineLesionPresence(lesionsToCheck, LesionLocationCategory.CNS, questionnaire.hasCnsLesions),
            hasActiveCnsLesions = questionnaire.hasActiveCnsLesions,
            hasBoneLesions = determineLesionPresence(lesionsToCheck, LesionLocationCategory.BONE, questionnaire.hasBoneLesions),
            hasLiverLesions = determineLesionPresence(lesionsToCheck, LesionLocationCategory.LIVER, questionnaire.hasLiverLesions),
            hasLungLesions = determineLesionPresence(lesionsToCheck, LesionLocationCategory.LUNG),
            hasLymphNodeLesions = determineLesionPresence(lesionsToCheck, LesionLocationCategory.LYMPH_NODE),
            otherLesions = curatedOtherLesions
        )

        return ExtractionResult(tumorDetails, otherLesionsResult + tumorExtractionResult + biopsyCuration?.extractionEvaluation)
    }

    fun curateTumorDetails(
        patientId: String,
        inputTumorLocation: String?,
        inputTumorType: String?
    ): Pair<TumorDetails, CurationExtractionEvaluation> {
        val inputPrimaryTumor = tumorInput(inputTumorLocation, inputTumorType)?.lowercase()
            ?: return Pair(TumorDetails(), CurationExtractionEvaluation())

        val primaryTumorCuration = CurationResponse.createFromConfigs(
            primaryTumorCuration.find(inputPrimaryTumor),
            patientId,
            CurationCategory.PRIMARY_TUMOR,
            inputPrimaryTumor,
            "primary tumor",
            true
        )

        val tumor = primaryTumorCuration.config()?.let {
            val curatedDetails = TumorDetails(
                primaryTumorLocation = it.primaryTumorLocation,
                primaryTumorSubLocation = it.primaryTumorSubLocation,
                primaryTumorType = it.primaryTumorType,
                primaryTumorSubType = it.primaryTumorSubType,
                primaryTumorExtraDetails = it.primaryTumorExtraDetails,
                doids = it.doids
            )
            curatedDetails.copy(derivedStages = tumorStageDeriver.derive(curatedDetails))
        } ?: TumorDetails()
        return Pair(tumor, primaryTumorCuration.extractionEvaluation)
    }

    private fun tumorInput(inputTumorLocation: String?, inputTumorType: String?): String? {
        return if (inputTumorLocation == null && inputTumorType == null) null else {
            CurationUtil.fullTrim(listOf(inputTumorLocation, inputTumorType).joinToString(" | ") { it ?: "" })
        }
    }

    fun curateOtherLesions(patientId: String, otherLesions: List<String>?): ExtractionResult<List<String>?> {
        if (otherLesions == null) {
            return ExtractionResult(null, CurationExtractionEvaluation())
        }
        val (configs, extractionResult) = otherLesions.asSequence()
            .map(CurationUtil::fullTrim)
            .map { Pair(it, lesionLocationCuration.find(it)) }
            .map { (input, configs) ->
                CurationResponse.createFromConfigs(
                    configs, patientId, CurationCategory.LESION_LOCATION, input, "lesion location"
                )
            }
            .fold(CurationResponse<LesionLocationConfig>()) { acc, response -> acc + response }

        val curatedLesions = configs.filter { config ->
            // We only want to include lesions from the other lesions in actual other lesions
            // if it does not override an explicit lesion location
            val hasRealOtherLesion = config.category == null || config.category == LesionLocationCategory.LYMPH_NODE
            hasRealOtherLesion && config.location.isNotEmpty()
        }
            .map(LesionLocationConfig::location)

        return ExtractionResult(curatedLesions, extractionResult)
    }

    private fun determineLesionPresence(
        lesionsToCheck: List<LesionLocationCategory>, lesionLocationCategory: LesionLocationCategory, hasLesion: Boolean? = null
    ): Boolean? {
        if (lesionsToCheck.contains(lesionLocationCategory)) {
            if (hasLesion == false) {
                logger.debug("  Overriding presence of ${lesionLocationCategory.name.lowercase()} lesions")
            }
            return true
        }
        return hasLesion
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext, tumorStageDeriver: TumorStageDeriver) = TumorDetailsExtractor(
            primaryTumorCuration = curationDatabaseContext.primaryTumorCuration,
            lesionLocationCuration = curationDatabaseContext.lesionLocationCuration,
            tumorStageDeriver = tumorStageDeriver
        )
    }
}