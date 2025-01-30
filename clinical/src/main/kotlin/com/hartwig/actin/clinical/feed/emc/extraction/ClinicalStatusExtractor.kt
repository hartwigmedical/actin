package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.InfectionStatus

class ClinicalStatusExtractor(
    private val infectionCuration: CurationDatabase<InfectionConfig>,
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>
) {

    fun extract(patientId: String, questionnaire: Questionnaire?, hasComplications: Boolean): ExtractionResult<ClinicalStatus> {
        if (questionnaire == null) {
            return ExtractionResult(ClinicalStatus(), CurationExtractionEvaluation())
        }
        val ecgCuration = curateECG(patientId, questionnaire.ecg)
        val infectionCuration = curateInfection(patientId, questionnaire.infectionStatus)

        val clinicalStatus = ClinicalStatus(
            who = questionnaire.whoStatus,
            infectionStatus = infectionCuration.extracted,
            lvef = determineLVEF(questionnaire.nonOncologicalHistory),
            hasComplications = hasComplications
        )


        return ExtractionResult(clinicalStatus, ecgCuration.evaluation + infectionCuration.evaluation)
    }

    private fun curateECG(patientId: String, rawECG: Ecg?): ExtractionResult<Ecg?> {
        val curationResponse = rawECG?.name?.let {
            CurationResponse.createFromConfigs(
                comorbidityCuration.find(it), patientId, CurationCategory.ECG, it, "ECG", true
            )
        }
        val ecg = when (curationResponse?.configs?.size) {
            0 -> rawECG
            1 -> {
                curationResponse.config()?.takeUnless { it.ignore }?.curated?.let { curated ->
                    val curatedEcg = curated as? Ecg
                    rawECG.copy(
                        name = curated.name?.ifEmpty { null },
                        qtcfMeasure = curatedEcg?.qtcfMeasure,
                        jtcMeasure = curatedEcg?.jtcMeasure
                    )
                }
            }
            else -> null
        }
        return ExtractionResult(ecg, curationResponse?.extractionEvaluation ?: CurationExtractionEvaluation())
    }

    private fun curateInfection(patientId: String, rawInfectionStatus: InfectionStatus?): ExtractionResult<InfectionStatus?> {
        val curationResponse = rawInfectionStatus?.description?.let {
            CurationResponse.createFromConfigs(
                infectionCuration.find(it), patientId, CurationCategory.INFECTION, it, "infection", true
            )
        }
        val infectionStatus = when (curationResponse?.configs?.size) {
            0 -> {
                rawInfectionStatus
            }

            1 -> {
                val config = curationResponse.configs.first()
                if (config.ignore) null else rawInfectionStatus.copy(description = config.interpretation.ifEmpty { null })
            }

            else -> {
                null
            }
        }
        return ExtractionResult(infectionStatus, curationResponse?.extractionEvaluation ?: CurationExtractionEvaluation())
    }

    private fun determineLVEF(nonOncologicalHistoryEntries: List<String>?): Double? {
        // We do not raise warnings or propagate evaluated inputs here since we use the same configs for otherConditions
        return nonOncologicalHistoryEntries?.asSequence()
            ?.flatMap(comorbidityCuration::find)
            ?.filterNot(CurationConfig::ignore)
            ?.firstNotNullOfOrNull { it.lvef }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            ClinicalStatusExtractor(
                infectionCuration = curationDatabaseContext.infectionCuration,
                comorbidityCuration = curationDatabaseContext.comorbidityCuration
            )
    }
}