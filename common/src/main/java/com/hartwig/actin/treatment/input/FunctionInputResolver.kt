package com.hartwig.actin.treatment.input

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput
import com.hartwig.actin.treatment.input.single.ImmutableManyGenes
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

class FunctionInputResolver(
    doidModel: DoidModel, molecularInputChecker: MolecularInputChecker,
    treatmentDatabase: TreatmentDatabase
) {
    private val doidModel: DoidModel
    private val molecularInputChecker: MolecularInputChecker
    private val treatmentDatabase: TreatmentDatabase

    init {
        this.doidModel = doidModel
        this.molecularInputChecker = molecularInputChecker
        this.treatmentDatabase = treatmentDatabase
    }

    fun hasValidInputs(function: EligibilityFunction): Boolean? {
        if (CompositeRules.isComposite(function.rule())) {
            return hasValidCompositeInputs(function)
        } else {
            return hasValidSingleInputs(function)
        }
    }

    private fun hasValidSingleInputs(function: EligibilityFunction): Boolean? {
        try {
            when (FunctionInputMapping.RULE_INPUT_MAP.get(function.rule())) {
                FunctionInput.NONE -> {
                    return function.parameters().isEmpty()
                }

                FunctionInput.ONE_INTEGER -> {
                    createOneIntegerInput(function)
                    return true
                }

                FunctionInput.TWO_INTEGERS -> {
                    createTwoIntegersInput(function)
                    return true
                }

                FunctionInput.MANY_INTEGERS -> {
                    createManyIntegersInput(function)
                    return true
                }

                FunctionInput.ONE_DOUBLE -> {
                    createOneDoubleInput(function)
                    return true
                }

                FunctionInput.TWO_DOUBLES -> {
                    createTwoDoublesInput(function)
                    return true
                }

                FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE -> {
                    createOneTreatmentCategoryOrTypeInput(function)
                    return true
                }

                FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER -> {
                    createOneTreatmentCategoryOrTypeOneIntegerInput(function)
                    return true
                }

                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES -> {
                    createOneTreatmentCategoryManyTypesInput(function)
                    return true
                }

                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER -> {
                    createOneTreatmentCategoryManyTypesOneIntegerInput(function)
                    return true
                }

                FunctionInput.ONE_SPECIFIC_TREATMENT -> {
                    createOneSpecificTreatmentInput(function)
                    return true
                }

                FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER -> {
                    createOneSpecificTreatmentOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS -> {
                    createManySpecificTreatmentsTwoIntegerInput(function)
                    return true
                }

                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS -> {
                    createOneTreatmentCategoryManyDrugsInput(function)
                    return true
                }

                FunctionInput.MANY_DRUGS -> {
                    createManyDrugsInput(function)
                    return true
                }

                FunctionInput.ONE_TUMOR_TYPE -> {
                    createOneTumorTypeInput(function)
                    return true
                }

                FunctionInput.ONE_STRING -> {
                    createOneStringInput(function)
                    return true
                }

                FunctionInput.ONE_STRING_ONE_INTEGER -> {
                    createOneStringOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_STRINGS_ONE_INTEGER -> {
                    createManyStringsOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_STRINGS_TWO_INTEGERS -> {
                    createManyStringsTwoIntegersInput(function)
                    return true
                }

                FunctionInput.ONE_INTEGER_ONE_STRING -> {
                    createOneIntegerOneStringInput(function)
                    return true
                }

                FunctionInput.ONE_INTEGER_MANY_STRINGS -> {
                    createOneIntegerManyStringsInput(function)
                    return true
                }

                FunctionInput.ONE_TUMOR_STAGE -> {
                    createOneTumorStageInput(function)
                    return true
                }

                FunctionInput.ONE_HLA_ALLELE -> {
                    createOneHlaAlleleInput(function)
                    return true
                }

                FunctionInput.ONE_GENE -> {
                    createOneGeneInput(function)
                    return true
                }

                FunctionInput.ONE_GENE_ONE_INTEGER -> {
                    createOneGeneOneIntegerInput(function)
                    return true
                }

                FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE -> {
                    createOneGeneOneIntegerOneVariantTypeInput(function)
                    return true
                }

                FunctionInput.ONE_GENE_TWO_INTEGERS -> {
                    createOneGeneTwoIntegersInput(function)
                    return true
                }

                FunctionInput.ONE_GENE_MANY_CODONS -> {
                    createOneGeneManyCodonsInput(function)
                    return true
                }

                FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS -> {
                    createOneGeneManyProteinImpactsInput(function)
                    return true
                }

                FunctionInput.MANY_GENES -> {
                    createManyGenesInput(function)
                    return true
                }

                FunctionInput.ONE_DOID_TERM -> {
                    createOneDoidTermInput(function)
                    return true
                }

                else -> {
                    LOGGER.warn("Rule '{}' not defined in parameter type map!", function.rule())
                    return null
                }
            }
        } catch (exception: Exception) {
            LOGGER.warn(exception.message)
            return false
        }
    }

    fun createOneIntegerInput(function: EligibilityFunction): Int {
        assertParamConfig(function, FunctionInput.ONE_INTEGER, 1)
        return (function.parameters().get(0) as String).toInt()
    }

    fun createTwoIntegersInput(function: EligibilityFunction): TwoIntegers {
        assertParamConfig(function, FunctionInput.TWO_INTEGERS, 2)
        return ImmutableTwoIntegers.builder()
            .integer1((function.parameters().get(0) as String).toInt())
            .integer2((function.parameters().get(1) as String).toInt())
            .build()
    }

    fun createManyIntegersInput(function: EligibilityFunction): List<Int> {
        assertParamConfig(function, FunctionInput.MANY_INTEGERS, 1)
        return toStringStream(function.parameters().get(0)).map<Int>(Function<String, Int>({ s: String -> s.toInt() }))
            .collect(Collectors.toList<Int>())
    }

    fun createOneDoubleInput(function: EligibilityFunction): Double {
        assertParamConfig(function, FunctionInput.ONE_DOUBLE, 1)
        return (function.parameters().get(0) as String).toDouble()
    }

    fun createTwoDoublesInput(function: EligibilityFunction): TwoDoubles {
        assertParamConfig(function, FunctionInput.TWO_DOUBLES, 2)
        return ImmutableTwoDoubles.builder()
            .double1((function.parameters().get(0) as String).toDouble())
            .double2((function.parameters().get(1) as String).toDouble())
            .build()
    }

    fun createOneTreatmentCategoryOrTypeInput(function: EligibilityFunction): TreatmentCategoryInput {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE, 1)
        return TreatmentCategoryInput.Companion.fromString(function.parameters().get(0) as String?)
    }

    fun createOneTreatmentCategoryOrTypeOneIntegerInput(function: EligibilityFunction): OneTreatmentCategoryOrTypeOneInteger {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER, 2)
        return ImmutableOneTreatmentCategoryOrTypeOneInteger.builder()
            .treatment(TreatmentCategoryInput.Companion.fromString(function.parameters().get(0) as String?))
            .integer((function.parameters().get(1) as String).toInt())
            .build()
    }

    fun createOneTreatmentCategoryManyTypesInput(function: EligibilityFunction): OneTreatmentCategoryManyTypes {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES, 2)
        return ImmutableOneTreatmentCategoryManyTypes.builder()
            .category(TreatmentCategoryResolver.fromString(function.parameters().get(0) as String?))
            .types(toTreatmentTypeSet(function.parameters().get(1)))
            .build()
    }

    fun createOneTreatmentCategoryManyTypesOneIntegerInput(
        function: EligibilityFunction
    ): OneTreatmentCategoryManyTypesOneInteger {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER, 3)
        return ImmutableOneTreatmentCategoryManyTypesOneInteger.builder()
            .category(TreatmentCategoryResolver.fromString(function.parameters().get(0) as String?))
            .types(toTreatmentTypeSet(function.parameters().get(1)))
            .integer((function.parameters().get(2) as String).toInt())
            .build()
    }

    fun createOneSpecificTreatmentInput(function: EligibilityFunction): Treatment {
        assertParamConfig(function, FunctionInput.ONE_SPECIFIC_TREATMENT, 1)
        return toTreatment(function.parameters().get(0) as String)
    }

    fun createOneSpecificTreatmentOneIntegerInput(function: EligibilityFunction): OneSpecificTreatmentOneInteger {
        assertParamConfig(function, FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER, 2)
        return ImmutableOneSpecificTreatmentOneInteger.builder()
            .treatment(toTreatment(function.parameters().get(0) as String))
            .integer((function.parameters().get(1) as String).toInt())
            .build()
    }

    fun createManySpecificTreatmentsTwoIntegerInput(function: EligibilityFunction): ManySpecificTreatmentsTwoIntegers {
        assertParamConfig(function, FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS, 3)
        return ImmutableManySpecificTreatmentsTwoIntegers.builder()
            .treatments(toTreatments(function.parameters().get(0)))
            .integer1((function.parameters().get(1) as String).toInt())
            .integer2((function.parameters().get(2) as String).toInt())
            .build()
    }

    private fun toTreatments(input: Any): List<Treatment> {
        return toStringStream(input).map<Treatment>(Function<String, Treatment>({ treatmentName: String -> toTreatment(treatmentName) }))
            .collect(Collectors.toList<Treatment>())
    }

    private fun toTreatment(treatmentName: String): Treatment {
        val treatment: Treatment = treatmentDatabase.findTreatmentByName(treatmentName)
        if (treatment == null) {
            throw IllegalStateException("Treatment not found in DB: " + treatmentName)
        }
        return treatment
    }

    private fun toTreatmentTypeSet(input: Any): Set<TreatmentType> {
        return toStringStream(input).map<TreatmentType>(Function<String, TreatmentType>({ input: String? ->
            TreatmentCategoryInput.Companion.treatmentTypeFromString(
                input
            )
        })).collect<Set<TreatmentType>, Any>(Collectors.toSet<TreatmentType>())
    }

    fun createOneTreatmentCategoryManyDrugsInput(function: EligibilityFunction): OneTreatmentCategoryManyDrugs {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS, 2)
        return ImmutableOneTreatmentCategoryManyDrugs.builder()
            .category(TreatmentCategoryResolver.fromString(function.parameters().get(0) as String?))
            .drugs(toDrugSet(function.parameters().get(1)))
            .build()
    }

    fun createManyDrugsInput(function: EligibilityFunction): Set<Drug> {
        assertParamConfig(function, FunctionInput.MANY_DRUGS, 1)
        return toDrugSet(function.parameters().get(0))
    }

    private fun toDrugSet(input: Any): Set<Drug> {
        return toStringStream(input).map<Drug>(Function<String, Drug>({ drugName: String -> toDrug(drugName) }))
            .collect<Set<Drug>, Any>(Collectors.toSet<Drug>())
    }

    private fun toDrug(drugName: String): Drug {
        val drug: Drug? = treatmentDatabase.findDrugByName(drugName)
        if (drug == null) {
            throw IllegalStateException("Drug not found in DB: " + drugName)
        }
        return drug
    }

    fun createOneTumorTypeInput(function: EligibilityFunction): TumorTypeInput {
        assertParamConfig(function, FunctionInput.ONE_TUMOR_TYPE, 1)
        return TumorTypeInput.Companion.fromString(function.parameters().get(0) as String?)
    }

    fun createOneStringInput(function: EligibilityFunction): String {
        assertParamConfig(function, FunctionInput.ONE_STRING, 1)
        return function.parameters().get(0)
    }

    fun createOneStringOneIntegerInput(function: EligibilityFunction): OneIntegerOneString {
        assertParamConfig(function, FunctionInput.ONE_STRING_ONE_INTEGER, 2)
        return ImmutableOneIntegerOneString.builder()
            .string(function.parameters().get(0) as String?)
            .integer((function.parameters().get(1) as String).toInt())
            .build()
    }

    fun createManyStringsOneIntegerInput(function: EligibilityFunction): OneIntegerManyStrings {
        assertParamConfig(function, FunctionInput.MANY_STRINGS_ONE_INTEGER, 2)
        return ImmutableOneIntegerManyStrings.builder()
            .strings(toStringList(function.parameters().get(0)))
            .integer((function.parameters().get(1) as String).toInt())
            .build()
    }

    fun createManyStringsTwoIntegersInput(function: EligibilityFunction): TwoIntegersManyStrings {
        assertParamConfig(function, FunctionInput.MANY_STRINGS_TWO_INTEGERS, 3)
        return ImmutableTwoIntegersManyStrings.builder()
            .strings(toStringList(function.parameters().get(0)))
            .integer1((function.parameters().get(1) as String).toInt())
            .integer2((function.parameters().get(2) as String).toInt())
            .build()
    }

    fun createOneIntegerOneStringInput(function: EligibilityFunction): OneIntegerOneString {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_ONE_STRING, 2)
        return ImmutableOneIntegerOneString.builder()
            .integer((function.parameters().get(0) as String).toInt())
            .string(function.parameters().get(1) as String?)
            .build()
    }

    fun createOneIntegerManyStringsInput(function: EligibilityFunction): OneIntegerManyStrings {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_MANY_STRINGS, 2)
        return ImmutableOneIntegerManyStrings.builder()
            .integer((function.parameters().get(0) as String).toInt())
            .strings(toStringList(function.parameters().get(1)))
            .build()
    }

    fun createOneTumorStageInput(function: EligibilityFunction): TumorStage {
        assertParamConfig(function, FunctionInput.ONE_TUMOR_STAGE, 1)
        return TumorStage.valueOf(function.parameters().get(0) as String?)
    }

    fun createOneHlaAlleleInput(function: EligibilityFunction): OneHlaAllele {
        assertParamConfig(function, FunctionInput.ONE_HLA_ALLELE, 1)
        val allele: String = function.parameters().get(0)
        if (!MolecularInputChecker.isHlaAllele(allele)) {
            throw IllegalArgumentException("Not a proper HLA allele: " + allele)
        }
        return ImmutableOneHlaAllele.builder().allele(allele).build()
    }

    fun createOneGeneInput(function: EligibilityFunction): OneGene {
        assertParamConfig(function, FunctionInput.ONE_GENE, 1)
        val gene: String = function.parameters().get(0)
        if (!molecularInputChecker.isGene(gene)) {
            throw IllegalStateException("Not a valid gene: " + gene)
        }
        return ImmutableOneGene.builder().geneName(gene).build()
    }

    fun createOneGeneOneIntegerInput(function: EligibilityFunction): OneGeneOneInteger {
        assertParamConfig(function, FunctionInput.ONE_GENE_ONE_INTEGER, 2)
        val gene: String = function.parameters().get(0)
        if (!molecularInputChecker.isGene(gene)) {
            throw IllegalStateException("Not a valid gene: " + gene)
        }
        return ImmutableOneGeneOneInteger.builder().geneName(gene).integer((function.parameters().get(1) as String).toInt()).build()
    }

    fun createOneGeneOneIntegerOneVariantTypeInput(function: EligibilityFunction): OneGeneOneIntegerOneVariantType {
        assertParamConfig(function, FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE, 3)
        val gene: String = function.parameters().get(0)
        if (!molecularInputChecker.isGene(gene)) {
            throw IllegalStateException("Not a valid gene: " + gene)
        }
        return ImmutableOneGeneOneIntegerOneVariantType.builder()
            .geneName(gene)
            .integer((function.parameters().get(1) as String).toInt())
            .variantType(VariantTypeInput.valueOf((function.parameters().get(2) as String?)!!))
            .build()
    }

    fun createOneGeneTwoIntegersInput(function: EligibilityFunction): OneGeneTwoIntegers {
        assertParamConfig(function, FunctionInput.ONE_GENE_TWO_INTEGERS, 3)
        val gene: String = function.parameters().get(0)
        if (!molecularInputChecker.isGene(gene)) {
            throw IllegalStateException("Not a valid gene: " + gene)
        }
        return ImmutableOneGeneTwoIntegers.builder()
            .geneName(gene)
            .integer1((function.parameters().get(1) as String).toInt())
            .integer2((function.parameters().get(2) as String).toInt())
            .build()
    }

    fun createOneGeneManyCodonsInput(function: EligibilityFunction): OneGeneManyCodons {
        assertParamConfig(function, FunctionInput.ONE_GENE_MANY_CODONS, 2)
        val gene: String = function.parameters().get(0)
        if (!molecularInputChecker.isGene(gene)) {
            throw IllegalStateException("Not a valid gene: " + gene)
        }
        val codons: List<String> = toStringList(function.parameters().get(1))
        for (codon: String in codons) {
            if (!MolecularInputChecker.isCodon(codon)) {
                throw IllegalStateException("Not a valid codon: " + codon)
            }
        }
        return ImmutableOneGeneManyCodons.builder().geneName(gene).codons(codons).build()
    }

    fun createOneGeneManyProteinImpactsInput(function: EligibilityFunction): OneGeneManyProteinImpacts {
        assertParamConfig(function, FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS, 2)
        val gene: String = function.parameters().get(0)
        if (!molecularInputChecker.isGene(gene)) {
            throw IllegalStateException("Not a valid gene: " + gene)
        }
        val proteinImpacts: List<String> = toStringList(function.parameters().get(1))
        for (proteinImpact: String in proteinImpacts) {
            if (!MolecularInputChecker.isProteinImpact(proteinImpact)) {
                throw IllegalStateException("Not a valid protein impact: " + proteinImpact)
            }
        }
        return ImmutableOneGeneManyProteinImpacts.builder().geneName(gene).proteinImpacts(proteinImpacts).build()
    }

    fun createManyGenesInput(function: EligibilityFunction): ManyGenes {
        assertParamConfig(function, FunctionInput.MANY_GENES, 1)
        val genes: List<String> = toStringList(function.parameters().get(0))
        for (gene: String in genes) {
            if (!molecularInputChecker.isGene(gene)) {
                throw IllegalStateException("Not a valid gene: " + gene)
            }
        }
        return ImmutableManyGenes.builder().geneNames(genes).build()
    }

    fun createOneDoidTermInput(function: EligibilityFunction): String {
        assertParamConfig(function, FunctionInput.ONE_DOID_TERM, 1)
        val param: String = function.parameters().get(0)
        if (doidModel.resolveDoidForTerm(param) == null) {
            throw IllegalStateException("Not a valid DOID term: " + param)
        }
        return param
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(FunctionInputResolver::class.java)
        private val MANY_STRING_SEPARATOR: String = ";"
        private fun hasValidCompositeInputs(function: EligibilityFunction): Boolean {
            try {
                val requiredInputs: CompositeInput = CompositeRules.inputsForCompositeRule(function.rule())
                if (requiredInputs == CompositeInput.AT_LEAST_2) {
                    createAtLeastTwoCompositeParameters(function)
                } else if (requiredInputs == CompositeInput.EXACTLY_1) {
                    createOneCompositeParameter(function)
                } else {
                    throw IllegalStateException(
                        "Could not interpret composite inputs for rule '" + function.rule() + "': " + requiredInputs
                    )
                }
                return true
            } catch (exception: Exception) {
                return false
            }
        }

        fun createOneCompositeParameter(function: EligibilityFunction): EligibilityFunction {
            assertParamCount(function, 1)
            return function.parameters().get(0) as EligibilityFunction
        }

        fun createAtLeastTwoCompositeParameters(function: EligibilityFunction): List<EligibilityFunction> {
            if (function.parameters().size < 2) {
                throw IllegalArgumentException(
                    "Not enough parameters passed into '" + function.rule() + "': " + function.parameters().size
                )
            }
            return function.parameters().stream()
                .map<EligibilityFunction>(Function<Any, EligibilityFunction?>({ input: Any? -> input as EligibilityFunction? }))
                .collect<List<EligibilityFunction>, Any>(Collectors.toList<EligibilityFunction>())
        }

        private fun toStringList(param: Any): List<String> {
            return toStringStream(param).collect(Collectors.toList<String>())
        }

        private fun toStringStream(param: Any): Stream<String> {
            return Arrays.stream((param as String).split(MANY_STRING_SEPARATOR.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray())
                .map(
                    Function({ obj: String -> obj.trim({ it <= ' ' }) })
                )
        }

        private fun assertParamConfig(function: EligibilityFunction, requestedInput: FunctionInput, expectedCount: Int) {
            assertParamType(function, requestedInput)
            assertParamCount(function, expectedCount)
        }

        private fun assertParamType(function: EligibilityFunction, requestedInput: FunctionInput) {
            if (requestedInput != FunctionInputMapping.RULE_INPUT_MAP.get(function.rule())) {
                throw IllegalStateException("Incorrect type of inputs requested for '" + function.rule() + "': " + requestedInput)
            }
        }

        private fun assertParamCount(function: EligibilityFunction, expectedCount: Int) {
            if (function.parameters().size != expectedCount) {
                throw IllegalArgumentException(
                    "Invalid number of inputs passed to '" + function.rule() + "': " + function.parameters().size
                )
            }
        }
    }
}
