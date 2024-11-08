package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.DrugInteraction

private const val STRONG = "Strg"
private const val MODERATE = "Mod"
private const val WEAK = "WK"
private const val SENSITIVE = "SENS"
private const val MODERATE_SENSITIVE = "$MODERATE $SENSITIVE"
private const val INHIBITOR = "INH"
private const val INDUCER = "IND"
private const val SUBSTRATE = "SUB"

class DrugInteractionConfigFactory : CurationConfigFactory<DrugInteractionConfig> {

    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<DrugInteractionConfig> {
        val strongInhibitors =
            extractInteractions(
                parts,
                fields,
                fieldName(STRONG, INHIBITOR),
                DrugInteraction.Strength.STRONG,
                DrugInteraction.Type.INHIBITOR
            )
        val moderateInhibitors =
            extractInteractions(parts, fields, fieldName(WEAK, INHIBITOR), DrugInteraction.Strength.WEAK, DrugInteraction.Type.INHIBITOR)
        val weakInhibitors = extractInteractions(
            parts,
            fields,
            fieldName(MODERATE, INHIBITOR),
            DrugInteraction.Strength.MODERATE,
            DrugInteraction.Type.INHIBITOR
        )
        val strongInducers =
            extractInteractions(parts, fields, fieldName(STRONG, INDUCER), DrugInteraction.Strength.STRONG, DrugInteraction.Type.INDUCER)
        val moderateInducers =
            extractInteractions(parts, fields, fieldName(WEAK, INDUCER), DrugInteraction.Strength.WEAK, DrugInteraction.Type.INDUCER)
        val weakInducers =
            extractInteractions(
                parts,
                fields,
                fieldName(MODERATE, INDUCER),
                DrugInteraction.Strength.MODERATE,
                DrugInteraction.Type.INDUCER
            )
        val sensitiveSubstrates = extractInteractions(
            parts,
            fields,
            fieldName(SENSITIVE, SUBSTRATE),
            DrugInteraction.Strength.SENSITIVE,
            DrugInteraction.Type.SUBSTRATE
        )
        val moderateSensitiveSubstrates = extractInteractions(
            parts,
            fields,
            fieldName(MODERATE_SENSITIVE, SUBSTRATE),
            DrugInteraction.Strength.MODERATE_SENSITIVE,
            DrugInteraction.Type.SUBSTRATE
        )
        val transporterSubstrates =
            extractInteractions(parts, fields, "TRNSP SUB", DrugInteraction.Strength.STRONG, DrugInteraction.Type.SUBSTRATE)
        val transporterInhibitors =
            extractInteractions(parts, fields, "TRNSP INH", DrugInteraction.Strength.STRONG, DrugInteraction.Type.INHIBITOR)
        val interactions =
            strongInhibitors + moderateInhibitors + weakInhibitors + strongInducers + moderateInducers + weakInducers + sensitiveSubstrates + moderateSensitiveSubstrates
        val transporterInteractions = transporterSubstrates + transporterInhibitors
        return ValidatedCurationConfig(
            DrugInteractionConfig(
                input = parts[fields["Drug or Other Substance"]!!],
                ignore = false,
                cypInteractions = interactions,
                transporterInteractions = transporterInteractions
            ), emptyList()
        )
    }

    private fun fieldName(strength: String, type: String): String {
        return "CYP $strength $type"
    }

    private fun extractInteractions(
        parts: Array<String>, fields: Map<String, Int>, fieldName: String, strength: DrugInteraction.Strength, type: DrugInteraction.Type
    ) = parts[fields[fieldName]!!].split(";").map { it.trim() }.filter { it.isNotEmpty() }
        .map { cyp -> DrugInteraction(name = cyp, strength = strength, type = type) }

}