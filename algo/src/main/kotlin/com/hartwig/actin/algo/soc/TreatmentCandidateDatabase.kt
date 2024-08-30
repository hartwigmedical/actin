package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

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
const val TRIFLURIDINE_TIPIRACIL_BEVACIZUMAB = "TRIFLURIDINE+TIPIRACIL+BEVACIZUMAB"
private const val RECENT_TREATMENT_THRESHOLD_WEEKS = "26"

private val drugExclusionExceptions = setOf(FLUOROURACIL, CAPECITABINE, FOLINIC_ACID)
private val antiEgfrDrugs = setOf(CETUXIMAB, PANITUMUMAB)
private val mutuallyExclusiveMonotherapies = setOf(CAPECITABINE, FLUOROURACIL)

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

            TRIFLURIDINE_TIPIRACIL_BEVACIZUMAB -> {
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
        val (drugBasedEligibility, drugBasedCriteriaForRequirement) = drugBasedExclusionsForTreatment(treatment as DrugTreatment)
        return TreatmentCandidate(
            treatment = treatment,
            optional = optional,
            eligibilityFunctions = treatmentLineFunctions + drugBasedEligibility,
            additionalCriteriaForRequirement = drugBasedCriteriaForRequirement
        )
    }

    private fun drugBasedExclusionsForTreatment(treatment: DrugTreatment): Pair<Set<EligibilityFunction>, Set<EligibilityFunction>> {
        if (treatment.name in mutuallyExclusiveMonotherapies) {
            return Pair(
                mutuallyExclusiveMonotherapies.map(::eligibleIfTreatmentNotInRecentHistoryOrWithPD).toSet(),
                setOf(eligibleIfDrugsNotInHistory(mutuallyExclusiveMonotherapies))
            )
        }

        val treatmentDrugs = treatment.drugs.map(Drug::name).toSet()
        val additionalDrugsToExclude = if (treatmentDrugs.intersect(antiEgfrDrugs).isEmpty()) emptyList() else antiEgfrDrugs
        val drugsToExclude = treatmentDrugs - drugExclusionExceptions + additionalDrugsToExclude
        val treatmentToExclude = if (treatmentDrugs.intersect(drugExclusionExceptions).isNotEmpty()) treatment.name else null

        val drugExclusionRules = setOfNotNull(
            eligibleIfDrugsNotInRecentHistoryOrWithPD(drugsToExclude),
            treatmentToExclude?.let(::eligibleIfTreatmentNotInRecentHistoryOrWithPD)
        )
        val drugCriteriaForRequirement = setOfNotNull(
            eligibleIfDrugsNotInHistory(drugsToExclude),
            treatmentToExclude?.let(::eligibleIfTreatmentNotInHistory)
        )
        return Pair(drugExclusionRules, drugCriteriaForRequirement)
    }

    private fun eligibleIfDrugsNotInRecentHistoryOrWithPD(drugNames: Iterable<String>): EligibilityFunction {
        val drugParameter = drugNames.joinToString(";")
        return eligibilityFunction(
            EligibilityRule.NOT,
            eligibilityFunction(
                EligibilityRule.OR,
                eligibilityFunction(
                    EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS, drugParameter, RECENT_TREATMENT_THRESHOLD_WEEKS
                ),
                eligibilityFunction(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_TREATMENT_WITH_ANY_DRUG_X, drugParameter),
            )
        )
    }

    private fun eligibleIfDrugsNotInHistory(drugNames: Iterable<String>): EligibilityFunction {
        return eligibilityFunction(
            EligibilityRule.NOT,
            eligibilityFunction(EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X, drugNames.joinToString(";"))
        )
    }

    private fun eligibleIfTreatmentNotInRecentHistoryOrWithPD(treatmentName: String): EligibilityFunction {
        return eligibilityFunction(
            EligibilityRule.NOT,
            eligibilityFunction(
                EligibilityRule.OR,
                eligibilityFunction(
                    EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS, treatmentName, RECENT_TREATMENT_THRESHOLD_WEEKS
                ),
                eligibilityFunction(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT, treatmentName)
            )
        )
    }

    private fun eligibleIfTreatmentNotInHistory(treatmentName: String): EligibilityFunction {
        return eligibilityFunction(
            EligibilityRule.NOT,
            eligibilityFunction(EligibilityRule.HAS_HAD_TREATMENT_NAME_X, treatmentName)
        )
    }

    private fun eligibleForTreatmentLines(lines: Set<Int>): EligibilityFunction {
        return eligibilityFunction(EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_LINES_X, lines.joinToString(";"))
    }

    private fun eligibilityFunction(rule: EligibilityRule, vararg parameters: Any): EligibilityFunction {
        return EligibilityFunction(rule = rule, parameters = listOf(*parameters))
    }
}