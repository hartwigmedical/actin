package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction

private const val STRONG = "Strg"
private const val MODERATE = "Mod"
private const val WEAK = "WK"
private const val SENSITIVE = "SENS"
private const val MODERATE_SENSITIVE = "$MODERATE $SENSITIVE"
private const val INHIBITOR = "INH"
private const val INDUCER = "IND"
private const val SUBSTRATE = "SUB"

class CypInteractionConfigFactory : CurationConfigFactory<CypInteractionConfig> {

    override fun create(fields: Map<String, Int>, parts: Array<String>): CypInteractionConfig {
        val strongInhibitors = extractInterations(parts, fields, fieldName(STRONG, INHIBITOR), CypInteraction.Strength.STRONG, CypInteraction.Type.INHIBITOR)
        val moderateInhibitors = extractInterations(parts, fields, fieldName(WEAK, INHIBITOR), CypInteraction.Strength.WEAK, CypInteraction.Type.INHIBITOR)
        val weakInhibitors = extractInterations(parts, fields, fieldName(MODERATE, INHIBITOR), CypInteraction.Strength.MODERATE, CypInteraction.Type.INHIBITOR)
        val strongInducers = extractInterations(parts, fields, fieldName(STRONG, INDUCER), CypInteraction.Strength.STRONG, CypInteraction.Type.INDUCER)
        val moderateInducers = extractInterations(parts, fields, fieldName(WEAK, INDUCER), CypInteraction.Strength.WEAK, CypInteraction.Type.INDUCER)
        val weakInducers = extractInterations(parts, fields, fieldName(MODERATE, INDUCER), CypInteraction.Strength.MODERATE, CypInteraction.Type.INDUCER)
        val sensitiveSubstrates = extractInterations(parts, fields, fieldName(SENSITIVE, SUBSTRATE), CypInteraction.Strength.SENSITIVE, CypInteraction.Type.SUBSTRATE)
        val moderateSensitiveSubstrates = extractInterations(parts, fields, fieldName(MODERATE_SENSITIVE, SUBSTRATE), CypInteraction.Strength.MODERATE_SENSITIVE, CypInteraction.Type.SUBSTRATE)
        val interactions = strongInhibitors + moderateInhibitors + weakInhibitors + strongInducers + moderateInducers + weakInducers + sensitiveSubstrates + moderateSensitiveSubstrates
        return CypInteractionConfig(input = parts[fields["Drug or Other Substance"]!!], ignore = false, interactions = interactions)
    }

    private fun fieldName(strength: String, type: String): String {
        return "CYP $strength $type"
    }

    private fun extractInterations(parts: Array<String>, fields: Map<String, Int>, fieldName: String, strength: CypInteraction.Strength, type: CypInteraction.Type) =
        parts[fields[fieldName]!!].split(";").map { it.trim() }.filter { it.isNotEmpty() }.map { cyp -> ImmutableCypInteraction.builder().cyp(cyp).strength(strength).type(type).build() }

}