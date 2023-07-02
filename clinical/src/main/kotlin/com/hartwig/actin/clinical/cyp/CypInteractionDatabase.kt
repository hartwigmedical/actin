package com.hartwig.actin.clinical.cyp

import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

private const val TAB = "\t"


fun fieldName(strength: String, type: String): String {
    return "CYP $strength $type"
}

private const val STRONG = "Strg"
private const val MODERATE = "Mod"
private const val WEAK = "WK"
private const val INHIBITOR = "INH"
private const val INDUCER = "IND"
private const val SENSITIVE_SUBSTRATE = "SENS SUB"

class CypInteractionDatabase(private val interactionsByName: Map<String, List<CypInteraction>>) {

    fun findByName(name: String): List<CypInteraction> {
        return interactionsByName[name] ?: emptyList();
    }

    companion object {
        fun readFromFile(path: String): CypInteractionDatabase {
            val lines = Files.readAllLines(File(path).toPath())
            val fields = TabularFile.createFields(lines[0].split(TAB).dropLastWhile { it.isEmpty() }.toTypedArray())
            val interactions = lines.subList(1, lines.size).map {
                val tabSplit = it.split(TAB)
                val strongInhibitors = extractInterations(tabSplit, fields, fieldName(STRONG, INHIBITOR), CypInteraction.Strength.STRONG, CypInteraction.Type.INHIBITOR)
                val moderateInhibitors = extractInterations(tabSplit, fields, fieldName(WEAK, INHIBITOR), CypInteraction.Strength.WEAK, CypInteraction.Type.INHIBITOR)
                val weakInhibitors = extractInterations(tabSplit, fields, fieldName(MODERATE, INHIBITOR), CypInteraction.Strength.MODERATE, CypInteraction.Type.INHIBITOR)
                val strongInducers = extractInterations(tabSplit, fields, fieldName(STRONG, INDUCER), CypInteraction.Strength.STRONG, CypInteraction.Type.INDUCER)
                val moderateInducers = extractInterations(tabSplit, fields, fieldName(WEAK, INDUCER), CypInteraction.Strength.WEAK, CypInteraction.Type.INDUCER)
                val weakInducers = extractInterations(tabSplit, fields, fieldName(MODERATE, INDUCER), CypInteraction.Strength.MODERATE, CypInteraction.Type.INDUCER)
                val sensitiveSubstrates = extractInterations(tabSplit, fields, fieldName(STRONG, SENSITIVE_SUBSTRATE), CypInteraction.Strength.NONE, CypInteraction.Type.SENSITIVE_SUBSTRATE)
                val moderateSensitiveSubstrates = extractInterations(tabSplit, fields, fieldName(WEAK, SENSITIVE_SUBSTRATE), CypInteraction.Strength.WEAK, CypInteraction.Type.SENSITIVE_SUBSTRATE)
                tabSplit[fields["name"]!!] to strongInhibitors + moderateInhibitors + weakInhibitors + strongInducers + moderateInducers + weakInducers + sensitiveSubstrates + moderateSensitiveSubstrates
            }
            return CypInteractionDatabase(interactions.toMap())
        }

        private fun extractInterations(tabSplit: List<String>, fields: Map<String, Int>, fieldName: String, strength: CypInteraction.Strength, type: CypInteraction.Type) =
            tabSplit[fields[fieldName]!!].split(";").map { cyp -> ImmutableCypInteraction.builder().cyp(cyp).strength(strength).type(type).build() }
    }
}