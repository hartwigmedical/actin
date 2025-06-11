package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.TumorStageResolver
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedLesion
import com.hartwig.feed.datamodel.FeedTumorDetail
import org.apache.logging.log4j.LogManager

class TumorDetailsExtractor(
    private val lesionLocationCuration: CurationDatabase<LesionLocationConfig>,
    private val primaryTumorCuration: CurationDatabase<PrimaryTumorConfig>,
    private val tumorStageDeriver: TumorStageDeriver
) {

    private val logger = LogManager.getLogger(TumorDetailsExtractor::class.java)

    fun extract(patientId: String, feedTumorDetail: FeedTumorDetail): ExtractionResult<TumorDetails> {

        val curatedOtherLesions = feedTumorDetail.lesions?.let {
            curateOtherLesions(patientId, it)
        }
        val curatedBiopsyLocation = feedTumorDetail.biopsyLocation?.let {
            CurationResponse.createFromConfigs(
                lesionLocationCuration.find(it), patientId, CurationCategory.LESION_LOCATION, it, "lesion location", true
            )
        }

        val lesionLocationConfigMap = listOf(curatedOtherLesions, curatedBiopsyLocation).mapNotNull { it?.configs }
            .flatten()
            .groupBy { it.category }

        val (primaryTumorDetails, tumorExtractionResult) = curateTumorDetails(
            patientId,
            feedTumorDetail.tumorLocation,
            feedTumorDetail.tumorType
        )

        val otherLesions = filterCurateOtherLesions(curatedOtherLesions?.configs)
        val otherSuspectedLesions = filterCurateOtherLesions(curatedOtherLesions?.configs, true)

        val (hasBrainLesions, hasSuspectedBrainLesions) = determineLesionPresence(
            lesionLocationConfigMap,
            LesionLocationCategory.BRAIN,
            feedTumorDetail.hasBrainLesions
        )
        val (hasCnsLesions, hasSuspectedCnsLesions) = determineLesionPresence(
            lesionLocationConfigMap,
            LesionLocationCategory.CNS,
            feedTumorDetail.hasCnsLesions
        )
        val (hasBoneLesions, hasSuspectedBoneLesions) = determineLesionPresence(
            lesionLocationConfigMap,
            LesionLocationCategory.BONE,
            feedTumorDetail.hasBoneLesions
        )
        val (hasLiverLesions, hasSuspectedLiverLesions) = determineLesionPresence(
            lesionLocationConfigMap,
            LesionLocationCategory.LIVER,
            feedTumorDetail.hasLiverLesions
        )
        val (hasLungLesions, hasSuspectedLungLesions) = determineLesionPresence(
            lesionLocationConfigMap,
            LesionLocationCategory.LUNG
        )
        val (hasLymphNodeLesions, hasSuspectedLymphNodeLesions) = determineLesionPresence(
            lesionLocationConfigMap,
            LesionLocationCategory.LYMPH_NODE
        )

        val hasBrainOrGliomaTumor = primaryTumorDetails.primaryTumorLocation == "Brain" || primaryTumorDetails.primaryTumorType == "Glioma"

        val tumorDetails = primaryTumorDetails.copy(
            stage = TumorStageResolver.resolve(feedTumorDetail.stage),
            hasMeasurableDisease = feedTumorDetail.measurableDisease,
            hasBrainLesions = if (hasBrainOrGliomaTumor) false else hasBrainLesions,
            hasSuspectedBrainLesions = if (hasBrainOrGliomaTumor) false else hasSuspectedBrainLesions,
            hasActiveBrainLesions = if (hasBrainOrGliomaTumor) false else feedTumorDetail.hasActiveBrainLesions,
            hasCnsLesions = if (hasBrainOrGliomaTumor) false else hasCnsLesions,
            hasSuspectedCnsLesions = if (hasBrainOrGliomaTumor) false else hasSuspectedCnsLesions,
            hasActiveCnsLesions = if (hasBrainOrGliomaTumor) false else feedTumorDetail.hasActiveCnsLesions,
            hasBoneLesions = hasBoneLesions,
            hasSuspectedBoneLesions = hasSuspectedBoneLesions,
            hasLiverLesions = hasLiverLesions,
            hasSuspectedLiverLesions = hasSuspectedLiverLesions,
            hasLungLesions = hasLungLesions,
            hasSuspectedLungLesions = hasSuspectedLungLesions,
            hasLymphNodeLesions = hasLymphNodeLesions,
            hasSuspectedLymphNodeLesions = hasSuspectedLymphNodeLesions,
            otherLesions = otherLesions,
            otherSuspectedLesions = otherSuspectedLesions,
            biopsyLocation = curatedBiopsyLocation?.config()?.location,
        )

        val tumorDetailsWithDerivedStages = tumorDetails.copy(derivedStages = tumorStageDeriver.derive(tumorDetails))

        return ExtractionResult(
            tumorDetailsWithDerivedStages,
            (curatedOtherLesions?.extractionEvaluation
                ?: CurationExtractionEvaluation()) + tumorExtractionResult + curatedBiopsyLocation?.extractionEvaluation
        )
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
            TumorDetails(
                primaryTumorLocation = it.primaryTumorLocation,
                primaryTumorSubLocation = it.primaryTumorSubLocation,
                primaryTumorType = it.primaryTumorType,
                primaryTumorSubType = it.primaryTumorSubType,
                primaryTumorExtraDetails = it.primaryTumorExtraDetails,
                doids = it.doids
            )
        } ?: TumorDetails()
        return Pair(tumor, primaryTumorCuration.extractionEvaluation)
    }

    private fun tumorInput(inputTumorLocation: String?, inputTumorType: String?): String? {
        return if (inputTumorLocation == null && inputTumorType == null) null else {
            CurationUtil.fullTrim(listOf(inputTumorLocation, inputTumorType).joinToString(" | ") { it ?: "" })
        }
    }

    private fun filterCurateOtherLesions(
        otherLesionsConfig: Set<LesionLocationConfig>?,
        suspected: Boolean? = null
    ): List<String>? {
        return otherLesionsConfig?.filter { config ->
            // We only want to include lesions from the other lesions in actual other lesions
            // if it does not override an explicit lesion location
            val hasRealOtherLesion = config.category == null || config.category == LesionLocationCategory.LYMPH_NODE

            if (suspected == true) {
                hasRealOtherLesion && config.location.isNotEmpty() && config.suspected == true
            } else {
                hasRealOtherLesion && config.location.isNotEmpty() && (config.suspected != true)
            }
        }
            ?.map(LesionLocationConfig::location)
    }

    fun curateOtherLesions(patientId: String, lesions: List<FeedLesion>?): CurationResponse<LesionLocationConfig> {
        if (lesions == null) {
            return CurationResponse()
        }
        return lesions.asSequence()
            .map { CurationUtil.fullTrim(it.location) }
            .map { Pair(it, lesionLocationCuration.find(it)) }
            .map { (input, configs) ->
                CurationResponse.createFromConfigs(
                    configs, patientId, CurationCategory.LESION_LOCATION, input, "lesion location"
                )
            }
            .fold(CurationResponse()) { acc, response -> acc + response }
    }

    private fun determineLesionPresence(
        lesionLocationConfigMap: Map<LesionLocationCategory?, List<LesionLocationConfig>>?,
        lesionLocationCategory: LesionLocationCategory,
        hasLesionsQuestionnaire: Boolean? = null
    ): Pair<Boolean?, Boolean?> {

        val lesionsConfig = lesionLocationConfigMap?.get(lesionLocationCategory)

        if (lesionsConfig.isNullOrEmpty()) {
            return Pair(hasLesionsQuestionnaire, null)
        }

        val hasLesions = lesionsConfig.any { it.suspected != true }.takeIf { it }
        val hasSuspectedLesions = lesionsConfig.any { it.suspected == true }.takeIf { it }

        if ((hasLesions == true || hasSuspectedLesions == true) && hasLesionsQuestionnaire != true) {
            logger.debug("  Overriding presence of ${lesionLocationCategory.name.lowercase()} lesions")
        }

        return Pair(if (hasLesions == true) true else hasLesionsQuestionnaire, hasSuspectedLesions)
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext, tumorStageDeriver: TumorStageDeriver) = TumorDetailsExtractor(
            primaryTumorCuration = curationDatabaseContext.primaryTumorCuration,
            lesionLocationCuration = curationDatabaseContext.lesionLocationCuration,
            tumorStageDeriver = tumorStageDeriver
        )
    }
}