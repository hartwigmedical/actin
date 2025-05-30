package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.algo.icd.IcdConstants.ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE
import com.hartwig.actin.algo.icd.IcdConstants.HARMFUL_EFFECTS_OF_DRUGS_CODE
import com.hartwig.actin.algo.icd.IcdConstants.UNSPECIFIED_INFECTION_CODE
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
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
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
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
import java.time.LocalDate
import kotlin.collections.plus

class ComorbidityExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>,
    private val toxicityTranslation: TranslationDatabase<String>
) {

    fun extract(
        patientId: String,
        questionnaire: Questionnaire?,
        toxicityEntries: List<DigitalFileEntry>,
        intoleranceEntries: List<IntoleranceEntry>
    ): ExtractionResult<Pair<List<Comorbidity>, ClinicalStatus>> {
        val infectionExtraction = extractInfection(patientId, questionnaire?.infectionStatus)
        val comorbidityExtraction = listOfNotNull(
            extractQuestionnaireComorbidities(
                questionnaire, Questionnaire::nonOncologicalHistory, patientId, CurationCategory.NON_ONCOLOGICAL_HISTORY
            ),
            extractQuestionnaireComorbidities(questionnaire, Questionnaire::complications, patientId, CurationCategory.COMPLICATION),
            extractFeedToxicities(toxicityEntries, patientId),
            questionnaire?.unresolvedToxicities?.let { extractQuestionnaireToxicities(patientId, it, questionnaire.date) },
            extractIntolerances(patientId, intoleranceEntries),
            questionnaire?.ecg?.let { extractEcg(patientId, it) },
            listOf(infectionExtraction)
        )
            .flatten().fold(
                ExtractionResult(emptyList<Comorbidity>(), CurationExtractionEvaluation())
            ) { (comorbidities, aggregatedEval), (comorbidity, eval) ->
                ExtractionResult(comorbidities + comorbidity, aggregatedEval + eval)
            }

        val clinicalStatus = extractClinicalStatus(
            questionnaire, infectionExtraction.extracted.firstOrNull(), comorbidityExtraction.extracted.filterIsInstance<Complication>()
        )

        return ExtractionResult(comorbidityExtraction.extracted to clinicalStatus, comorbidityExtraction.evaluation)
    }

    private fun extractQuestionnaireComorbidities(
        questionnaire: Questionnaire?, getInput: (Questionnaire) -> List<String>?, patientId: String, category: CurationCategory
    ): List<ExtractionResult<List<Comorbidity>>>? {
        return questionnaire?.let(getInput)?.let { rawInputs ->
            val default = if (category == CurationCategory.COMPLICATION) {
                Complication(null, icdCodes = emptySet())
            } else {
                OtherCondition(null, icdCodes = emptySet())
            }
            rawInputs.map {
                val curatedComorbidity = curate(CurationUtil.fullTrim(it), patientId, category, category.categoryName.lowercase(), default)
                ExtractionResult(
                    extracted = curatedComorbidity.configs.mapNotNull(ComorbidityConfig::curated).map { curated ->
                        if (curated is ToxicityCuration) {
                            Toxicity(curated.name, curated.icdCodes, questionnaire.date, ToxicitySource.QUESTIONNAIRE, curated.grade)
                        } else curated
                    },
                    evaluation = curatedComorbidity.extractionEvaluation
                )
            }
        }
    }

    private fun extractIntolerances(
        patientId: String, intoleranceEntries: List<IntoleranceEntry>
    ): List<ExtractionResult<List<Intolerance>>> {
        return intoleranceEntries.map { entry: IntoleranceEntry ->
            val rawIntolerance = with(entry) {
                Intolerance(
                    name = capitalizeFirstLetterOnly(codeText),
                    icdCodes = setOf(IcdCode(ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE)),
                    type = capitalizeFirstLetterOnly(isSideEffect),
                    clinicalStatus = capitalizeFirstLetterOnly(clinicalStatus),
                    verificationStatus = capitalizeFirstLetterOnly(verificationStatus),
                    criticality = capitalizeFirstLetterOnly(criticality),
                )
            }
            val curationResponse =
                curate(entry.codeText, patientId, CurationCategory.INTOLERANCE, "intolerance", rawIntolerance.copy(name = null), true)

            val curatedIntolerance = curationResponse.config()?.curated?.let { curated ->
                rawIntolerance.copy(name = curated.name, icdCodes = curated.icdCodes)
            } ?: rawIntolerance
            ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
        }
    }

    private fun extractFeedToxicities(toxicityEntries: List<DigitalFileEntry>, patientId: String): List<ExtractionResult<List<Toxicity>>> {
        return toxicityEntries.mapNotNull { toxicityEntry ->
            extractGrade(toxicityEntry)?.let { grade ->
                val input = toxicityEntry.itemText
                val rawToxicity = Toxicity(
                    name = input,
                    icdCodes = setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)),
                    evaluatedDate = toxicityEntry.authored,
                    source = ToxicitySource.EHR,
                    grade = grade,
                )
                if (input.isEmpty()) ExtractionResult(listOf(rawToxicity), CurationExtractionEvaluation()) else {
                    curatedToxicity(input, rawToxicity, patientId) ?: translatedToxicity(input, rawToxicity, patientId)
                }
            }
        }
    }

    private fun curatedToxicity(input: String, rawToxicity: Toxicity, patientId: String): ExtractionResult<List<Toxicity>>? {
        val curationResponse = curate(input, patientId, CurationCategory.TOXICITY, "toxicity", rawToxicity.copy(name = null))
        return curationResponse.config()?.curated?.let { curated ->
            ExtractionResult(
                listOf(
                    rawToxicity.copy(
                        name = curated.name,
                        icdCodes = curated.icdCodes,
                        grade = (curated as? ToxicityCuration)?.grade ?: rawToxicity.grade
                    )
                ),
                curationResponse.extractionEvaluation
            )
        }
    }

    private fun translatedToxicity(input: String, rawToxicity: Toxicity, patientId: String): ExtractionResult<List<Toxicity>> {
        return CurationResponse.createFromTranslation(
            toxicityTranslation.find(input), patientId, CurationCategory.TOXICITY_TRANSLATION, input, "toxicity"
        ).let { translationResponse ->
            ExtractionResult(
                listOf(translationResponse.config()?.translated?.let { rawToxicity.copy(name = it) } ?: rawToxicity),
                translationResponse.extractionEvaluation
            )
        }
    }

    private fun extractGrade(entry: DigitalFileEntry): Int? = entry.itemAnswerValueValueString.takeUnless { it.isEmpty() }?.let { value ->
        val notApplicableIndex = value.indexOf(". Not applicable")
        return Integer.valueOf(if (notApplicableIndex > 0) value.substring(0, notApplicableIndex) else value)
    }

    private fun extractQuestionnaireToxicities(
        patientId: String, unresolvedToxicities: List<String>, questionnaireDate: LocalDate
    ): List<ExtractionResult<List<Toxicity>>> {
        val rawToxicity = Toxicity(
            name = null,
            icdCodes = setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)),
            evaluatedDate = questionnaireDate,
            source = ToxicitySource.QUESTIONNAIRE,
            grade = null
        )
        return unresolvedToxicities.map { input ->
            val trimmed = CurationUtil.fullTrim(input)
            val curationResponse = curate(trimmed, patientId, CurationCategory.TOXICITY, "toxicity", rawToxicity)
            val toxicities = curationResponse.configs.filterNot(CurationConfig::ignore).mapNotNull { config ->
                config.curated?.let { curated ->
                    rawToxicity.copy(name = curated.name, icdCodes = curated.icdCodes, grade = (curated as? ToxicityCuration)?.grade)
                }
            }
            ExtractionResult(toxicities, curationResponse.extractionEvaluation)
        }
    }

    private fun extractEcg(patientId: String, rawEcg: Ecg): List<ExtractionResult<List<Ecg>>> {
        val curationResponse = rawEcg.name?.let { curate(it, patientId, CurationCategory.ECG, "ECG", rawEcg.copy(name = null), true) }
        val ecg = if (curationResponse?.configs?.size == 0) rawEcg else {
            curationResponse?.config()?.takeUnless { it.ignore }?.curated?.let { curated ->
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

    private fun <T, U> coalesce(curated: Comorbidity?, default: T, function: (T) -> U): U =
        curated?.let { it as? T }?.let(function) ?: function(default)

    private fun extractInfection(patientId: String, rawInfectionStatus: InfectionStatus?): ExtractionResult<List<OtherCondition>> {
        val defaultInfection = OtherCondition(null, setOf(IcdCode(UNSPECIFIED_INFECTION_CODE)))
        val curationResponse = rawInfectionStatus?.description?.let {
            curate(it, patientId, CurationCategory.INFECTION, "infection", defaultInfection, true)
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
        questionnaire: Questionnaire?, curatedInfection: OtherCondition?, curatedComplications: List<Complication>
    ): ClinicalStatus {
        return questionnaire?.let {
            ClinicalStatus(
                who = questionnaire.whoStatus,
                infectionStatus = questionnaire.infectionStatus?.copy(description = curatedInfection?.name),
                lvef = determineLvef(questionnaire.nonOncologicalHistory),
                hasComplications = when {
                    curatedComplications.isNotEmpty() -> true
                    questionnaire.complications?.any(BooleanValueParser::isUnknown) != false -> null
                    else -> false
                }
            )
        } ?: ClinicalStatus()
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