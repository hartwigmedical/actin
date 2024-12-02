package com.hartwig.actin.clinical

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import java.io.File

data class DrugInteractions(
    val cypInteractions: List<DrugInteraction>,
    val transporterInteractions: List<DrugInteraction>
)

data class DrugInteractionEntry(
    val drug: String,
    val cypStrongInhibitor: String,
    val cypModerateInhibitor: String,
    val cypWeakInhibitor: String,
    val cypStrongInducer: String,
    val cypModerateInducer: String,
    val cypWeakInducer: String,
    val cypSensitiveSubstrate: String,
    val cypModerateSensitiveSubstrate: String,
    val trnspInh: String,
    val trnspSub: String,
)

class DrugInteractionsDatabase(private val interactions: Map<String, DrugInteractions>) {

    fun curateMedicationCypInteractions(
        medicationName: String
    ): List<DrugInteraction> {
        return interactions[medicationName.lowercase()]?.cypInteractions ?: emptyList()
    }

    fun curateMedicationTransporterInteractions(
        medicationName: String
    ): List<DrugInteraction> {
        return interactions[medicationName.lowercase()]?.transporterInteractions ?: emptyList()
    }

    companion object {
        fun read(tsv: String): DrugInteractionsDatabase {
            val reader = CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(DrugInteractionEntry::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))
            val interactions = reader.readValues<DrugInteractionEntry>(File(tsv)).readAll()
                .groupBy { it.drug.lowercase() }
                .mapValues { (_, entries) ->
                    val cypInteractions = entries.flatMap { entry ->
                        listOf(
                            extractInteractions(
                                entry.cypStrongInhibitor,
                                DrugInteraction.Strength.STRONG,
                                DrugInteraction.Type.INHIBITOR,
                                DrugInteraction.Group.CYP
                            ),
                            extractInteractions(
                                entry.cypModerateInhibitor,
                                DrugInteraction.Strength.MODERATE,
                                DrugInteraction.Type.INHIBITOR,
                                DrugInteraction.Group.CYP
                            ),
                            extractInteractions(
                                entry.cypWeakInhibitor,
                                DrugInteraction.Strength.WEAK,
                                DrugInteraction.Type.INHIBITOR,
                                DrugInteraction.Group.CYP
                            ),
                            extractInteractions(
                                entry.cypStrongInducer,
                                DrugInteraction.Strength.STRONG,
                                DrugInteraction.Type.INDUCER,
                                DrugInteraction.Group.CYP
                            ),
                            extractInteractions(
                                entry.cypModerateInducer,
                                DrugInteraction.Strength.MODERATE,
                                DrugInteraction.Type.INDUCER,
                                DrugInteraction.Group.CYP
                            ),
                            extractInteractions(
                                entry.cypWeakInducer,
                                DrugInteraction.Strength.WEAK,
                                DrugInteraction.Type.INDUCER,
                                DrugInteraction.Group.CYP
                            ),
                            extractInteractions(
                                entry.cypSensitiveSubstrate,
                                DrugInteraction.Strength.SENSITIVE,
                                DrugInteraction.Type.SUBSTRATE,
                                DrugInteraction.Group.CYP
                            ),
                            extractInteractions(
                                entry.cypModerateSensitiveSubstrate,
                                DrugInteraction.Strength.MODERATE_SENSITIVE,
                                DrugInteraction.Type.SUBSTRATE,
                                DrugInteraction.Group.CYP
                            )
                        ).flatten()
                    }
                    val transporterInteractions = entries.flatMap { entry ->
                        listOf(
                            extractInteractions(
                                entry.trnspSub,
                                DrugInteraction.Strength.UNKNOWN,
                                DrugInteraction.Type.SUBSTRATE,
                                DrugInteraction.Group.TRANSPORTER
                            ),
                            extractInteractions(
                                entry.trnspInh,
                                DrugInteraction.Strength.UNKNOWN,
                                DrugInteraction.Type.INHIBITOR,
                                DrugInteraction.Group.TRANSPORTER
                            )
                        ).flatten()
                    }
                    DrugInteractions(cypInteractions = cypInteractions, transporterInteractions = transporterInteractions)
                }
            return DrugInteractionsDatabase(interactions)
        }

        private fun extractInteractions(
            entry: String,
            strength: DrugInteraction.Strength,
            type: DrugInteraction.Type,
            group: DrugInteraction.Group
        ) = entry.split(";").map { it.trim() }.filter { it.isNotEmpty() }
            .map { name -> DrugInteraction(name = name, strength = strength, type = type, group = group) }
    }
}