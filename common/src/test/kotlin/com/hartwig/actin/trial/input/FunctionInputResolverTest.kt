package com.hartwig.actin.trial.input

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.Cyp
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.datamodel.clinical.TnmT
import com.hartwig.actin.datamodel.clinical.Transporter
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.FunctionInput
import com.hartwig.actin.icd.datamodel.IcdNode
import com.hartwig.actin.trial.input.TestFunctionInputResolverFactory.createTestResolver
import com.hartwig.actin.datamodel.clinical.AlbiGrade
import com.hartwig.actin.trial.input.datamodel.NyhaClass
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput
import com.hartwig.actin.trial.input.single.ManyDrugsOneInteger
import com.hartwig.actin.trial.input.single.ManyDrugsTwoIntegers
import com.hartwig.actin.trial.input.single.ManyGenes
import com.hartwig.actin.trial.input.single.ManyHlaAlleles
import com.hartwig.actin.trial.input.single.ManyIntents
import com.hartwig.actin.trial.input.single.ManyIntentsOneInteger
import com.hartwig.actin.trial.input.single.ManySpecificTreatmentsTwoIntegers
import com.hartwig.actin.trial.input.single.OneCypOneInteger
import com.hartwig.actin.trial.input.single.OneDoubleOneGender
import com.hartwig.actin.trial.input.single.OneGene
import com.hartwig.actin.trial.input.single.OneGeneManyCodons
import com.hartwig.actin.trial.input.single.OneGeneManyProteinImpacts
import com.hartwig.actin.trial.input.single.OneGeneOneInteger
import com.hartwig.actin.trial.input.single.OneGeneOneIntegerOneVariantType
import com.hartwig.actin.trial.input.single.OneGeneTwoIntegers
import com.hartwig.actin.trial.input.single.OneHaplotype
import com.hartwig.actin.trial.input.single.OneHlaGroup
import com.hartwig.actin.trial.input.single.OneIcdTitleOneInteger
import com.hartwig.actin.trial.input.single.OneIntegerManyDoidTerms
import com.hartwig.actin.trial.input.single.OneIntegerManyIcdTitles
import com.hartwig.actin.trial.input.single.OneIntegerOneBodyLocation
import com.hartwig.actin.trial.input.single.OneMedicationCategory
import com.hartwig.actin.trial.input.single.OneProteinOneString
import com.hartwig.actin.trial.input.single.OneTreatmentCategoryManyDrugs
import com.hartwig.actin.trial.input.single.OneTreatmentCategoryManyTypesManyDrugs
import com.hartwig.actin.trial.input.single.TwoDoubles
import com.hartwig.actin.trial.input.single.TwoIntegers
import com.hartwig.actin.trial.input.single.TwoStrings
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

class FunctionInputResolverTest {

    private val epsilon = 1.0E-10
    private val resolver = createTestResolver()

    @Test
    fun `Should determine input validity for every rule`() {
        for (rule in EligibilityRule.entries) {
            assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isNotNull()
        }
    }

    @Test
    fun `Should resolve composite inputs with no inputs`() {
        val inputs = emptyList<Any>()
        assertThat(resolver.hasValidInputs(create(EligibilityRule.AND, inputs))!!).isFalse
        assertThat(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs))!!).isFalse
    }

    @Test
    fun `Should resolve composite inputs with one valid input`() {
        val inputs = listOf(createValidTestFunction())
        assertThat(resolver.hasValidInputs(create(EligibilityRule.AND, inputs))!!).isFalse
        assertThat(resolver.hasValidInputs(create(EligibilityRule.OR, inputs))!!).isFalse
        val valid1: EligibilityFunction = create(EligibilityRule.NOT, inputs)
        assertThat(resolver.hasValidInputs(valid1)!!).isTrue
        assertThat(FunctionInputResolver.createOneCompositeParameter(valid1)).isNotNull
        assertThat(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs))!!).isTrue
    }

    @Test
    fun `Should resolve composite inputs with two valid inputs`() {
        val inputs = listOf(createValidTestFunction(), createValidTestFunction())
        val valid2: EligibilityFunction = create(EligibilityRule.OR, inputs)
        assertThat(resolver.hasValidInputs(valid2)!!).isTrue
        assertThat(FunctionInputResolver.createAtLeastTwoCompositeParameters(valid2)).isNotNull
        assertThat(resolver.hasValidInputs(create(EligibilityRule.NOT, inputs))!!).isFalse
    }

    @Test
    fun `Should resolve composite inputs with three valid inputs`() {
        val inputs = listOf(createValidTestFunction(), createValidTestFunction(), createValidTestFunction())
        assertThat(resolver.hasValidInputs(create(EligibilityRule.OR, inputs))!!).isTrue
        assertThat(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs))!!).isFalse
    }

    @Test
    fun `Should fail when inputs are correct in number but not in type`() {
        assertThat(resolver.hasValidInputs(create(EligibilityRule.AND, listOf("not a function", "not a function either")))!!).isFalse
    }

    @Test
    fun `Should resolve functions without inputs`() {
        val rule = firstOfType(FunctionInput.NONE)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isTrue
        assertThat(resolver.hasValidInputs(create(rule, listOf("1 is too many")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one ALBI grade input`() {
        val rule = firstOfType(FunctionInput.ONE_ALBI_GRADE)
        val valid = create(rule, listOf("1"))

        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneAlbiGradeInput(valid)).isEqualTo(AlbiGrade.GRADE_1)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "2")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an albi grade")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one integer input`() {
        val rule = firstOfType(FunctionInput.ONE_INTEGER)
        val valid = create(rule, listOf("2"))

        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneIntegerInput(valid).toLong()).isEqualTo(2)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "2")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an integer")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with two integer inputs`() {
        val rule = firstOfType(FunctionInput.TWO_INTEGERS)
        val valid = create(rule, listOf("2", "3"))

        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createTwoIntegersInput(valid)).isEqualTo(TwoIntegers(2, 3))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an integer", "also not an integer")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one double input`() {
        val rule = firstOfType(FunctionInput.ONE_DOUBLE)
        val valid = create(rule, listOf("3.1"))

        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneDoubleInput(valid)).isEqualTo(3.1, Offset.offset(epsilon))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("3.1", "3.2")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a double")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with two double inputs`() {
        val rule = firstOfType(FunctionInput.TWO_DOUBLES)
        val valid = create(rule, listOf("3.1", "3.2"))

        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createTwoDoublesInput(valid)).isEqualTo(TwoDoubles(3.1, 3.2))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("3.1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("3.1", "not a double")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one specific systemic treatment input`() {
        val rule = firstOfType(FunctionInput.ONE_SYSTEMIC_TREATMENT)
        val treatmentName = TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN
        val valid = create(rule, listOf(treatmentName))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = TestTreatmentDatabaseFactory.createProper().findTreatmentByName(treatmentName)!!
        assertThat(resolver.createOneSystemicTreatment(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(treatmentName, treatmentName)))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("ABLATION")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category or type input`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE)
        val treatment = TreatmentCategory.IMMUNOTHERAPY.display()

        val valid = create(rule, listOf(treatment))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val input = resolver.createOneTreatmentCategoryOrTypeInput(valid)
        assertThat(input.mappedCategory).isEqualTo(TreatmentCategory.IMMUNOTHERAPY)
        assertThat(input.mappedType).isNull()
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment input")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category or type one integer input`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER)
        val treatment = TreatmentCategory.IMMUNOTHERAPY.display()
        val valid = create(rule, listOf(treatment, "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createOneTreatmentCategoryOrTypeOneIntegerInput(valid)
        assertThat(inputs.treatment.mappedCategory).isEqualTo(TreatmentCategory.IMMUNOTHERAPY)
        assertThat(inputs.treatment.mappedType).isNull()
        assertThat(inputs.integer).isEqualTo(1)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment input", "test")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category many types input`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES)
        val category = TreatmentCategory.IMMUNOTHERAPY.display()
        val valid = create(rule, listOf(category, "${DrugType.ANTI_PD_L1};${DrugType.ANTI_PD_1}"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createOneTreatmentCategoryManyTypesInput(valid)
        assertThat(inputs.category).isEqualTo(TreatmentCategory.IMMUNOTHERAPY)
        assertThat(inputs.types).isEqualTo(setOf(DrugType.ANTI_PD_L1, DrugType.ANTI_PD_1))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category)))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(TreatmentCategory.TARGETED_THERAPY.display(), "test")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment category", "test")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category many types and another treatment category many types input`() {
        val rule = firstOfType(FunctionInput.TWO_TREATMENT_CATEGORIES_MANY_TYPES)
        val category1 = TreatmentCategory.IMMUNOTHERAPY.display()
        val category2 = TreatmentCategory.CHEMOTHERAPY.display()
        val valid = create(
            rule,
            listOf(
                category1,
                "${DrugType.ANTI_PD_L1};${DrugType.ANTI_PD_1}",
                category2,
                "${DrugType.PLATINUM_COMPOUND};${DrugType.ANTIMETABOLITE}"
            )
        )
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createTwoTreatmentCategoriesManyTypesInput(valid)
        assertThat(inputs.category1).isEqualTo(TreatmentCategory.IMMUNOTHERAPY)
        assertThat(inputs.types1).isEqualTo(setOf(DrugType.ANTI_PD_L1, DrugType.ANTI_PD_1))
        assertThat(inputs.category2).isEqualTo(TreatmentCategory.CHEMOTHERAPY)
        assertThat(inputs.types2).isEqualTo(setOf(DrugType.PLATINUM_COMPOUND, DrugType.ANTIMETABOLITE))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category1)))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(TreatmentCategory.TARGETED_THERAPY.display(), "test")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment category", "test")))!!).isFalse
        assertThat(
            resolver.hasValidInputs(
                create(
                    rule,
                    listOf(TreatmentCategory.CHEMOTHERAPY.display(), "${DrugType.PLATINUM_COMPOUND}")
                )
            )!!
        ).isFalse
        assertThat(
            resolver.hasValidInputs(
                create(
                    rule,
                    listOf(
                        TreatmentCategory.CHEMOTHERAPY.display(),
                        TreatmentCategory.IMMUNOTHERAPY.display(),
                        "${DrugType.PLATINUM_COMPOUND}",
                        "${DrugType.ANTIMETABOLITE}"
                    )
                )
            )!!
        ).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category many types one integer input`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER)
        val category = TreatmentCategory.IMMUNOTHERAPY.display()
        val valid = create(rule, listOf(category, "${DrugType.ANTI_PD_L1};${DrugType.ANTI_PD_1}", "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createOneTreatmentCategoryManyTypesOneIntegerInput(valid)
        assertThat(inputs.category).isEqualTo(TreatmentCategory.IMMUNOTHERAPY)
        assertThat(inputs.types).isEqualTo(setOf(DrugType.ANTI_PD_L1, DrugType.ANTI_PD_1))
        assertThat(inputs.integer).isEqualTo(1)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(TreatmentCategory.TARGETED_THERAPY.display(), "test", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category, "1", "hello1;hello2")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category and many intents`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS)
        val category = TreatmentCategory.IMMUNOTHERAPY.display()
        val valid = create(rule, listOf(category, "${Intent.ADJUVANT};${Intent.PALLIATIVE}"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createOneTreatmentCategoryManyIntentsInput(valid)
        assertThat(inputs.category).isEqualTo(TreatmentCategory.IMMUNOTHERAPY)
        assertThat(inputs.intents).isEqualTo(setOf(Intent.ADJUVANT, Intent.PALLIATIVE))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(TreatmentCategory.TARGETED_THERAPY.display(), "test")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category, "hello1;hello2")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category, many intents, and one integer input`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS_ONE_INTEGER)
        val category = TreatmentCategory.IMMUNOTHERAPY.display()
        val intents = "${Intent.ADJUVANT};${Intent.PALLIATIVE}"
        val valid = create(rule, listOf(category, intents, "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createOneTreatmentCategoryManyIntentsOneIntegerInput(valid)
        assertThat(inputs.category).isEqualTo(TreatmentCategory.IMMUNOTHERAPY)
        assertThat(inputs.intents).isEqualTo(setOf(Intent.ADJUVANT, Intent.PALLIATIVE))
        assertThat(inputs.integer).isEqualTo(1)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(TreatmentCategory.TARGETED_THERAPY.display(), "noIntent", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("no category", intents, "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category, intents, "noInteger")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment type, one integer input`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_TYPE_ONE_INTEGER)
        val treatmentType = DrugType.PLATINUM_COMPOUND.display()
        val valid = create(rule, listOf(treatmentType, "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createOneTreatmentTypeOneIntegerInput(valid)
        assertThat(inputs.type).isEqualTo(DrugType.PLATINUM_COMPOUND)
        assertThat(inputs.integer).isEqualTo(1)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment type", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(treatmentType, "not an integer")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many treatment categories`() {
        val rule = firstOfType(FunctionInput.MANY_TREATMENT_CATEGORIES)
        val valid = create(rule, listOf(TreatmentCategory.IMMUNOTHERAPY.display() + ";" + TreatmentCategory.TARGETED_THERAPY.display()))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createManyTreatmentCategories(valid)
        assertThat(inputs.treatmentCategories).isEqualTo(setOf(TreatmentCategory.IMMUNOTHERAPY, TreatmentCategory.TARGETED_THERAPY))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment category")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(TreatmentCategory.IMMUNOTHERAPY, "test")))).isFalse
    }

    @Test
    fun `Should resolve functions with one specific treatment input`() {
        val rule = firstOfType(FunctionInput.ONE_SPECIFIC_TREATMENT)
        val treatmentName = TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN
        val valid = create(rule, listOf(treatmentName))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = TestTreatmentDatabaseFactory.createProper().findTreatmentByName(treatmentName)!!
        assertThat(resolver.createOneSpecificTreatmentInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(treatmentName, treatmentName)))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many specific treatments input`() {
        val rule = firstOfType(FunctionInput.MANY_SPECIFIC_TREATMENTS)
        val treatmentNames = listOf(TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN, TestTreatmentDatabaseFactory.RADIOTHERAPY)
        val valid = create(rule, listOf(treatmentNames.joinToString(";")))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        val expected = treatmentNames.map { treatmentDatabase.findTreatmentByName(it)!! }
        assertThat(resolver.createManySpecificTreatmentsInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(treatmentNames, "second parameter")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many specific treatment two integers input`() {
        val rule = firstOfType(FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS)
        val treatmentNames = listOf(TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN, TestTreatmentDatabaseFactory.RADIOTHERAPY)
        val valid = create(rule, listOf(treatmentNames.joinToString(";"), "1", "2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        val expected = ManySpecificTreatmentsTwoIntegers(
            treatments = treatmentNames.map { treatmentDatabase.findTreatmentByName(it)!! }, integer1 = 1, integer2 = 2
        )
        assertThat(resolver.createManySpecificTreatmentsTwoIntegerInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(treatmentNames, treatmentNames)))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category many drugs input`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS)
        val category = TreatmentCategory.CHEMOTHERAPY
        val drugNames = listOf("CAPECITABINE", "OXALIPLATIN")
        val valid = create(rule, listOf(category.display(), drugNames.joinToString(";")))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        val expected = OneTreatmentCategoryManyDrugs(
            category = category,
            drugs = drugNames.map { treatmentDatabase.findDrugByName(it)!! }.toSet()
        )
        assertThat(resolver.createOneTreatmentCategoryManyDrugsInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment category", "CAPECITABINE;OXALIPLATIN")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category, "CAPECITABINE;OXALIPLATIN", "1")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one treatment category many types many drugs input`() {
        val rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS)
        val category = TreatmentCategory.CHEMOTHERAPY
        val drugNames = listOf("CAPECITABINE", "OXALIPLATIN")
        val types = "${DrugType.ALKYLATING_AGENT};${DrugType.ANTIMETABOLITE}"
        val valid = create(rule, listOf(category.display(), types, drugNames.joinToString(";")))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        val expected = OneTreatmentCategoryManyTypesManyDrugs(
            category = category,
            types = setOf(DrugType.ALKYLATING_AGENT, DrugType.ANTIMETABOLITE),
            drugs = drugNames.map { treatmentDatabase.findDrugByName(it)!! }.toSet()
        )
        assertThat(resolver.createOneTreatmentCategoryManyTypesManyDrugsInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment category", types, drugNames.joinToString(";"))))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category, types, drugNames.joinToString(";"), "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category, drugNames.joinToString(";"))))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many drugs input`() {
        val rule = firstOfType(FunctionInput.MANY_DRUGS)
        val drugNames = listOf("CAPECITABINE", "OXALIPLATIN")
        val valid = create(rule, listOf(drugNames.joinToString(";")))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        val expected = drugNames.map { treatmentDatabase.findDrugByName(it)!! }.toSet()
        assertThat(resolver.createManyDrugsInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CAPECITABINE;notADrug")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CAPECITABINE;OXALIPLATIN", "1")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many drugs one integer input`() {
        val rule = firstOfType(FunctionInput.MANY_DRUGS_ONE_INTEGER)
        val drugNames = listOf("CAPECITABINE", "OXALIPLATIN")
        val valid = create(rule, listOf(drugNames.joinToString(";"), "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        val expected = ManyDrugsOneInteger(
            drugs = drugNames.map { treatmentDatabase.findDrugByName(it)!! }.toSet(), integer = 1
        )
        assertThat(resolver.createManyDrugsOneIntegerInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CAPECITABINE;notADrug", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CAPECITABINE;OXALIPLATIN")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many drugs two integers input`() {
        val rule = firstOfType(FunctionInput.MANY_DRUGS_TWO_INTEGERS)
        val drugNames = listOf("CAPECITABINE", "OXALIPLATIN")
        val valid = create(rule, listOf(drugNames.joinToString(";"), "1", "2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        val expected = ManyDrugsTwoIntegers(drugNames.map { treatmentDatabase.findDrugByName(it)!! }.toSet(), 1, 2)
        assertThat(resolver.createManyDrugsTwoIntegersInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "CAPECITABINE;OXALIPLATIN")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CAPECITABINE;notADrug", "1", "2")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CAPECITABINE;OXALIPLATIN", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CAPECITABINE;OXALIPLATIN", "1", "not an integer")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one ICD title input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithIcdNodes(
            listOf(IcdNode("code 1", listOf("parent 1"), "title 1"), IcdNode("code 2", listOf("parent 2"), "title 2"))
        )
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_ICD_TITLE)
        val valid: EligibilityFunction = create(rule, listOf("title 1&title 2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = "title 1&title 2"
        assertThat(resolver.createOneIcdTitleInput(valid)).isEqualTo(expected)
        assertThat(resolver.createOneIcdTitleInput(create(rule, listOf("code 1&code 2")))).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, listOf("code 1")))!!).isTrue
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("title 1", "title 2")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many ICD titles input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithIcdNodes(
            listOf(
                IcdNode("code 1", listOf("parent 1"), "title 1"),
                IcdNode("code 2", listOf("parent 2"), "title 2"),
                IcdNode("extension code", emptyList(), "extension title")
            )
        )
        val rule: EligibilityRule = firstOfType(FunctionInput.MANY_ICD_TITLES)
        val valid: EligibilityFunction = create(rule, listOf("title 1&extension title;title 2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = listOf("title 1&extension title", "title 2")
        assertThat(resolver.createManyIcdTitlesInput(valid)).isEqualTo(expected)
        assertThat(resolver.createManyIcdTitlesInput(create(rule, listOf("code 1&extension code;code 2")))).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, listOf("code 1&extension code;title 2")))!!).isTrue
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("title 1", "title 2")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("title 1&extension title;invalid title")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one ICD title one integer input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithIcdNodes(
            listOf(IcdNode("code 1", listOf("parent 1"), "title 1"), IcdNode("code 2", listOf("parent 2"), "title 2"))
        )
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_ICD_TITLE_ONE_INTEGER)
        val valid: EligibilityFunction = create(rule, listOf("title 1", "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneIcdTitleOneInteger("title 1", 1)
        assertThat(resolver.createOneIcdTitleOneIntegerInput(valid)).isEqualTo(expected)
        assertThat(resolver.createOneIcdTitleOneIntegerInput(create(rule, listOf("code 1", "1")))).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, listOf("code 2", "1")))!!).isTrue
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("title 1", "title 2")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("title 1", "1", "2")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one NYHA class type input`() {
        val rule = firstOfType(FunctionInput.ONE_NYHA_CLASS)
        val valid = create(rule, listOf("IV"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneNyhaClassInput(valid)).isEqualTo(NyhaClass.IV)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("I", "II")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a NYHA class")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one tumor type input`() {
        val rule = firstOfType(FunctionInput.ONE_TUMOR_TYPE)
        val category = TumorTypeInput.CARCINOMA.display()
        val valid = create(rule, listOf(category))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneTumorTypeInput(valid)).isEqualTo(TumorTypeInput.CARCINOMA)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a tumor type")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one string input`() {
        val rule = firstOfType(FunctionInput.ONE_STRING)
        val valid = create(rule, listOf("0045"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneStringInput(valid)).isEqualTo("0045")
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("012", "234")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with two string inputs`() {
        val rule = firstOfType(FunctionInput.TWO_STRINGS)
        val valid = create(rule, listOf("string1", "string2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createTwoStringsInput(valid)).isEqualTo(TwoStrings("string1", "string2"))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(1, 2)))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one string one integer input`() {
        val rule = firstOfType(FunctionInput.ONE_STRING_ONE_INTEGER)
        val valid = create(rule, listOf("string", "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val inputs = resolver.createOneStringOneIntegerInput(valid)
        assertThat(inputs.string).isEqualTo("string")
        assertThat(inputs.integer).isEqualTo(1)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "string")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many strings`() {
        val rule = firstOfType(FunctionInput.MANY_STRINGS)
        val valid = create(rule, listOf("BRAF;KRAS"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = listOf("BRAF", "KRAS")
        assertThat(resolver.createManyStringsInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many body location input`() {
        val rule = firstOfType(FunctionInput.MANY_BODY_LOCATIONS)
        val valid = create(rule, listOf("liver;lung"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        val expected = setOf(BodyLocationCategory.LIVER, BodyLocationCategory.LUNG)
        assertThat(resolver.createManyBodyLocationsInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("liver")))!!).isTrue
        assertThat(resolver.hasValidInputs(create(rule, listOf("liver;not a body location")))!!).isFalse()
    }

    @Test
    fun `Should resolve functions with one integer one body location input`() {
        val rule = firstOfType(FunctionInput.ONE_INTEGER_ONE_BODY_LOCATION)
        val valid = create(rule, listOf("2", "liver"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneIntegerOneBodyLocationInput(valid)).isEqualTo(OneIntegerOneBodyLocation(2, BodyLocationCategory.LIVER))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an integer", BodyLocationCategory.LIVER)))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one integer many doid terms input`() {
        val resolver =
            TestFunctionInputResolverFactory.createResolverWithTwoDoidsAndTerms(listOf("doid 1", "doid 2"), listOf("term 1", "term 2"))
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_INTEGER_MANY_DOID_TERMS)
        val valid: EligibilityFunction = create(rule, listOf("2", "term 1;term 2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneIntegerManyDoidTerms(2, listOf("term 1", "term 2"))
        assertThat(resolver.createOneIntegerManyDoidTermsInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an integer", "not an integer")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "doid term", "other string")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one integer many ICD titles input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithIcdNodes(
            listOf(IcdNode("code 1", listOf("parent 1"), "title 1"), IcdNode("code 2", listOf("parent 2"), "title 2"))
        )
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_INTEGER_MANY_ICD_TITLES)
        val valid: EligibilityFunction = create(rule, listOf("2", "title 1;title 2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneIntegerManyIcdTitles(2, listOf("title 1", "title 2"))
        assertThat(resolver.createOneIntegerManyIcdTitlesInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an integer", "not an integer")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "icd title", "other string")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many tumor stage input`() {
        val rule = firstOfType(FunctionInput.MANY_TUMOR_STAGES)
        val valid = create(rule, listOf("I;IV"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue()

        assertThat(resolver.createManyTumorStagesInput(valid)).isEqualTo(setOf(TumorStage.I, TumorStage.IV))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("IIIA")))!!).isTrue
        assertThat(resolver.hasValidInputs(create(rule, listOf("II;III")))!!).isTrue
    }

    @Test
    fun `Should resolve functions with many hla alleles input`() {
        val rule: EligibilityRule = firstOfType(FunctionInput.MANY_HLA_ALLELES)
        val valid: EligibilityFunction = create(rule, listOf("A*02:01;A*02:02"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = ManyHlaAlleles(setOf("A*02:01", "A*02:02"))
        assertThat(resolver.createManyHlaAllelesInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an HLA allele")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("A*02:01", "A*02:02")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one hla group input`() {
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_HLA_GROUP)
        val allele = "A*02"
        val valid: EligibilityFunction = create(rule, listOf(allele))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneHlaGroup(allele)
        assertThat(resolver.createOneHlaGroupInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an HLA allele group")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("A*02:01")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("A*02", "A*01")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one haplotype input`() {
        val resolver = createTestResolver()
        val rule = firstOfType(FunctionInput.ONE_HAPLOTYPE)
        val haplotype = "*1_HOM"
        val valid = create(rule, listOf(haplotype))
        assertThat(resolver.hasValidInputs(valid)).isTrue

        val expected = OneHaplotype(haplotype)
        assertThat(resolver.createOneHaplotypeInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an haplotype")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("*1_HOM", "*1_HOM")))).isFalse
    }

    @Test
    fun `Should resolve functions with one gene input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.ONE_GENE)
        val valid = create(rule, listOf("gene"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneGene("gene")
        assertThat(resolver.createOneGeneInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "gene")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one gene one integer input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.ONE_GENE_ONE_INTEGER)
        val valid = create(rule, listOf("gene", "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneGeneOneInteger("gene", 1)
        assertThat(resolver.createOneGeneOneIntegerInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "gene")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "gene")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one gene one integer one variant type input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE)
        val valid = create(rule, listOf("gene", "1", "SNV"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneGeneOneIntegerOneVariantType("gene", 1, VariantTypeInput.SNV)
        assertThat(resolver.createOneGeneOneIntegerOneVariantTypeInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene", "1", "SNV")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "1", "not a type")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "gene", "SNV")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "gene")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one gene two integers input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.ONE_GENE_TWO_INTEGERS)
        val valid = create(rule, listOf("gene", "1", "2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneGeneTwoIntegers("gene", 1, 2)
        assertThat(resolver.createOneGeneTwoIntegersInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene", "1", "2")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "1", "not a number")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "gene", "2")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one gene many codons input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.ONE_GENE_MANY_CODONS)
        val valid = create(rule, listOf("gene", "V600;V601"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneGeneManyCodons("gene", listOf("V600", "V601"))
        assertThat(resolver.createOneGeneManyCodonsInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene", "V600")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "not a codon")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("V600", "gene")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one gene many protein impacts input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS)
        val valid = create(rule, listOf("gene", "V600E;V601K"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneGeneManyProteinImpacts("gene", setOf("V600E", "V601K"))
        assertThat(resolver.createOneGeneManyProteinImpactsInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene", "V600E")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "not a protein impact")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("V600E", "gene")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many genes input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.MANY_GENES)
        val valid = create(rule, listOf("gene;gene"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = ManyGenes(setOf("gene", "gene"))
        assertThat(resolver.createManyGenesInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "gene")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one doid term input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithDoidAndTerm("doid 1", "term 1")
        val rule = firstOfType(FunctionInput.ONE_DOID_TERM)
        val valid = create(rule, listOf("term 1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        assertThat(resolver.createOneDoidTermInput(valid)).isEqualTo("term 1")
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("doid 1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("term 2")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("term 1", "term 2")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many doid terms input`() {
        val resolver =
            TestFunctionInputResolverFactory.createResolverWithTwoDoidsAndTerms(listOf("doid 1", "doid 2"), listOf("term 1", "term 2"))
        val rule = firstOfType(FunctionInput.MANY_DOID_TERMS)
        val valid = create(rule, listOf("term 1;term 2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        assertThat(resolver.createManyDoidTermsInput(valid)).isEqualTo(listOf("term 1", "term 2"))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("doid 1", "doid 2")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one receptor type input`() {
        val rule = firstOfType(FunctionInput.ONE_RECEPTOR_TYPE)
        val valid = create(rule, listOf("ER"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneReceptorTypeInput(valid)).isEqualTo(ReceptorType.ER)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("ER", "something else")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a receptor")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many intents input`() {
        val resolver = createTestResolver()

        val rule = firstOfType(FunctionInput.MANY_INTENTS)

        val valid = create(rule, listOf(Intent.ADJUVANT.display() + ";" + Intent.NEOADJUVANT.display()))
        assertThat(resolver.hasValidInputs(valid)).isTrue

        val inputs: ManyIntents = resolver.createManyIntentsInput(valid)
        assertThat(inputs).isEqualTo(ManyIntents(setOf(Intent.ADJUVANT, Intent.NEOADJUVANT)))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an intent")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(Intent.ADJUVANT, "test")))).isFalse
    }

    @Test
    fun `Should resolve functions with many intents one integer input`() {
        val resolver = createTestResolver()

        val rule = firstOfType(FunctionInput.MANY_INTENTS_ONE_INTEGER)

        val valid = create(rule, listOf(Intent.ADJUVANT.display() + ";" + Intent.NEOADJUVANT.display(), "1"))
        assertThat(resolver.hasValidInputs(valid)).isTrue

        val inputs = resolver.createManyIntentsOneIntegerInput(valid)
        assertThat(inputs).isEqualTo(ManyIntentsOneInteger(intents = setOf(Intent.ADJUVANT, Intent.NEOADJUVANT), integer = 1))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(Intent.ADJUVANT.display(), "test", "1")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", Intent.ADJUVANT.display())))).isFalse
    }

    @Test
    fun `Should resolve functions with one medication category input`() {
        val resolver = createTestResolver()

        val rule = firstOfType(FunctionInput.ONE_MEDICATION_CATEGORY)

        val valid = create(rule, listOf(ATC_CODE_1))
        assertThat(resolver.hasValidInputs(valid)).isTrue

        assertThat(resolver.createOneMedicationCategoryInput(valid))
            .isEqualTo(OneMedicationCategory(ATC_CODE_1, setOf(AtcLevel(name = CATEGORY_1, code = ATC_CODE_1))))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(ATC_CODE_1, "1")))).isFalse
    }

    @Test
    fun `Should resolve functions with one medication category one integer input`() {
        val resolver = createTestResolver()

        val rule = firstOfType(FunctionInput.ONE_MEDICATION_CATEGORY_ONE_INTEGER)

        val valid = create(rule, listOf(ATC_CODE_1, "1"))
        assertThat(resolver.hasValidInputs(valid)).isTrue

        assertThat(resolver.createOneMedicationCategoryOneIntegerInput(valid))
            .isEqualTo(Pair(OneMedicationCategory(ATC_CODE_1, setOf(AtcLevel(name = CATEGORY_1, code = ATC_CODE_1))), 1))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(ATC_CODE_1)))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(ATC_CODE_1, "1", "2")))).isFalse
    }

    @Test
    fun `Should resolve functions with many medication categories one integer input`() {
        val resolver = createTestResolver()

        val rule = firstOfType(FunctionInput.MANY_MEDICATION_CATEGORIES_ONE_INTEGER)

        val manyCategoriesString = "$ATC_CODE_1;$ATC_CODE_2"
        val valid = create(rule, listOf(manyCategoriesString, "1"))
        assertThat(resolver.hasValidInputs(valid)).isTrue
        assertThat(resolver.hasValidInputs(create(rule, listOf(ATC_CODE_1, "1")))).isTrue

        val expectedCategoryMap = mapOf(
            CATEGORY_1 to setOf(AtcLevel(name = CATEGORY_1, code = ATC_CODE_1)),
            CATEGORY_2 to setOf(AtcLevel(name = CATEGORY_2, code = ATC_CODE_2))
        )
        assertThat(resolver.createManyMedicationCategoriesOneIntegerInput(valid)).isEqualTo(Pair(expectedCategoryMap, 1))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(manyCategoriesString)))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(manyCategoriesString, "1", "2")))).isFalse
    }

    @Test
    fun `Should resolve functions with many medication categories two integers input`() {
        val resolver = createTestResolver()

        val rule = firstOfType(FunctionInput.MANY_MEDICATION_CATEGORIES_TWO_INTEGERS)

        val manyCategoriesString = "$ATC_CODE_1;$ATC_CODE_2"
        val valid = create(rule, listOf(manyCategoriesString, "1", "2"))
        assertThat(resolver.hasValidInputs(valid)).isTrue
        assertThat(resolver.hasValidInputs(create(rule, listOf(ATC_CODE_1, "1", "2")))).isTrue

        val expectedCategoryMap = mapOf(
            CATEGORY_1 to setOf(AtcLevel(name = CATEGORY_1, code = ATC_CODE_1)),
            CATEGORY_2 to setOf(AtcLevel(name = CATEGORY_2, code = ATC_CODE_2))
        )
        assertThat(resolver.createManyMedicationCategoriesTwoIntegersInput(valid)).isEqualTo(Triple(expectedCategoryMap, 1, 2))

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(manyCategoriesString)))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(manyCategoriesString, "1")))).isFalse
    }

    @Test
    fun `Should resolve functions with one cyp input`() {
        val resolver = createTestResolver()
        val rule = firstOfType(FunctionInput.ONE_CYP)
        val cyp = "CYP3A4_5"
        val valid = create(rule, listOf(cyp))
        assertThat(resolver.hasValidInputs(valid)).isTrue

        val expected = Cyp.valueOf(cyp)
        assertThat(resolver.createOneCypInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("3A4_5")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CYP")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CYP3A4_5;CYP2J2")))).isFalse
    }

    @Test
    fun `Should resolve functions with one cyp and one integer input`() {
        val resolver = createTestResolver()
        val rule = firstOfType(FunctionInput.ONE_CYP_ONE_INTEGER)
        val valid = create(rule, listOf("CYP3A4_5", "1"))
        assertThat(resolver.hasValidInputs(valid)).isTrue

        assertThat(resolver.createOneCypOneIntegerInput(valid)).isEqualTo(OneCypOneInteger(Cyp.CYP3A4_5, 1))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("3A4_5")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CYP", "2")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("CYP3A4_5;CYP2J2", "1")))).isFalse
    }

    @Test
    fun `Should resolve functions with one transporter input`() {
        val resolver = createTestResolver()
        val rule = firstOfType(FunctionInput.ONE_TRANSPORTER)
        val transporter = "OATP1B1"
        val valid = create(rule, listOf(transporter))
        assertThat(resolver.hasValidInputs(valid)).isTrue

        val expected = Transporter.valueOf(transporter)
        assertThat(resolver.createOneTransporterInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("OatP1b1")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("BCRP, OATP1B1")))).isFalse
    }

    @Test
    fun `Should resolve functions with one double one gender input`() {
        val rule = firstOfType(FunctionInput.ONE_DOUBLE_ONE_GENDER)
        val valid = create(rule, listOf("1.0", "female"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneDoubleOneGender(gender = Gender.FEMALE, double = 1.0)
        assertThat(resolver.createOneDoubleOneGenderInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "not a gender")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("een", "female")))).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("female", "1.0")))).isFalse
    }

    @Test
    fun `Should resolve functions with one protein one string input`() {
        val resolver = TestFunctionInputResolverFactory.createResolverWithOneValidGene("FGFR2")
        val rule = firstOfType(FunctionInput.ONE_PROTEIN_ONE_STRING)
        val valid = create(rule, listOf("FGFR2b", "string"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneProteinOneString("FGFR2b", "string")
        assertThat(resolver.createOneProteinOneStringInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("FGFR2b")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("FGFR2b", 1)))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many TNM Ts as input`() {
        val rule = firstOfType(FunctionInput.MANY_TNM_T)
        val valid = create(rule, listOf("T2A"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createManyTnmTInput(valid)).isEqualTo(setOf(TnmT.T2A))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("T2C")))!!).isFalse
    }

    private fun firstOfType(input: FunctionInput): EligibilityRule {
        return EligibilityRule.entries.find { it.input == input }
            ?: throw IllegalStateException("Could not find single rule requiring input: $input")
    }

    private fun createValidTestFunction(): EligibilityFunction {
        return create(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, listOf("18"))
    }

    private fun create(rule: EligibilityRule, parameters: List<Any>): EligibilityFunction {
        return EligibilityFunction(rule = rule, parameters = parameters)
    }
}