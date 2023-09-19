package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction

class RecommendationDatabase(val treatmentDatabase: TreatmentDatabase) {
    fun treatmentCandidatesForDoidSet(doids: Set<String>): List<TreatmentCandidate> {
        return if (!doids.contains(DoidConstants.COLORECTAL_CANCER_DOID)) emptyList() else listOf(
            combinableChemotherapies(),
            combinableChemotherapies().map(::addBevacizumabToTreatment),
            antiEGFRTherapies(),
            antiEGFRTherapies().flatMap { therapy: TreatmentCandidate ->
                combinableChemotherapies().map { chemo: TreatmentCandidate ->
                    therapy.copy(
                        treatment = treatmentDatabase.findTreatmentByName("${chemo.treatment.name()}+${therapy.treatment.name()}")!!,
                        expectedBenefitScore = SCORE_MONOTHERAPY_PLUS_TARGETED,
                    )
                }
            },
            otherTreatments()
        ).flatten()
    }

    private fun combinableChemotherapies(): List<TreatmentCandidate> {
        return listOf(
            createChemotherapy("FLUOROURACIL", SCORE_MONOTHERAPY),
            createChemotherapy("CAPECITABINE", SCORE_MONOTHERAPY),
            createChemotherapy("IRINOTECAN", SCORE_MONOTHERAPY),
            createChemotherapy("OXALIPLATIN", -1),
            createMultiChemotherapy(TREATMENT_CAPOX),
            createChemotherapy(
                TREATMENT_FOLFIRI,
                SCORE_MULTITHERAPY,
                false,
                setOf(1, 2), setOf(
                    eligibleIfTreatmentNotInHistory(TREATMENT_CAPOX), eligibleIfTreatmentNotInHistory(TREATMENT_FOLFOX),
                    IS_YOUNG_AND_FIT
                )
            ),
            createMultiChemotherapy(TREATMENT_FOLFOX),
            createMultiChemotherapy(
                TREATMENT_FOLFIRINOX
            )
        )
    }

    private fun createMultiChemotherapy(name: String): TreatmentCandidate {
        return createChemotherapy(name, SCORE_MULTITHERAPY, false, setOf(1, 2), setOf(IS_YOUNG_AND_FIT))
    }

    private fun createChemotherapy(
        name: String, score: Int, isOptional: Boolean = false, lines: Set<Int> = setOf(1, 2),
        extraFunctions: Set<EligibilityFunction> = emptySet()
    ): TreatmentCandidate {
        return TreatmentCandidate(
            treatment = treatmentDatabase.findTreatmentByName(name)!!,
            expectedBenefitScore = score,
            isOptional = isOptional,
            eligibilityFunctions = setOf(IS_COLORECTAL_CANCER, eligibleIfTreatmentNotInHistory(name), eligibleForTreatmentLines(lines))
                    union extraFunctions
        )
    }

    private fun addBevacizumabToTreatment(treatmentCandidate: TreatmentCandidate): TreatmentCandidate {
        return TreatmentCandidate(
            treatmentDatabase.findTreatmentByName(treatmentCandidate.treatment.name() + "+Bevacizumab")!!,
            isOptional = treatmentCandidate.isOptional,
            treatmentCandidate.expectedBenefitScore.coerceAtLeast(SCORE_MONOTHERAPY_PLUS_TARGETED),
            treatmentCandidate.eligibilityFunctions
        )
    }

    private fun antiEGFRTherapies(): List<TreatmentCandidate> {
        return listOf(
            createAntiEGFRTherapy(TREATMENT_CETUXIMAB),
            createAntiEGFRTherapy(TREATMENT_PANITUMUMAB)
        )
    }

    private fun createAntiEGFRTherapy(name: String): TreatmentCandidate {
        return TreatmentCandidate(
            treatmentDatabase.findTreatmentByName(name)!!,
            isOptional = false,
            expectedBenefitScore = SCORE_TARGETED_THERAPY,
            eligibilityFunctions = setOf(
                IS_COLORECTAL_CANCER,
                eligibleIfGenesAreWildType(listOf("KRAS", "NRAS", "BRAF")),
                eligibilityFunction(EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR),
                eligibleForTreatmentLines(setOf(2, 3))
            )
        )
    }

    private fun otherTreatments(): List<TreatmentCandidate> {
        return listOf(
            createChemotherapy(
                TREATMENT_LONSURF,
                SCORE_LONSURF,
                true, setOf(3),
                setOf(eligibleIfTreatmentNotInHistory("trifluridine"))
            ),
            TreatmentCandidate(
                treatment = treatmentDatabase.findTreatmentByName(TREATMENT_PEMBROLIZUMAB)!!,
                isOptional = false,
                expectedBenefitScore = SCORE_PEMBROLIZUMAB,
                eligibilityFunctions = setOf(
                    IS_COLORECTAL_CANCER, eligibilityFunction(EligibilityRule.MSI_SIGNATURE),
                    eligibleForTreatmentLines(setOf(1, 2))
                )
            ),
            TreatmentCandidate(
                treatment = treatmentDatabase.findTreatmentByName("Cetuximab+Encorafenib")!!,
                isOptional = false,
                expectedBenefitScore = SCORE_CETUXIMAB_PLUS_ENCORAFENIB,
                eligibilityFunctions = setOf(
                    IS_COLORECTAL_CANCER,
                    eligibilityFunction(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, "BRAF"),
                    eligibleForTreatmentLines(setOf(2, 3))
                )
            )
        )
    }

    companion object {
        const val TREATMENT_CAPOX = "CAPOX"
        const val TREATMENT_CETUXIMAB = "CETUXIMAB"
        const val TREATMENT_FOLFIRI = "FOLFIRI"
        const val TREATMENT_FOLFIRINOX = "FOLFIRINOX"
        const val TREATMENT_FOLFOX = "FOLFOX"
        const val TREATMENT_LONSURF = "LONSURF"
        const val TREATMENT_PANITUMUMAB = "PANITUMUMAB"
        const val TREATMENT_PEMBROLIZUMAB = "PEMBROLIZUMAB"
        private const val SCORE_CETUXIMAB_PLUS_ENCORAFENIB = 4
        private const val SCORE_LONSURF = 2
        private const val SCORE_MONOTHERAPY = 3
        private const val SCORE_MONOTHERAPY_PLUS_TARGETED = 4
        private const val SCORE_MULTITHERAPY = 5
        private const val SCORE_PEMBROLIZUMAB = 6
        private const val SCORE_TARGETED_THERAPY = 5
        private const val CHEMO_MAX_CYCLES = "12"
        private const val RECENT_TREATMENT_THRESHOLD_WEEKS = "104"
        private val IS_COLORECTAL_CANCER: EligibilityFunction =
            eligibilityFunction(EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X, "colorectal cancer")
        private val IS_YOUNG_AND_FIT: EligibilityFunction = eligibilityFunction(
            EligibilityRule.AND,
            eligibilityFunction(EligibilityRule.NOT, eligibilityFunction(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, "75")),
            eligibilityFunction(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, "1")
        )

        private fun eligibleIfGenesAreWildType(genes: List<String>): EligibilityFunction {
            return eligibilityFunction(
                EligibilityRule.AND,
                *genes.map { eligibilityFunction(EligibilityRule.WILDTYPE_OF_GENE_X, it) }.toTypedArray()
            )
        }

        private fun eligibleIfTreatmentNotInHistory(treatmentName: String): EligibilityFunction {
            return eligibilityFunction(
                EligibilityRule.NOT,
                eligibilityFunction(
                    EligibilityRule.OR,
                    eligibilityFunction(
                        EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS,
                        treatmentName,
                        RECENT_TREATMENT_THRESHOLD_WEEKS
                    ),
                    eligibilityFunction(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT, treatmentName),
                    eligibilityFunction(
                        EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES,
                        treatmentName, CHEMO_MAX_CYCLES, CHEMO_MAX_CYCLES
                    )
                )
            )
        }

        private fun eligibleForTreatmentLines(lines: Set<Int>): EligibilityFunction {
            return eligibilityFunction(EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_LINES_X, lines.joinToString(";"))
        }

        private fun eligibilityFunction(rule: EligibilityRule, vararg parameters: Any): EligibilityFunction {
            return ImmutableEligibilityFunction.builder().rule(rule).addParameters(*parameters).build()
        }
    }
}