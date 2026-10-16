package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val HYPERSENSITIVITY_BLOCK = IcdConstants.ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_BLOCK
private const val PENICILLIN_CODE = "XM7Q57"
private const val PEANUT_CODE = "XM6Q82"

class DrugAllergyFunctionsTest {

    private val icdModel = IcdModel.create(
        listOf(
            IcdNode(IcdConstants.DRUG_ALLERGY_CODE, listOf(HYPERSENSITIVITY_BLOCK), "Drug or pharmacological agents hypersensitivity"),
            IcdNode(IcdConstants.FOOD_HYPERSENSITIVITY_CODE, listOf(HYPERSENSITIVITY_BLOCK), "Food hypersensitivity"),
            IcdNode(IcdConstants.NIVOLUMAB_CODE, listOf(IcdConstants.ANTI_NEOPLASTIC_AGENTS), "Nivolumab", isExtension = true),
            IcdNode(PENICILLIN_CODE, emptyList(), "Penicillin (any)", isExtension = true),
            IcdNode(PEANUT_CODE, emptyList(), "Peanut", isExtension = true)
        )
    )

    @Test
    fun `Should find full match for drug allergy main code with matching drug extension`() {
        listOf(HYPERSENSITIVITY_BLOCK, IcdConstants.DRUG_ALLERGY_CODE, IcdConstants.HARMFUL_EFFECTS_OF_DRUGS_CODE).forEach { mainCode ->
            val allergy = intolerance(IcdCode(mainCode, IcdConstants.NIVOLUMAB_CODE))
            assertDrugAllergies(listOf(allergy), expectedFullMatches = listOf(allergy), expectedUnknownDrugMatches = emptyList())
        }
    }

    @Test
    fun `Should find unknown drug match for drug specific main code without extension`() {
        listOf(IcdConstants.DRUG_ALLERGY_CODE, IcdConstants.HISTORY_OF_DRUG_OR_BIOLOGICAL_SUBSTANCE_ALLERGY_CODE).forEach { mainCode ->
            val allergy = intolerance(IcdCode(mainCode))
            assertDrugAllergies(listOf(allergy), expectedFullMatches = emptyList(), expectedUnknownDrugMatches = listOf(allergy))
        }
    }

    @Test
    fun `Should not match hypersensitivity main codes that do not imply a drug`() {
        val allergies = listOf(HYPERSENSITIVITY_BLOCK, IcdConstants.FOOD_HYPERSENSITIVITY_CODE).map { intolerance(IcdCode(it)) }
        assertDrugAllergies(allergies, expectedFullMatches = emptyList(), expectedUnknownDrugMatches = emptyList())
    }

    @Test
    fun `Should not match drug allergy main codes with other substance as extension code`() {
        val allergies = listOf(PENICILLIN_CODE, PEANUT_CODE).flatMap { extensionCode ->
            listOf(HYPERSENSITIVITY_BLOCK, IcdConstants.DRUG_ALLERGY_CODE).map { mainCode -> intolerance(IcdCode(mainCode, extensionCode)) }
        }
        assertDrugAllergies(allergies, expectedFullMatches = emptyList(), expectedUnknownDrugMatches = emptyList())
    }

    @Test
    fun `Should consider comorbidity with both known drug and unknown drug ICD codes as a full match only`() {
        val allergy = intolerance(IcdCode(HYPERSENSITIVITY_BLOCK, IcdConstants.NIVOLUMAB_CODE), IcdCode(IcdConstants.DRUG_ALLERGY_CODE))
        assertDrugAllergies(listOf(allergy), expectedFullMatches = listOf(allergy), expectedUnknownDrugMatches = emptyList())
    }

    private fun intolerance(vararg icdCodes: IcdCode) = Intolerance("allergy", icdCodes.toSet())

    private fun assertDrugAllergies(
        comorbidities: List<Comorbidity>,
        expectedFullMatches: List<Comorbidity>,
        expectedUnknownDrugMatches: List<Comorbidity>
    ) {
        val drugAllergies = DrugAllergyFunctions.findDrugAllergies(icdModel, comorbidities, setOf(IcdConstants.ANTI_NEOPLASTIC_AGENTS))
        assertThat(drugAllergies.fullMatches).containsExactlyElementsOf(expectedFullMatches)
        assertThat(drugAllergies.mainCodeMatchesWithUnknownExtension).containsExactlyElementsOf(expectedUnknownDrugMatches)
    }
}
