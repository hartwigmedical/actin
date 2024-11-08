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
        val strongCypInhibitors =
            extractInteractions(
                parts,
                fields,
                fieldName(STRONG, INHIBITOR),
                DrugInteraction.Strength.STRONG,
                DrugInteraction.Type.INHIBITOR
            )
        val moderateCypInhibitors =
            extractInteractions(parts, fields, fieldName(WEAK, INHIBITOR), DrugInteraction.Strength.WEAK, DrugInteraction.Type.INHIBITOR)
        val weakCypInhibitors = extractInteractions(
            parts,
            fields,
            fieldName(MODERATE, INHIBITOR),
            DrugInteraction.Strength.MODERATE,
            DrugInteraction.Type.INHIBITOR
        )
        val strongCypInducers =
            extractInteractions(parts, fields, fieldName(STRONG, INDUCER), DrugInteraction.Strength.STRONG, DrugInteraction.Type.INDUCER)
        val moderateCypInducers =
            extractInteractions(parts, fields, fieldName(WEAK, INDUCER), DrugInteraction.Strength.WEAK, DrugInteraction.Type.INDUCER)
        val weakCypInducers =
            extractInteractions(
                parts,
                fields,
                fieldName(MODERATE, INDUCER),
                DrugInteraction.Strength.MODERATE,
                DrugInteraction.Type.INDUCER
            )
        val sensitiveCypSubstrates = extractInteractions(
            parts,
            fields,
            fieldName(SENSITIVE, SUBSTRATE),
            DrugInteraction.Strength.SENSITIVE,
            DrugInteraction.Type.SUBSTRATE
        )
        val moderateSensitiveCypSubstrates = extractInteractions(
            parts,
            fields,
            fieldName(MODERATE_SENSITIVE, SUBSTRATE),
            DrugInteraction.Strength.MODERATE_SENSITIVE,
            DrugInteraction.Type.SUBSTRATE
        )
        val transporterSubstrates =
            extractInteractions(parts, fields, "TRNSP SUB", DrugInteraction.Strength.UNKNOWN, DrugInteraction.Type.SUBSTRATE)
        val transporterInhibitors =
            extractInteractions(parts, fields, "TRNSP INH", DrugInteraction.Strength.UNKNOWN, DrugInteraction.Type.INHIBITOR)
        val cypInteractions =
            strongCypInhibitors + moderateCypInhibitors + weakCypInhibitors + strongCypInducers + moderateCypInducers + weakCypInducers + sensitiveCypSubstrates + moderateSensitiveCypSubstrates
        val transporterInteractions = transporterSubstrates + transporterInhibitors
        return ValidatedCurationConfig(
            DrugInteractionConfig(
                input = parts[fields["Drug or Other Substance"]!!],
                ignore = false,
                cypInteractions = cypInteractions,
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