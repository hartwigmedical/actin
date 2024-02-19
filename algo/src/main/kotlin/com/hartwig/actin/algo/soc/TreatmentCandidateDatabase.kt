package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

const val BEVACIZUMAB = "BEVACIZUMAB"
const val CAPECITABINE = "CAPECITABINE"
const val CAPIRI = "CAPIRI"
const val CAPOX = "CAPOX"
const val CETUXIMAB = "CETUXIMAB"
const val ENCORAFENIB_CETUXIMAB = "CETUXIMAB+ENCORAFENIB"
const val ENTRECTINIB = "ENTRECTINIB"
const val FOLFIRI = "FOLFIRI"
const val FOLFIRI_CETUXIMAB = "FOLFIRI+CETUXIMAB"
const val FOLFIRI_PANITUMUMAB = "FOLFIRI+PANITUMUMAB"
const val FOLFOXIRI = "FOLFOXIRI"
const val FOLFOX = "FOLFOX"
const val FOLFOX_CETUXIMAB = "FOLFOX+CETUXIMAB"
const val FOLFOX_PANITUMUMAB = "FOLFOX+PANITUMUMAB"
const val FOLINIC_ACID = "FOLINIC_ACID"
const val FLUOROURACIL = "FLUOROURACIL"
const val IRINOTECAN = "IRINOTECAN"
const val IRINOTECAN_CETUXIMAB = "IRINOTECAN+CETUXIMAB"
const val IRINOTECAN_PANITUMUMAB = "IRINOTECAN+PANITUMUMAB"
const val LAROTRECTINIB = "LAROTRECTINIB"
const val TRIFLURIDINE_TIPIRACIL = "TRIFLURIDINE+TIPIRACIL"
const val NIVOLUMAB = "NIVOLUMAB"
const val OXALIPLATIN = "OXALIPLATIN"
const val PANITUMUMAB = "PANITUMUMAB"
const val PEMBROLIZUMAB = "PEMBROLIZUMAB"
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
                createTreatmentCandidate(treatmentName, setOf(2, 3, 4, 5))
            }

            TRIFLURIDINE_TIPIRACIL -> {
                createTreatmentCandidate(treatmentName, setOf(3, 4, 5), optional = true)
            }

            ENTRECTINIB, LAROTRECTINIB -> {
                createTreatmentCandidate(treatmentName, optional = true)
            }

            else -> {
                throw IllegalArgumentException("Unknown treatment name: $treatmentName")
            }
        }
    }

    fun treatmentCandidateWithBevacizumab(treatmentName: String): TreatmentCandidate {
        return createTreatmentCandidate("$treatmentName+$BEVACIZUMAB", setOf(1), optional = true)
    }

    private fun createTreatmentCandidate(
        treatmentName: String, treatmentLines: Set<Int> = emptySet(), optional: Boolean = false
    ): TreatmentCandidate {
        val treatment = treatmentDatabase.findTreatmentByName(treatmentName)
            ?: throw IllegalArgumentException("Unknown treatment name: $treatmentName")
        val treatmentLineFunctions = if (treatmentLines.isEmpty()) emptySet() else setOf(eligibleForTreatmentLines(treatmentLines))
        return TreatmentCandidate(
            treatment = treatment,
            optional = optional,
            eligibilityFunctions = treatmentLineFunctions + drugBasedExclusionsForTreatment(treatment)
        )
    }

    private fun drugBasedExclusionsForTreatment(treatment: Treatment): EligibilityFunction {
        val mutuallyExclusiveMonotherapies = setOf(CAPECITABINE, FLUOROURACIL)
        if (treatment.name in mutuallyExclusiveMonotherapies) {
            return EligibilityFunction(EligibilityRule.AND, mutuallyExclusiveMonotherapies.map(::eligibleIfTreatmentNotInHistory))
        }
        
        val treatmentDrugs = (treatment as DrugTreatment).drugs.map(Drug::name).toSet()
        val additionalDrugsToExclude = if (treatmentDrugs.intersect(antiEgfrDrugs).isEmpty()) emptyList() else antiEgfrDrugs
        val drugExclusionRule = eligibleIfDrugsNotInHistory(treatmentDrugs - drugExclusionExceptions + additionalDrugsToExclude)

        return if (treatmentDrugs.intersect(drugExclusionExceptions).isEmpty()) drugExclusionRule else {
            appendSpecificTreatmentExclusion(drugExclusionRule, treatment)
        }
    }

    private fun appendSpecificTreatmentExclusion(
        drugExclusionRule: EligibilityFunction,
        treatment: Treatment
    ) = eligibilityFunction(EligibilityRule.AND, drugExclusionRule, eligibleIfTreatmentNotInHistory(treatment.name))

    companion object {
        private val drugExclusionExceptions = setOf(FLUOROURACIL, CAPECITABINE, FOLINIC_ACID)
        private val antiEgfrDrugs = setOf(CETUXIMAB, PANITUMUMAB)

        private fun eligibleIfDrugsNotInHistory(drugNames: Iterable<String>): EligibilityFunction {
            val drugParameter = drugNames.joinToString(";")
            return eligibilityFunction(
                EligibilityRule.NOT,
                eligibilityFunction(
                    EligibilityRule.OR,
                    eligibilityFunction(
                        EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_WITHIN_Y_WEEKS, drugParameter, RECENT_TREATMENT_THRESHOLD_WEEKS
                    ),
                    eligibilityFunction(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_TREATMENT_WITH_ANY_DRUG_X, drugParameter),
                )
            )
        }

        private fun eligibleIfTreatmentNotInHistory(treatmentName: String): EligibilityFunction {
            return eligibilityFunction(
                EligibilityRule.NOT,
                eligibilityFunction(
                    EligibilityRule.OR,
                    eligibilityFunction(
                        EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS, treatmentName, RECENT_TREATMENT_THRESHOLD_WEEKS
                    ),
                    eligibilityFunction(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT, treatmentName)
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