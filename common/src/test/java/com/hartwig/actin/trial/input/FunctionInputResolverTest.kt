package com.hartwig.actin.treatment.input

import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.TestFunctionInputResolveFactory
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput
import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput
import com.hartwig.actin.treatment.input.single.FunctionInput
import com.hartwig.actin.treatment.input.single.ManyGenes
import com.hartwig.actin.treatment.input.single.OneGene
import com.hartwig.actin.treatment.input.single.OneGeneManyCodons
import com.hartwig.actin.treatment.input.single.OneGeneManyProteinImpacts
import com.hartwig.actin.treatment.input.single.OneGeneOneInteger
import com.hartwig.actin.treatment.input.single.OneGeneOneIntegerOneVariantType
import com.hartwig.actin.treatment.input.single.OneGeneTwoIntegers
import com.hartwig.actin.treatment.input.single.OneHlaAllele
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings
import com.hartwig.actin.treatment.input.single.OneIntegerOneString
import com.hartwig.actin.treatment.input.single.TwoDoubles
import com.hartwig.actin.treatment.input.single.TwoIntegers
import com.hartwig.actin.treatment.input.single.TwoIntegersManyStrings
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

class FunctionInputResolverTest {
    private val resolver = TestFunctionInputResolveFactory.createTestResolver()
    
    @Test
    fun `Should determine input validity for every rule`() {
        for (rule in EligibilityRule.values()) {
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
        assertThat(resolver.createOneDoubleInput(valid)).isEqualTo(3.1, Offset.offset(EPSILON))
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
        assertThat(resolver.hasValidInputs(create(rule, listOf(TreatmentCategory.ANTIVIRAL_THERAPY.display(), "test")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a treatment category", "test")))!!).isFalse
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
        assertThat(resolver.hasValidInputs(create(rule, listOf(TreatmentCategory.ANTIVIRAL_THERAPY.display(), "test", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf(category, "1", "hello1;hello2")))!!).isFalse
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
    fun `Should resolve functions with many strings one integer input`() {
        val rule = firstOfType(FunctionInput.MANY_STRINGS_ONE_INTEGER)
        val valid = create(rule, listOf("BRAF;KRAS", "1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneIntegerManyStrings(1, listOf("BRAF", "KRAS"))
        assertThat(resolver.createManyStringsOneIntegerInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "BRAF;KRAS")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many strings two integers input`() {
        val rule = firstOfType(FunctionInput.MANY_STRINGS_TWO_INTEGERS)
        val valid = create(rule, listOf("BRAF;KRAS", "1", "2"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = TwoIntegersManyStrings(1, 2, listOf("BRAF", "KRAS"))
        assertThat(resolver.createManyStringsTwoIntegersInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1", "BRAF;KRAS")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("BRAF;KRAS", "1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("BRAF;KRAS", "1", "not an integer")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one integer one string input`() {
        val rule = firstOfType(FunctionInput.ONE_INTEGER_ONE_STRING)
        val valid = create(rule, listOf("2", "test"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue
        assertThat(resolver.createOneIntegerOneStringInput(valid)).isEqualTo(OneIntegerOneString(2, "test"))
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an integer", "not an integer")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one integer many strings input`() {
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_INTEGER_MANY_STRINGS)
        val valid: EligibilityFunction = create(rule, listOf("2", "test1;test2;test3"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneIntegerManyStrings(2, listOf("test1", "test2", "test3"))
        assertThat(resolver.createOneIntegerManyStringsInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an integer", "not an integer")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one tumor stage input`() {
        val rule = firstOfType(FunctionInput.ONE_TUMOR_STAGE)
        val valid = create(rule, listOf("IIIA"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        assertThat(resolver.createOneTumorStageInput(valid)).isEqualTo(TumorStage.IIIA)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("IIIa")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("II", "III")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one hla allele input`() {
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_HLA_ALLELE)
        val allele = "A*02:01"
        val valid: EligibilityFunction = create(rule, listOf(allele))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneHlaAllele(allele)
        assertThat(resolver.createOneHlaAlleleInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not an HLA allele")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("A*02:01", "A*02:02")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one gene input`() {
        val resolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
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
        val resolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
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
        val resolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
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
        val resolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
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
        val resolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
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
        val resolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS)
        val valid = create(rule, listOf("gene", "V600E;V601K"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = OneGeneManyProteinImpacts("gene", listOf("V600E", "V601K"))
        assertThat(resolver.createOneGeneManyProteinImpactsInput(valid)).isEqualTo(expected)
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene", "V600E")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "not a protein impact")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("V600E", "gene")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with many genes input`() {
        val resolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule = firstOfType(FunctionInput.MANY_GENES)
        val valid = create(rule, listOf("gene;gene"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        val expected = ManyGenes(listOf("gene", "gene"))
        assertThat(resolver.createManyGenesInput(valid)).isEqualTo(expected)

        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("not a gene")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("gene", "gene")))!!).isFalse
    }

    @Test
    fun `Should resolve functions with one doid term input`() {
        val resolver = TestFunctionInputResolveFactory.createResolverWithDoidAndTerm("doid 1", "term 1")
        val rule = firstOfType(FunctionInput.ONE_DOID_TERM)
        val valid = create(rule, listOf("term 1"))
        assertThat(resolver.hasValidInputs(valid)!!).isTrue

        assertThat(resolver.createOneDoidTermInput(valid)).isEqualTo("term 1")
        assertThat(resolver.hasValidInputs(create(rule, emptyList()))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("doid 1")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("term 2")))!!).isFalse
        assertThat(resolver.hasValidInputs(create(rule, listOf("term 1", "term 2")))!!).isFalse
    }

    companion object {
        private const val EPSILON = 1.0E-10

        private fun firstOfType(input: FunctionInput): EligibilityRule {
            return FunctionInputMapping.RULE_INPUT_MAP.entries.find { it.value == input }?.key
                ?: throw IllegalStateException("Could not find single rule requiring input: $input")
        }

        private fun createValidTestFunction(): EligibilityFunction {
            return create(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, listOf("18"))
        }

        private fun create(rule: EligibilityRule, parameters: List<Any>): EligibilityFunction {
            return EligibilityFunction(rule = rule, parameters = parameters)
        }
    }
}