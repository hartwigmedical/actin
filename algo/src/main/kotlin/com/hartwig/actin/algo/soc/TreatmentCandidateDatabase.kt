package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

const val CAPECITABINE = "CAPECITABINE"
const val CAPIRI = "CAPIRI"
const val CAPOX = "CAPOX"
const val CETUXIMAB = "CETUXIMAB"
const val ENCORAFENIB_CETUXIMAB = "CETUXIMAB+ENCORAFENIB"
const val FOLFIRI = "FOLFIRI"
const val FOLFIRI_CETUXIMAB = "FOLFIRI+CETUXIMAB"
const val FOLFIRI_PANITUMUMAB = "FOLFIRI+PANITUMUMAB"
const val FOLFOXIRI = "FOLFOXIRI"
const val FOLFOX = "FOLFOX"
const val FOLFOX_CETUXIMAB = "FOLFOX+CETUXIMAB"
const val FOLFOX_PANITUMUMAB = "FOLFOX+PANITUMUMAB"
const val FLUOROURACIL = "FLUOROURACIL"
const val IRINOTECAN = "IRINOTECAN"
const val IRINOTECAN_CETUXIMAB = "IRINOTECAN+CETUXIMAB"
const val IRINOTECAN_PANITUMUMAB = "IRINOTECAN+PANITUMUMAB"
const val LONSURF = "LONSURF"
const val NIVOLUMAB = "NIVOLUMAB"
const val OXALIPLATIN = "OXALIPLATIN"
const val PANITUMUMAB = "PANITUMUMAB"
const val PEMBROLIZUMAB = "PEMBROLIZUMAB"
private const val CHEMO_MAX_CYCLES = "12"
private const val RECENT_TREATMENT_THRESHOLD_WEEKS = "26"

class TreatmentCandidateDatabase(val treatmentDatabase: TreatmentDatabase) {

    fun treatmentCandidate(treatmentName: String): TreatmentCandidate {
        return when (treatmentName) {
            CAPECITABINE, CAPIRI, CAPOX, FOLFOX, FOLFIRI, FOLFOXIRI, FLUOROURACIL, OXALIPLATIN, PEMBROLIZUMAB -> {
                createTreatmentCandidate(treatmentName)
            }

            FOLFOX_CETUXIMAB, FOLFOX_PANITUMUMAB -> {
                createTreatmentCandidate(treatmentName, setOf(1))
            }

            FOLFIRI_CETUXIMAB, FOLFIRI_PANITUMUMAB, IRINOTECAN_CETUXIMAB, IRINOTECAN_PANITUMUMAB -> {
                createTreatmentCandidate(treatmentName, setOf(1, 2))
            }

            CETUXIMAB, ENCORAFENIB_CETUXIMAB, NIVOLUMAB, PANITUMUMAB, IRINOTECAN -> {
                createTreatmentCandidate(treatmentName, setOf(2, 3))
            }

            LONSURF -> {
                createTreatmentCandidate(treatmentName, setOf(3))
            }

            else -> {
                throw IllegalArgumentException("Unknown treatment name: $treatmentName")
            }
        }
    }

    fun treatmentCandidateWithBevacizumab(treatmentName: String): TreatmentCandidate {
        return createTreatmentCandidate("$treatmentName+BEVACIZUMAB", setOf(1))
    }

    private fun createTreatmentCandidate(treatmentName: String, treatmentLines: Set<Int> = emptySet()): TreatmentCandidate {
        val treatment = treatmentDatabase.findTreatmentByName(treatmentName)
            ?: throw IllegalArgumentException("Unknown treatment name: $treatmentName")
        val treatmentLineFunctions = if (treatmentLines.isEmpty()) emptySet() else setOf(eligibleForTreatmentLines(treatmentLines))
        return TreatmentCandidate(
            treatment = treatment,
            isOptional = false,
            eligibilityFunctions = drugBasedExclusionsForTreatment(treatment) + treatmentLineFunctions
        )
    }

    private fun drugBasedExclusionsForTreatment(treatment: Treatment): Set<EligibilityFunction> {
        val additionalDrugsToExclude = when (treatment.name) {
            CETUXIMAB -> listOf(PANITUMUMAB)
            PANITUMUMAB -> listOf(CETUXIMAB)
            else -> emptyList()
        }
        val drugsToExclude = (treatment as DrugTreatment).drugs.map(Drug::name) + additionalDrugsToExclude
        return drugsToExclude.map(::eligibleIfDrugNotInHistory).toSet()
    }

    companion object {
        private fun eligibleIfDrugNotInHistory(drugName: String): EligibilityFunction {
            // TODO: Create drug-based versions of these rules
            return eligibilityFunction(
                EligibilityRule.NOT,
                eligibilityFunction(
                    EligibilityRule.OR,
                    eligibilityFunction(
                        EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS,
                        drugName,
                        RECENT_TREATMENT_THRESHOLD_WEEKS
                    ),
                    eligibilityFunction(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT, drugName),
                    eligibilityFunction(
                        EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES,
                        drugName, CHEMO_MAX_CYCLES, CHEMO_MAX_CYCLES
                    )
                )
            )
        }

        private fun eligibleForTreatmentLines(lines: Set<Int>): EligibilityFunction {
            return eligibilityFunction(EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_LINES_X, lines.joinToString(";"))
        }

        private fun eligibilityFunction(rule: EligibilityRule, vararg parameters: Any): EligibilityFunction {
            return EligibilityFunction(rule = rule, parameters = listOf(*parameters))
        }
    }
}