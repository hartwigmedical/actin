package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.algo.icd.IcdConstants.ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE
import com.hartwig.actin.algo.icd.IcdConstants.HARMFUL_EFFECTS_OF_DRUGS_CODE
import com.hartwig.actin.algo.icd.IcdConstants.UNSPECIFIED_INFECTION_CODE
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationUtil.capitalizeFirstLetterOnly
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.ToxicityCuration
import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.InfectionStatus
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.DatedEntry
import com.hartwig.feed.datamodel.FeedAllergy
import com.hartwig.feed.datamodel.FeedPatientRecord
import com.hartwig.feed.datamodel.FeedToxicity

class ComorbidityExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>,
    private val toxicityTranslation: TranslationDatabase<String>
) {

    fun extract(feedRecord: FeedPatientRecord): ExtractionResult<Pair<List<Comorbidity>, ClinicalStatus>> {
        val patientId = feedRecord.patientDetails.patientId

        val infectionStatus = feedRecord.infectionStatus?.let {
            InfectionStatus(it.hasActiveInfection, it.description)
        }

        val infectionExtraction = extractInfection(patientId, infectionStatus)

        val comorbidityExtraction = listOfNotNull(
            extractComorbidities(
                feedRecord, FeedPatientRecord::otherConditions, patientId, CurationCategory.NON_ONCOLOGICAL_HISTORY
            ),
            feedRecord.complications?.let {
                extractComorbidities(
                    feedRecord,
                    FeedPatientRecord::complications,
                    patientId,
                    CurationCategory.COMPLICATION
                )
            },
            extractToxicities(patientId, feedRecord.toxicities),
            extractIntolerances(patientId, feedRecord.allergies),
            feedRecord.ecg?.let { extractEcg(patientId, it) },
            listOf(infectionExtraction)
        )
            .flatten().fold(
                ExtractionResult(emptyList<Comorbidity>(), CurationExtractionEvaluation())
            ) { (comorbidities, aggregatedEval), (comorbidity, eval) ->
                ExtractionResult(comorbidities + comorbidity, aggregatedEval + eval)
            }

        val clinicalStatus = extractClinicalStatus(
            feedRecord,
            infectionStatus,
            infectionExtraction.extracted.firstOrNull(),
            comorbidityExtraction.extracted.filterIsInstance<Complication>()
        )

        return ExtractionResult(comorbidityExtraction.extracted to clinicalStatus, comorbidityExtraction.evaluation)
    }

    private fun extractComorbidities(
        feedRecord: FeedPatientRecord, getInput: (FeedPatientRecord) -> List<DatedEntry>?, patientId: String, category: CurationCategory
    ): List<ExtractionResult<List<Comorbidity>>>? {
        return getInput(feedRecord).let { entries ->
            val default = if (category == CurationCategory.COMPLICATION) {
                Complication(null, icdCodes = emptySet())
            } else {
                OtherCondition(null, icdCodes = emptySet())
            }
            entries?.map {
                val curatedComorbidity =
                    curate(CurationUtil.fullTrim(it.name), patientId, category, category.categoryName.lowercase(), default)
                ExtractionResult(
                    extracted = curatedComorbidity.configs
                        .filterNot(CurationConfig::ignore)
                        .mapNotNull(ComorbidityConfig::curated).map { curated ->
                            if (curated is ToxicityCuration) {
                                Toxicity(
                                    curated.name,
                                    curated.icdCodes,
                                    feedRecord.patientDetails.questionnaireDate,
                                    ToxicitySource.QUESTIONNAIRE,
                                    curated.grade
                                )
                            } else curated
                        },
                    evaluation = curatedComorbidity.extractionEvaluation
                )
            }
        }
    }

    private fun extractIntolerances(
        patientId: String, entries: List<FeedAllergy>
    ): List<ExtractionResult<List<Intolerance>>> {
        return entries.map { entry ->
            val rawIntolerance = with(entry) {
                Intolerance(
                    name = capitalizeFirstLetterOnly(name),
                    icdCodes = setOf(IcdCode(ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE)),
                    type = capitalizeFirstLetterOnly(type),
                    clinicalStatus = capitalizeFirstLetterOnly(clinicalStatus),
                    verificationStatus = capitalizeFirstLetterOnly(verificationStatus),
                    criticality = capitalizeFirstLetterOnly(severity),
                )
            }
            val curationResponse =
                curate(entry.name, patientId, CurationCategory.INTOLERANCE, "intolerance", rawIntolerance.copy(name = null), true)

            val curatedIntolerance = curationResponse.config()
                ?.takeUnless { it.ignore }?.curated?.let { curated ->
                    rawIntolerance.copy(name = curated.name, icdCodes = curated.icdCodes)
                } ?: rawIntolerance
            ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
        }
    }

    private fun extractToxicities(patientId: String, entries: List<FeedToxicity>): List<ExtractionResult<List<Toxicity>>> {
        return entries.map { entry ->
            entry to Toxicity(
                name = entry.name,
                icdCodes = setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)),
                evaluatedDate = entry.evaluatedDate,
                source = resolveToxicitySource(entry.source),
                grade = entry.grade
            )
        }.mapNotNull { (entry, raw) ->
            when (raw.source) {
                ToxicitySource.QUESTIONNAIRE -> {
                    curatedQuestionnaireToxicities(patientId, entry.name, raw)
                }

                ToxicitySource.EHR -> {
                    entry.grade?.let {
                        curatedEHRToxicity(patientId, entry.name, raw) ?: translatedToxicity(patientId, entry.name, raw)
                    }
                }
            }
        }
    }

    fun resolveToxicitySource(source: String?) = ToxicitySource.entries.firstOrNull { it.display().equals(source, ignoreCase = true) }
        ?: throw IllegalStateException("Could not resolved Toxicity Source lab unit: '$source'")


    private fun curatedEHRToxicity(patientId: String, input: String, rawToxicity: Toxicity): ExtractionResult<List<Toxicity>>? {
        return if (input.isEmpty()) {
            ExtractionResult(listOf(rawToxicity), CurationExtractionEvaluation())
        } else {
            val curationResponse = curate(input, patientId, CurationCategory.TOXICITY, "toxicity", rawToxicity.copy(name = null), true)
            curationResponse.config()?.takeUnless { it.ignore }?.let { config ->
                config.curated?.let { curated ->
                    ExtractionResult(
                        listOf(
                            rawToxicity.copy(
                                name = curated.name,
                                icdCodes = curated.icdCodes,
                                grade = (curated as? ToxicityCuration)?.grade ?: rawToxicity.grade
                            )
                        ), curationResponse.extractionEvaluation
                    )
                }
            }
        }
    }

    private fun curatedQuestionnaireToxicities(patientId: String, input: String, rawToxicity: Toxicity): ExtractionResult<List<Toxicity>>? {
        val curationResponse = curate(input, patientId, CurationCategory.TOXICITY, "toxicity", rawToxicity.copy(name = null), false)
        val toxicities = curationResponse.configs.filterNot { it.ignore }
            .filter { it.curated != null && it.curated::class == rawToxicity::class }
            .mapNotNull { config ->
                config.curated?.let { curated ->
                    rawToxicity.copy(
                        name = curated.name,
                        icdCodes = curated.icdCodes,
                        grade = (curated as? ToxicityCuration)?.grade ?: rawToxicity.grade
                    )
                }
            }
        return ExtractionResult(toxicities, curationResponse.extractionEvaluation)
    }

    private fun translatedToxicity(patientId: String, input: String, rawToxicity: Toxicity): ExtractionResult<List<Toxicity>> {
        return CurationResponse.createFromTranslation(
            toxicityTranslation.find(input), patientId, CurationCategory.TOXICITY_TRANSLATION, input, "toxicity"
        ).let { translationResponse ->
            ExtractionResult(
                listOf(translationResponse.config()?.translated?.let { rawToxicity.copy(name = it) } ?: rawToxicity),
                translationResponse.extractionEvaluation
            )
        }
    }

    private fun extractEcg(patientId: String, input: String): List<ExtractionResult<List<Ecg>>> {
        val rawEcg = Ecg(name = input, jtcMeasure = null, qtcfMeasure = null)
        val curationResponse = rawEcg.name?.let { curate(it, patientId, CurationCategory.ECG, "ECG", rawEcg.copy(name = null), true) }
        val ecg = if (curationResponse?.config() == null) rawEcg else {
            curationResponse.config()?.takeUnless { it.ignore }?.curated?.let { curated ->
                rawEcg.copy(
                    name = curated.name,
                    icdCodes = curated.icdCodes,
                    year = curated.year,
                    month = curated.month,
                    qtcfMeasure = coalesce(curated, rawEcg, Ecg::qtcfMeasure),
                    jtcMeasure = coalesce(curated, rawEcg, Ecg::jtcMeasure)
                )
            }
        }
        return listOf(ExtractionResult(listOfNotNull(ecg), curationResponse?.extractionEvaluation ?: CurationExtractionEvaluation()))
    }

    inline fun <reified T, U> coalesce(curated: Comorbidity?, default: T, function: (T) -> U): U =
        (curated as? T)?.let(function) ?: function(default)

    private fun extractInfection(patientId: String, rawInfectionStatus: InfectionStatus?): ExtractionResult<List<OtherCondition>> {
        val defaultInfection = OtherCondition(null, setOf(IcdCode(UNSPECIFIED_INFECTION_CODE)))
        val curationResponse = rawInfectionStatus?.description?.let {
            curate(it, patientId, CurationCategory.INFECTION, "infection", defaultInfection)
        }
        val infectionStatus = when (curationResponse?.configs?.size) {
            0 -> defaultInfection
            1 -> curationResponse.configs.first().takeUnless { it.ignore }?.curated?.let { curated ->
                (curated as? OtherCondition) ?: OtherCondition(curated.name, curated.icdCodes, curated.year, curated.month)
            }

            else -> null
        }
        return ExtractionResult(listOfNotNull(infectionStatus), curationResponse?.extractionEvaluation ?: CurationExtractionEvaluation())
    }

    private fun extractClinicalStatus(
        feedRecord: FeedPatientRecord,
        infectionStatus: InfectionStatus?,
        curatedInfection: OtherCondition?,
        curatedComplications: List<Complication>
    ): ClinicalStatus {
        return ClinicalStatus(
            who = feedRecord.whoEvaluations.maxByOrNull { it.evaluationDate }?.status,
            infectionStatus = infectionStatus?.copy(description = curatedInfection?.name),
            lvef = determineLvef(feedRecord.otherConditions.map { it.name }),
            hasComplications = when {
                curatedComplications.isNotEmpty() -> true
                // AE: Ideally BooleanValueParser should be removed from this project and actin-feed should return null complications and/or the unknown indication
                feedRecord.complications?.any { BooleanValueParser.isUnknown(it.name) } != false -> null
                else -> false
            }
        )
    }

    private fun determineLvef(nonOncologicalHistoryEntries: List<String>?): Double? {
        // We do not raise warnings or propagate evaluated inputs here since we use the same configs for otherConditions
        return nonOncologicalHistoryEntries?.asSequence()
            ?.flatMap(comorbidityCuration::find)
            ?.filterNot(CurationConfig::ignore)
            ?.firstNotNullOfOrNull { it.lvef }
    }

    private fun curate(
        input: String,
        patientId: String,
        category: CurationCategory,
        configType: String,
        defaultForYesInput: Comorbidity,
        requireUniqueness: Boolean = false
    ): CurationResponse<ComorbidityConfig> {
        val trimmed = input.trim()
        return if (BooleanValueParser.isTrue(trimmed)) {
            CurationResponse(
                setOf(ComorbidityConfig(input, false, curated = defaultForYesInput)),
                CurationExtractionEvaluation(comorbidityEvaluatedInputs = setOf(trimmed.lowercase()))
            )
        } else {
            CurationResponse.createFromConfigs(
                comorbidityCuration.find(trimmed),
                patientId,
                category,
                input,
                configType,
                requireUniqueness
            )
        }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            ComorbidityExtractor(
                comorbidityCuration = curationDatabaseContext.comorbidityCuration,
                toxicityTranslation = curationDatabaseContext.toxicityTranslation
            )
    }
}