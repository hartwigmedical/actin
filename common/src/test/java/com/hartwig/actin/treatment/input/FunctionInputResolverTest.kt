package com.hartwig.actin.treatment.input

import com.google.common.collect.Lists
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction
import org.junit.Assert
import org.junit.Test
import java.util.Set

class FunctionInputResolverTest {
    @Test
    fun shouldDetermineInputValidityForEveryRule() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        for (rule in EligibilityRule.values()) {
            Assert.assertNotNull(resolver.hasValidInputs(create(rule, Lists.newArrayList())))
        }
    }

    @Test
    fun shouldResolveCompositeInputs() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()

        // No inputs
        val inputs: MutableList<Any> = Lists.newArrayList()
        Assert.assertFalse(resolver.hasValidInputs(create(EligibilityRule.AND, inputs))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs))!!)

        // Add first input
        inputs.add(createValidTestFunction())
        Assert.assertFalse(resolver.hasValidInputs(create(EligibilityRule.AND, inputs))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(EligibilityRule.OR, inputs))!!)
        val valid1: EligibilityFunction = create(EligibilityRule.NOT, inputs)
        Assert.assertTrue(resolver.hasValidInputs(valid1)!!)
        Assert.assertNotNull(FunctionInputResolver.createOneCompositeParameter(valid1))
        Assert.assertTrue(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs))!!)

        // Add 2nd input
        inputs.add(createValidTestFunction())
        val valid2: EligibilityFunction = create(EligibilityRule.OR, inputs)
        Assert.assertTrue(resolver.hasValidInputs(valid2)!!)
        Assert.assertNotNull(FunctionInputResolver.createAtLeastTwoCompositeParameters(valid2))
        Assert.assertFalse(resolver.hasValidInputs(create(EligibilityRule.NOT, inputs))!!)

        // Add 3rd input
        inputs.add(createValidTestFunction())
        Assert.assertTrue(resolver.hasValidInputs(create(EligibilityRule.OR, inputs))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs))!!)

        // Make sure that the check fails when number of inputs is correct but datamodel is not.
        Assert.assertFalse(
            resolver.hasValidInputs(
                create(
                    EligibilityRule.AND,
                    Lists.newArrayList<Any>("not a function", "not a function either")
                )
            )!!
        )
    }

    @Test
    fun shouldResolveFunctionsWithoutInputs() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.NONE)
        Assert.assertTrue(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1 is too many")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneIntegerInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_INTEGER)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("2"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        Assert.assertEquals(2, resolver.createOneIntegerInput(valid).toLong())
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1", "2")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not an integer")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithTwoIntegerInputs() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.TWO_INTEGERS)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("2", "3"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        assertEquals(ImmutableTwoIntegers.builder().integer1(2).integer2(3).build(), resolver.createTwoIntegersInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not an integer", "also not an integer")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneDoubleInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_DOUBLE)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("3.1"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        Assert.assertEquals(3.1, resolver.createOneDoubleInput(valid), EPSILON)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("3.1", "3.2")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a double")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithTwoDoubleInputs() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.TWO_DOUBLES)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("3.1", "3.2"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        assertEquals(ImmutableTwoDoubles.builder().double1(3.1).double2(3.2).build(), resolver.createTwoDoublesInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("3.1")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("3.1", "not a double")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneTreatmentCategoryOrTypeInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE)
        val treatment: String = TreatmentCategory.IMMUNOTHERAPY.display()
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>(treatment))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val input: TreatmentCategoryInput = resolver.createOneTreatmentCategoryOrTypeInput(valid)
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, input.mappedCategory())
        Assert.assertNull(input.mappedType())
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a treatment input")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneTreatmentCategoryOrTypeOneIntegerInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER)
        val treatment: String = TreatmentCategory.IMMUNOTHERAPY.display()
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>(treatment, "1"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val inputs: OneTreatmentCategoryOrTypeOneInteger = resolver.createOneTreatmentCategoryOrTypeOneIntegerInput(valid)
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.treatment().mappedCategory())
        Assert.assertNull(inputs.treatment().mappedType())
        assertEquals(1, inputs.integer())
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a treatment input", "test")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneTreatmentCategoryManyTypesInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES)
        val category: String = TreatmentCategory.IMMUNOTHERAPY.display()
        val valid: EligibilityFunction =
            create(rule, Lists.newArrayList<Any>(category, DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val inputs: OneTreatmentCategoryManyTypes = resolver.createOneTreatmentCategoryManyTypesInput(valid)
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.category())
        assertEquals(Set.of(DrugType.ANTI_PD_L1, DrugType.ANTI_PD_1), inputs.types())
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>(category)))!!)
        Assert.assertFalse(
            resolver.hasValidInputs(
                create(
                    rule,
                    Lists.newArrayList<Any>(TreatmentCategory.ANTIVIRAL_THERAPY.display(), "test")
                )
            )!!
        )
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a treatment category", "test")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneTreatmentCategoryManyTypesOneIntegerInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER)
        val category: String = TreatmentCategory.IMMUNOTHERAPY.display()
        val valid: EligibilityFunction =
            create(rule, Lists.newArrayList<Any>(category, DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1, "1"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val inputs: OneTreatmentCategoryManyTypesOneInteger = resolver.createOneTreatmentCategoryManyTypesOneIntegerInput(valid)
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.category())
        assertEquals(Set.of(DrugType.ANTI_PD_L1, DrugType.ANTI_PD_1), inputs.types())
        assertEquals(1, inputs.integer())
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(
            resolver.hasValidInputs(
                create(
                    rule,
                    Lists.newArrayList<Any>(TreatmentCategory.ANTIVIRAL_THERAPY.display(), "test", "1")
                )
            )!!
        )
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>(category, "1", "hello1;hello2")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneTumorTypeInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_TUMOR_TYPE)
        val category: String = TumorTypeInput.CARCINOMA.display()
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>(category))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        Assert.assertEquals(TumorTypeInput.CARCINOMA, resolver.createOneTumorTypeInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a tumor type")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneStringInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_STRING)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("0045"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        Assert.assertEquals("0045", resolver.createOneStringInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("012", "234")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneStringOneIntegerInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_STRING_ONE_INTEGER)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("string", "1"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val inputs: OneIntegerOneString = resolver.createOneStringOneIntegerInput(valid)
        assertEquals("string", inputs.string())
        assertEquals(1, inputs.integer())
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1", "string")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithManyStringsOneIntegerInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.MANY_STRINGS_ONE_INTEGER)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("BRAF;KRAS", "1"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneIntegerManyStrings =
            ImmutableOneIntegerManyStrings.builder().integer(1).strings(Lists.newArrayList("BRAF", "KRAS")).build()
        Assert.assertEquals(expected, resolver.createManyStringsOneIntegerInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1", "BRAF;KRAS")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithManyStringsTwoIntegersInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.MANY_STRINGS_TWO_INTEGERS)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("BRAF;KRAS", "1", "2"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: TwoIntegersManyStrings =
            ImmutableTwoIntegersManyStrings.builder().integer1(1).integer2(2).strings(Lists.newArrayList("BRAF", "KRAS")).build()
        Assert.assertEquals(expected, resolver.createManyStringsTwoIntegersInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1", "BRAF;KRAS")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("BRAF;KRAS", "1")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("BRAF;KRAS", "1", "not an integer")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneIntegerOneStringInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_INTEGER_ONE_STRING)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("2", "test"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        assertEquals(
            ImmutableOneIntegerOneString.builder().integer(2).string("test").build(),
            resolver.createOneIntegerOneStringInput(valid)
        )
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not an integer", "not an integer")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneIntegerManyStringsInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_INTEGER_MANY_STRINGS)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("2", "test1;test2;test3"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneIntegerManyStrings =
            ImmutableOneIntegerManyStrings.builder().integer(2).strings(Lists.newArrayList("test1", "test2", "test3")).build()
        Assert.assertEquals(expected, resolver.createOneIntegerManyStringsInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not an integer", "not an integer")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneTumorStageInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_TUMOR_STAGE)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("IIIA"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        Assert.assertEquals(TumorStage.IIIA, resolver.createOneTumorStageInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("IIIa")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("II", "III")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneHlaAlleleInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createTestResolver()
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_HLA_ALLELE)
        val allele = "A*02:01"
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>(allele))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneHlaAllele = ImmutableOneHlaAllele.builder().allele(allele).build()
        Assert.assertEquals(expected, resolver.createOneHlaAlleleInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not an HLA allele")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("A*02:01", "A*02:02")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneGeneInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_GENE)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("gene"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneGene = ImmutableOneGene.builder().geneName("gene").build()
        Assert.assertEquals(expected, resolver.createOneGeneInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a gene")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene", "gene")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneGeneOneIntegerInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_GENE_ONE_INTEGER)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("gene", "1"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneGeneOneInteger = ImmutableOneGeneOneInteger.builder().geneName("gene").integer(1).build()
        Assert.assertEquals(expected, resolver.createOneGeneOneIntegerInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a gene", "1")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1", "gene")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene", "gene")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneGeneOneIntegerOneVariantTypeInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("gene", "1", "SNV"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneGeneOneIntegerOneVariantType =
            ImmutableOneGeneOneIntegerOneVariantType.builder().geneName("gene").integer(1).variantType(VariantTypeInput.SNV).build()
        Assert.assertEquals(expected, resolver.createOneGeneOneIntegerOneVariantTypeInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a gene", "1", "SNV")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene", "1", "not a type")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1", "gene", "SNV")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene", "gene")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneGeneTwoIntegersInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_GENE_TWO_INTEGERS)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("gene", "1", "2"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneGeneTwoIntegers = ImmutableOneGeneTwoIntegers.builder().geneName("gene").integer1(1).integer2(2).build()
        Assert.assertEquals(expected, resolver.createOneGeneTwoIntegersInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a gene", "1", "2")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene", "1", "not a number")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("1", "gene", "2")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneGeneManyCodonsInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_GENE_MANY_CODONS)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("gene", "V600;V601"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneGeneManyCodons =
            ImmutableOneGeneManyCodons.builder().geneName("gene").codons(Lists.newArrayList("V600", "V601")).build()
        Assert.assertEquals(expected, resolver.createOneGeneManyCodonsInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a gene", "V600")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene", "not a codon")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("V600", "gene")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneGeneManyProteinImpactsInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("gene", "V600E;V601K"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: OneGeneManyProteinImpacts =
            ImmutableOneGeneManyProteinImpacts.builder().geneName("gene").proteinImpacts(Lists.newArrayList("V600E", "V601K")).build()
        Assert.assertEquals(expected, resolver.createOneGeneManyProteinImpactsInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a gene", "V600E")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene", "not a protein impact")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("V600E", "gene")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithManyGenesInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createResolverWithOneValidGene("gene")
        val rule: EligibilityRule = firstOfType(FunctionInput.MANY_GENES)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("gene;gene"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        val expected: ManyGenes = ImmutableManyGenes.builder().geneNames(Lists.newArrayList("gene", "gene")).build()
        Assert.assertEquals(expected, resolver.createManyGenesInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("not a gene")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("gene", "gene")))!!)
    }

    @Test
    fun shouldResolveFunctionsWithOneDoidTermInput() {
        val resolver: FunctionInputResolver = TestFunctionInputResolveFactory.createResolverWithDoidAndTerm("doid 1", "term 1")
        val rule: EligibilityRule = firstOfType(FunctionInput.ONE_DOID_TERM)
        val valid: EligibilityFunction = create(rule, Lists.newArrayList<Any>("term 1"))
        Assert.assertTrue(resolver.hasValidInputs(valid)!!)
        Assert.assertEquals("term 1", resolver.createOneDoidTermInput(valid))
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList()))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("doid 1")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("term 2")))!!)
        Assert.assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList<Any>("term 1", "term 2")))!!)
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private fun firstOfType(input: FunctionInput): EligibilityRule {
            for ((key, value) in FunctionInputMapping.RULE_INPUT_MAP) {
                if (value == input) {
                    return key
                }
            }
            throw IllegalStateException("Could not find single rule requiring input: $input")
        }

        private fun createValidTestFunction(): EligibilityFunction {
            return create(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, Lists.newArrayList<Any>("18"))
        }

        private fun create(rule: EligibilityRule, parameters: List<Any>): EligibilityFunction {
            return ImmutableEligibilityFunction.builder().rule(rule).parameters(parameters).build()
        }
    }
}