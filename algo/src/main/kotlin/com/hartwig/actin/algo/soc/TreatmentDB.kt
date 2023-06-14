package com.hartwig.actin.algo.soc

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.algo.soc.datamodel.Treatment
import com.hartwig.actin.algo.soc.datamodel.TreatmentComponent
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction

internal object TreatmentDB {
    const val TREATMENT_CAPOX = "CAPOX"
    const val TREATMENT_CETUXIMAB = "Cetuximab"
    const val TREATMENT_FOLFIRI = "FOLFIRI"
    const val TREATMENT_FOLFIRINOX = "FOLFIRINOX"
    const val TREATMENT_FOLFOX = "FOLFOX"
    const val TREATMENT_LONSURF = "Lonsurf"
    const val TREATMENT_PANITUMUMAB = "Panitumumab"
    const val TREATMENT_PEMBROLIZUMAB = "Pembrolizumab"
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

    fun loadTreatments(): List<Treatment> {
        return listOf(
            combinableChemotherapies(),
            combinableChemotherapies().map(TreatmentDB::addBevacizumabToTreatment),
            antiEGFRTherapies(),
            antiEGFRTherapies().flatMap { therapy: Treatment ->
                combinableChemotherapies().map { chemo: Treatment ->
                    addComponentsToTreatment(
                        therapy,
                        chemo.name,
                        chemo.components,
                        TreatmentCategory.CHEMOTHERAPY,
                        SCORE_MONOTHERAPY_PLUS_TARGETED, setOf(2)
                    )
                }
            },
            otherTreatments()
        ).flatten()
    }

    private fun combinableChemotherapies(): List<Treatment> {
        return listOf(
            createChemotherapy("5-FU", setOf(TreatmentComponent.FLUOROURACIL), SCORE_MONOTHERAPY),
            createChemotherapy("Capecitabine", setOf(TreatmentComponent.CAPECITABINE), SCORE_MONOTHERAPY),
            createChemotherapy("Irinotecan", setOf(TreatmentComponent.IRINOTECAN), SCORE_MONOTHERAPY),
            createChemotherapy("Oxaliplatin", setOf(TreatmentComponent.OXALIPLATIN), -1),
            createMultiChemotherapy(TREATMENT_CAPOX, setOf(TreatmentComponent.CAPECITABINE, TreatmentComponent.OXALIPLATIN)),
            createChemotherapy(
                TREATMENT_FOLFIRI,
                setOf(TreatmentComponent.FLUOROURACIL, TreatmentComponent.IRINOTECAN),
                SCORE_MULTITHERAPY,
                false, setOf(1, 2),
                setOf(
                    eligibleIfTreatmentNotInHistory(TREATMENT_CAPOX), eligibleIfTreatmentNotInHistory(TREATMENT_FOLFOX),
                    IS_YOUNG_AND_FIT
                )
            ),
            createMultiChemotherapy(TREATMENT_FOLFOX, setOf(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN)),
            createMultiChemotherapy(
                TREATMENT_FOLFIRINOX,
                setOf(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN, TreatmentComponent.IRINOTECAN)
            )
        )
    }

    private fun createMultiChemotherapy(name: String, components: Set<TreatmentComponent>): Treatment {
        return createChemotherapy(name, components, SCORE_MULTITHERAPY, false, setOf(1, 2), setOf(IS_YOUNG_AND_FIT))
    }

    private fun createChemotherapy(
        name: String, components: Set<TreatmentComponent>, score: Int, isOptional: Boolean = false,
        lines: Set<Int> = setOf(1, 2), extraFunctions: Set<EligibilityFunction> = emptySet()
    ): Treatment {
        return Treatment(
            name = name,
            categories = setOf(TreatmentCategory.CHEMOTHERAPY),
            components = components,
            score = score,
            isOptional = isOptional,
            lines = lines,
            eligibilityFunctions = setOf(IS_COLORECTAL_CANCER, eligibleIfTreatmentNotInHistory(name)) union extraFunctions,
        )
    }

    private fun antiEGFRTherapies(): List<Treatment> {
        return listOf(
            createAntiEGFRTherapy(TREATMENT_CETUXIMAB, TreatmentComponent.CETUXIMAB),
            createAntiEGFRTherapy(TREATMENT_PANITUMUMAB, TreatmentComponent.PANITUMUMAB)
        )
    }

    private fun createAntiEGFRTherapy(name: String, component: TreatmentComponent): Treatment {
        return Treatment(
            name = name,
            categories = setOf(TreatmentCategory.TARGETED_THERAPY),
            components = setOf(component),
            isOptional = false,
            score = SCORE_TARGETED_THERAPY,
            lines = setOf(2, 3),
            eligibilityFunctions = setOf(
                IS_COLORECTAL_CANCER,
                eligibleIfGenesAreWildType(listOf("KRAS", "NRAS", "BRAF")),
                eligibilityFunction(EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR)
            )
        )
    }

    private fun otherTreatments(): List<Treatment> {
        return listOf(
            createChemotherapy(
                TREATMENT_LONSURF,
                setOf(TreatmentComponent.TRIFLURIDINE, TreatmentComponent.TIPIRACIL),
                SCORE_LONSURF,
                true, setOf(3),
                setOf(eligibleIfTreatmentNotInHistory("trifluridine"))
            ),
            Treatment(
                name = TREATMENT_PEMBROLIZUMAB,
                components = setOf(TreatmentComponent.PEMBROLIZUMAB),
                categories = setOf(TreatmentCategory.IMMUNOTHERAPY),
                isOptional = false,
                score = SCORE_PEMBROLIZUMAB,
                lines = setOf(1, 2),
                eligibilityFunctions = setOf(IS_COLORECTAL_CANCER, eligibilityFunction(EligibilityRule.MSI_SIGNATURE))
            ),
            Treatment(
                name = "Cetuximab + Encorafenib",
                components = setOf(TreatmentComponent.CETUXIMAB, TreatmentComponent.ENCORAFENIB),
                categories = setOf(TreatmentCategory.TARGETED_THERAPY),
                isOptional = false,
                score = SCORE_CETUXIMAB_PLUS_ENCORAFENIB,
                lines = setOf(2, 3),
                eligibilityFunctions = setOf(
                    IS_COLORECTAL_CANCER,
                    eligibilityFunction(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, "BRAF")
                )
            )
        )
    }

    private fun addBevacizumabToTreatment(treatment: Treatment): Treatment {
        return addComponentsToTreatment(
            treatment,
            "Bevacizumab",
            setOf(TreatmentComponent.BEVACIZUMAB),
            TreatmentCategory.IMMUNOTHERAPY,
            treatment.score.coerceAtLeast(SCORE_MONOTHERAPY_PLUS_TARGETED),
            treatment.lines
        )
    }

    private fun addComponentsToTreatment(
        treatment: Treatment, name: String, components: Set<TreatmentComponent>,
        category: TreatmentCategory, score: Int, lines: Set<Int>
    ): Treatment {
        return treatment.copy(
            name = treatment.name + " + " + name,
            components = treatment.components + components,
            categories = treatment.categories + category,
            score = score,
            lines = lines
        )
    }

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

    private fun eligibilityFunction(rule: EligibilityRule, vararg parameters: Any): EligibilityFunction {
        return ImmutableEligibilityFunction.builder().rule(rule).addParameters(*parameters).build()
    }
}