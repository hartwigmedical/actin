package com.hartwig.actin.trial.input

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Cyp
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.datamodel.clinical.Transporter
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionInput
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.medication.MedicationInputChecker
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.composite.CompositeInput
import com.hartwig.actin.trial.input.composite.CompositeRules
import com.hartwig.actin.trial.input.datamodel.TreatmentCategoryInput
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput
import com.hartwig.actin.trial.input.single.ManyDrugsOneInteger
import com.hartwig.actin.trial.input.single.ManyDrugsTwoIntegers
import com.hartwig.actin.trial.input.single.ManyGenes
import com.hartwig.actin.trial.input.single.ManyIntents
import com.hartwig.actin.trial.input.single.ManyIntentsOneInteger
import com.hartwig.actin.trial.input.single.ManySpecificTreatmentsTwoIntegers
import com.hartwig.actin.trial.input.single.OneCypOneInteger
import com.hartwig.actin.trial.input.single.OneDoidTermOneInteger
import com.hartwig.actin.trial.input.single.OneDoubleOneGender
import com.hartwig.actin.trial.input.single.OneGene
import com.hartwig.actin.trial.input.single.OneGeneManyCodons
import com.hartwig.actin.trial.input.single.OneGeneManyProteinImpacts
import com.hartwig.actin.trial.input.single.OneGeneOneInteger
import com.hartwig.actin.trial.input.single.OneGeneOneIntegerOneVariantType
import com.hartwig.actin.trial.input.single.OneGeneTwoIntegers
import com.hartwig.actin.trial.input.single.OneHaplotype
import com.hartwig.actin.trial.input.single.OneHlaAllele
import com.hartwig.actin.trial.input.single.OneIntegerManyDoidTerms
import com.hartwig.actin.trial.input.single.OneIntegerManyIcdTitles
import com.hartwig.actin.trial.input.single.OneIntegerManyStrings
import com.hartwig.actin.trial.input.single.OneIntegerOneString
import com.hartwig.actin.trial.input.single.OneMedicationCategory
import com.hartwig.actin.trial.input.single.OneSpecificDrugOneTreatmentCategoryManyTypes
import com.hartwig.actin.trial.input.single.OneSpecificTreatmentOneInteger
import com.hartwig.actin.trial.input.single.OneTreatmentCategoryManyDrugs
import com.hartwig.actin.trial.input.single.OneTreatmentCategoryManyIntents
import com.hartwig.actin.trial.input.single.OneTreatmentCategoryManyTypes
import com.hartwig.actin.trial.input.single.OneTreatmentCategoryManyTypesManyDrugs
import com.hartwig.actin.trial.input.single.OneTreatmentCategoryManyTypesOneInteger
import com.hartwig.actin.trial.input.single.OneTreatmentCategoryOrTypeOneInteger
import com.hartwig.actin.trial.input.single.TwoDoubles
import com.hartwig.actin.trial.input.single.TwoIntegers
import com.hartwig.actin.trial.input.single.TwoStrings
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.Locale

class FunctionInputResolver(
    private val doidModel: DoidModel,
    val icdModel: IcdModel,
    private val molecularInputChecker: MolecularInputChecker,
    private val treatmentDatabase: TreatmentDatabase,
    private val medicationCategories: MedicationCategories
) {

    fun hasValidInputs(function: EligibilityFunction): Boolean? {
        return if (CompositeRules.isComposite(function.rule)) hasValidCompositeInputs(function) else hasValidSingleInputs(function)
    }

    private fun hasValidSingleInputs(function: EligibilityFunction): Boolean? {
        try {
            when (function.rule.input) {
                FunctionInput.NONE -> {
                    return function.parameters.isEmpty()
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

                FunctionInput.ONE_CYP -> {
                    createOneCypInput(function)
                    return true
                }

                FunctionInput.ONE_CYP_ONE_INTEGER -> {
                    createOneCypOneIntegerInput(function)
                    return true
                }

                FunctionInput.ONE_TRANSPORTER -> {
                    createOneTransporterInput(function)
                    return true
                }

                FunctionInput.ONE_DOUBLE -> {
                    createOneDoubleInput(function)
                    return true
                }

                FunctionInput.ONE_DOUBLE_ONE_GENDER -> {
                    createOneDoubleOneGenderInput(function)
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

                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS -> {
                    createOneTreatmentCategoryManyIntentsInput(function)
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

                FunctionInput.ONE_SPECIFIC_DRUG_ONE_TREATMENT_CATEGORY_MANY_TYPES -> {
                    createOneSpecificDrugOneTreatmentCategoryManyTypesInput(function)
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

                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS -> {
                    createOneTreatmentCategoryManyTypesManyDrugsInput(function)
                    return true
                }

                FunctionInput.MANY_DRUGS -> {
                    createManyDrugsInput(function)
                    return true
                }

                FunctionInput.MANY_DRUGS_ONE_INTEGER -> {
                    createManyDrugsOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_DRUGS_TWO_INTEGERS -> {
                    createManyDrugsTwoIntegersInput(function)
                    return true
                }

                FunctionInput.ONE_ICD_TITLE -> {
                    createOneIcdTitleInput(function)
                    return true
                }

                FunctionInput.MANY_ICD_TITLES -> {
                    createManyIcdTitlesInput(function)
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

                FunctionInput.TWO_STRINGS -> {
                    createTwoStringsInput(function)
                    return true
                }

                FunctionInput.ONE_STRING_ONE_INTEGER -> {
                    createOneStringOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_STRINGS -> {
                    createManyStringsInput(function)
                    return true
                }

                FunctionInput.MANY_STRINGS_ONE_INTEGER -> {
                    createManyStringsOneIntegerInput(function)
                    return true
                }

                FunctionInput.ONE_INTEGER_MANY_DOID_TERMS -> {
                    createOneIntegerManyDoidTermsInput(function)
                    return true
                }

                FunctionInput.ONE_INTEGER_MANY_ICD_TITLES -> {
                    createOneIntegerManyIcdTitlesInput(function)
                    return true
                }

                FunctionInput.MANY_TUMOR_STAGES -> {
                    createManyTumorStagesInput(function)
                    return true
                }

                FunctionInput.ONE_HLA_ALLELE -> {
                    createOneHlaAlleleInput(function)
                    return true
                }

                FunctionInput.ONE_HAPLOTYPE -> {
                    createOneHaplotypeInput(function)
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

                FunctionInput.ONE_DOID_TERM_ONE_INTEGER -> {
                    createOneDoidTermOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_DOID_TERMS -> {
                    createManyDoidTermsInput(function)
                    return true
                }

                FunctionInput.ONE_RECEPTOR_TYPE -> {
                    createOneReceptorTypeInput(function)
                    return true
                }

                FunctionInput.MANY_INTENTS_ONE_INTEGER -> {
                    createManyIntentsOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_INTENTS -> {
                    createManyIntentsInput(function)
                    return true
                }

                FunctionInput.ONE_MEDICATION_CATEGORY -> {
                    createOneMedicationCategoryInput(function)
                    return true
                }

                FunctionInput.ONE_MEDICATION_CATEGORY_ONE_INTEGER -> {
                    createOneMedicationCategoryOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_MEDICATION_CATEGORIES_ONE_INTEGER -> {
                    createManyMedicationCategoriesOneIntegerInput(function)
                    return true
                }

                FunctionInput.MANY_MEDICATION_CATEGORIES_TWO_INTEGERS -> {
                    createManyMedicationCategoriesTwoIntegersInput(function)
                    return true
                }

                else -> {
                    LOGGER.warn("Rule '{}' not defined in parameter type map!", function.rule)
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
        return (function.parameters[0] as String).toInt()
    }

    fun createTwoIntegersInput(function: EligibilityFunction): TwoIntegers {
        assertParamConfig(function, FunctionInput.TWO_INTEGERS, 2)
        return TwoIntegers(
            integer1 = (function.parameters[0] as String).toInt(),
            integer2 = (function.parameters[1] as String).toInt(),
        )
    }

    fun createManyIntegersInput(function: EligibilityFunction): List<Int> {
        assertParamConfig(function, FunctionInput.MANY_INTEGERS, 1)
        return toStringList(function.parameters.first()).map(String::toInt)
    }

    fun createOneDoubleInput(function: EligibilityFunction): Double {
        assertParamConfig(function, FunctionInput.ONE_DOUBLE, 1)
        return parameterAsString(function, 0).toDouble()
    }

    fun createOneDoubleOneGenderInput(function: EligibilityFunction): OneDoubleOneGender {
        assertParamConfig(function, FunctionInput.ONE_DOUBLE_ONE_GENDER, 2)
        return OneDoubleOneGender(
            double = parameterAsString(function, 0).toDouble(),
            gender = toGender(function.parameters[1] as String)
        )
    }

    fun createTwoDoublesInput(function: EligibilityFunction): TwoDoubles {
        assertParamConfig(function, FunctionInput.TWO_DOUBLES, 2)
        return TwoDoubles(
            double1 = parameterAsString(function, 0).toDouble(),
            double2 = parameterAsString(function, 1).toDouble()
        )
    }

    fun createOneTreatmentCategoryOrTypeInput(function: EligibilityFunction): TreatmentCategoryInput {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE, 1)
        return TreatmentCategoryInput.fromString(parameterAsString(function, 0))
    }

    fun createOneTreatmentCategoryOrTypeOneIntegerInput(function: EligibilityFunction): OneTreatmentCategoryOrTypeOneInteger {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER, 2)
        return OneTreatmentCategoryOrTypeOneInteger(
            treatment = TreatmentCategoryInput.fromString(parameterAsString(function, 0)),
            integer = parameterAsInt(function, 1)
        )
    }

    fun createOneTreatmentCategoryManyTypesInput(function: EligibilityFunction): OneTreatmentCategoryManyTypes {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES, 2)
        return OneTreatmentCategoryManyTypes(
            category = TreatmentCategoryResolver.fromString(parameterAsString(function, 0)),
            types = toTreatmentTypeSet(function.parameters[1])
        )
    }

    fun createOneTreatmentCategoryManyTypesOneIntegerInput(
        function: EligibilityFunction
    ): OneTreatmentCategoryManyTypesOneInteger {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER, 3)
        return OneTreatmentCategoryManyTypesOneInteger(
            category = TreatmentCategoryResolver.fromString(parameterAsString(function, 0)),
            types = toTreatmentTypeSet(function.parameters[1]),
            integer = parameterAsInt(function, 2)
        )
    }

    fun createOneTreatmentCategoryManyIntentsInput(function: EligibilityFunction): OneTreatmentCategoryManyIntents {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS, 2)
        return OneTreatmentCategoryManyIntents(
            category = TreatmentCategoryResolver.fromString(parameterAsString(function, 0)),
            intents = toIntents(function.parameters[1])
        )
    }

    fun createOneSpecificTreatmentInput(function: EligibilityFunction): Treatment {
        assertParamConfig(function, FunctionInput.ONE_SPECIFIC_TREATMENT, 1)
        return toTreatment(parameterAsString(function, 0))
    }

    fun createOneSpecificTreatmentOneIntegerInput(function: EligibilityFunction): OneSpecificTreatmentOneInteger {
        assertParamConfig(function, FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER, 2)
        return OneSpecificTreatmentOneInteger(
            treatment = toTreatment(parameterAsString(function, 0)),
            integer = parameterAsInt(function, 1)
        )
    }

    fun createManySpecificTreatmentsTwoIntegerInput(function: EligibilityFunction): ManySpecificTreatmentsTwoIntegers {
        assertParamConfig(function, FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS, 3)
        return ManySpecificTreatmentsTwoIntegers(
            treatments = toTreatments(function.parameters.first()),
            integer1 = parameterAsInt(function, 1),
            integer2 = parameterAsInt(function, 2)
        )
    }

    fun createOneSpecificDrugOneTreatmentCategoryManyTypesInput(
        function: EligibilityFunction
    ): OneSpecificDrugOneTreatmentCategoryManyTypes {
        assertParamConfig(function, FunctionInput.ONE_SPECIFIC_DRUG_ONE_TREATMENT_CATEGORY_MANY_TYPES, 3)
        return OneSpecificDrugOneTreatmentCategoryManyTypes(
            drug = toDrug(parameterAsString(function, 0)),
            category = TreatmentCategoryResolver.fromString(parameterAsString(function, 1)),
            types = toTreatmentTypeSet(function.parameters[2])
        )
    }

    private fun toTreatments(input: Any): List<Treatment> {
        return toStringList(input).map(::toTreatment)
    }

    private fun toTreatment(treatmentName: String): Treatment {
        return treatmentDatabase.findTreatmentByName(treatmentName)
            ?: throw IllegalStateException("Treatment not found in DB: $treatmentName")
    }

    private fun toTreatmentTypeSet(input: Any): Set<TreatmentType> {
        return toStringList(input).map(TreatmentCategoryInput::treatmentTypeFromString).toSet()
    }

    fun createOneTreatmentCategoryManyDrugsInput(function: EligibilityFunction): OneTreatmentCategoryManyDrugs {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS, 2)
        return OneTreatmentCategoryManyDrugs(
            category = TreatmentCategoryResolver.fromString(parameterAsString(function, 0)),
            drugs = toDrugSet(function.parameters[1])
        )
    }

    fun createOneTreatmentCategoryManyTypesManyDrugsInput(function: EligibilityFunction): OneTreatmentCategoryManyTypesManyDrugs {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS, 3)
        return OneTreatmentCategoryManyTypesManyDrugs(
            category = TreatmentCategoryResolver.fromString(parameterAsString(function, 0)),
            types = toTreatmentTypeSet(function.parameters[1]),
            drugs = toDrugSet(function.parameters[2])
        )
    }

    fun createManyDrugsInput(function: EligibilityFunction): Set<Drug> {
        assertParamConfig(function, FunctionInput.MANY_DRUGS, 1)
        return toDrugSet(function.parameters.first())
    }

    fun createManyDrugsOneIntegerInput(function: EligibilityFunction): ManyDrugsOneInteger {
        assertParamConfig(function, FunctionInput.MANY_DRUGS_ONE_INTEGER, 2)
        return ManyDrugsOneInteger(toDrugSet(function.parameters.first()), parameterAsInt(function, 1))
    }

    fun createManyDrugsTwoIntegersInput(function: EligibilityFunction): ManyDrugsTwoIntegers {
        assertParamConfig(function, FunctionInput.MANY_DRUGS_TWO_INTEGERS, 3)
        return ManyDrugsTwoIntegers(toDrugSet(function.parameters.first()), parameterAsInt(function, 1), parameterAsInt(function, 2))
    }

    private fun toDrugSet(input: Any): Set<Drug> {
        return toStringList(input).map(::toDrug).toSet()
    }

    private fun toDrug(drugName: String): Drug {
        return treatmentDatabase.findDrugByName(drugName) ?: throw IllegalStateException("Drug not found in DB: $drugName")
    }

    fun createOneIcdTitleInput(function: EligibilityFunction): String {
        assertParamConfig(function, FunctionInput.ONE_ICD_TITLE, 1)
        val input = parameterAsString(function, 0)
        if (!icdModel.isValidIcdTitle(input)) {
            throw IllegalStateException("ICD title(s) not valid: $input")
        }
        return input
    }

    fun createManyIcdTitlesInput(function: EligibilityFunction): List<String> {
        assertParamConfig(function, FunctionInput.MANY_ICD_TITLES, 1)
        val icdStringList = toStringList(function.parameters.first())
        val invalidTitles = icdStringList.filter { !icdModel.isValidIcdTitle(it) }
        if (invalidTitles.isNotEmpty()) {
            throw IllegalStateException("ICD title(s) not valid: ${invalidTitles.joinToString(", ")}")
        }
        return icdStringList
    }

    fun createOneTumorTypeInput(function: EligibilityFunction): TumorTypeInput {
        assertParamConfig(function, FunctionInput.ONE_TUMOR_TYPE, 1)
        return TumorTypeInput.fromString(parameterAsString(function, 0))
    }

    fun createOneStringInput(function: EligibilityFunction): String {
        assertParamConfig(function, FunctionInput.ONE_STRING, 1)
        return parameterAsString(function, 0)
    }

    fun createTwoStringsInput(function: EligibilityFunction): TwoStrings {
        assertParamConfig(function, FunctionInput.TWO_STRINGS, 2)
        return TwoStrings(
            string1 = parameterAsString(function, 0),
            string2 = parameterAsString(function, 1),
        )
    }

    fun createOneStringOneIntegerInput(function: EligibilityFunction): OneIntegerOneString {
        assertParamConfig(function, FunctionInput.ONE_STRING_ONE_INTEGER, 2)
        return OneIntegerOneString(
            string = parameterAsString(function, 0),
            integer = parameterAsInt(function, 1)
        )
    }

    fun createManyStringsInput(function: EligibilityFunction): List<String> {
        assertParamConfig(function, FunctionInput.MANY_STRINGS, 1)
        return toStringList(function.parameters.first())
    }

    fun createManyStringsOneIntegerInput(function: EligibilityFunction): OneIntegerManyStrings {
        assertParamConfig(function, FunctionInput.MANY_STRINGS_ONE_INTEGER, 2)
        return OneIntegerManyStrings(
            strings = toStringList(function.parameters.first()),
            integer = parameterAsInt(function, 1)
        )
    }

    fun createOneIntegerManyDoidTermsInput(function: EligibilityFunction): OneIntegerManyDoidTerms {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_MANY_DOID_TERMS, 2)
        val doidStringList = toStringList(function.parameters[1])
        val invalidTerms = doidStringList.filter { doidModel.resolveDoidForTerm(it) == null }
        if (invalidTerms.isNotEmpty()) {
            throw IllegalStateException("DOID term(s) not valid: ${invalidTerms.joinToString(", ")}")
        }
        return OneIntegerManyDoidTerms(
            integer = parameterAsInt(function, 0),
            doidTerms = doidStringList
        )
    }

    fun createOneIntegerManyIcdTitlesInput(function: EligibilityFunction): OneIntegerManyIcdTitles {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_MANY_ICD_TITLES, 2)
        val icdStringList = toStringList(function.parameters[1])
        val invalidTitles = icdStringList.filter { !icdModel.isValidIcdTitle(it) }
        if (invalidTitles.isNotEmpty()) {
            throw IllegalStateException("ICD title(s) not valid: ${invalidTitles.joinToString(", ")}")
        }

        return OneIntegerManyIcdTitles(
            integer = parameterAsInt(function, 0),
            icdTitles = icdStringList
        )
    }

    fun createManyTumorStagesInput(function: EligibilityFunction): Set<TumorStage> {
        assertParamConfig(function, FunctionInput.MANY_TUMOR_STAGES, 1)
        return toStringList(function.parameters.first()).map(TumorStage::valueOf).toSet()
    }

    fun createOneHlaAlleleInput(function: EligibilityFunction): OneHlaAllele {
        assertParamConfig(function, FunctionInput.ONE_HLA_ALLELE, 1)
        val allele = function.parameters.first() as String
        if (!MolecularInputChecker.isHlaAllele(allele)) {
            throw IllegalArgumentException("Not a proper HLA allele: $allele")
        }
        return OneHlaAllele(allele)
    }

    fun createOneHaplotypeInput(function: EligibilityFunction): OneHaplotype {
        assertParamConfig(function, FunctionInput.ONE_HAPLOTYPE, 1)

        val haplotype = function.parameters.first() as String
        if (!MolecularInputChecker.isHaplotype(haplotype)) {
            throw IllegalArgumentException("Not a proper haplotype: $haplotype")
        }

        return OneHaplotype(haplotype)
    }

    fun createOneGeneInput(function: EligibilityFunction): OneGene {
        assertParamConfig(function, FunctionInput.ONE_GENE, 1)
        return OneGene(firstParameterAsGene(function))
    }

    fun createOneGeneOneIntegerInput(function: EligibilityFunction): OneGeneOneInteger {
        assertParamConfig(function, FunctionInput.ONE_GENE_ONE_INTEGER, 2)
        return OneGeneOneInteger(geneName = firstParameterAsGene(function), integer = (function.parameters[1] as String).toInt())
    }

    fun createOneGeneOneIntegerOneVariantTypeInput(function: EligibilityFunction): OneGeneOneIntegerOneVariantType {
        assertParamConfig(function, FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE, 3)
        return OneGeneOneIntegerOneVariantType(
            geneName = firstParameterAsGene(function),
            integer = (function.parameters[1] as String).toInt(),
            variantType = VariantTypeInput.valueOf(function.parameters[2] as String)
        )
    }

    fun createOneGeneTwoIntegersInput(function: EligibilityFunction): OneGeneTwoIntegers {
        assertParamConfig(function, FunctionInput.ONE_GENE_TWO_INTEGERS, 3)
        return OneGeneTwoIntegers(
            geneName = firstParameterAsGene(function),
            integer1 = (function.parameters[1] as String).toInt(),
            integer2 = (function.parameters[2] as String).toInt()
        )
    }

    fun createOneGeneManyCodonsInput(function: EligibilityFunction): OneGeneManyCodons {
        assertParamConfig(function, FunctionInput.ONE_GENE_MANY_CODONS, 2)
        val codons = toStringList(function.parameters[1])
        for (codon in codons) {
            if (!MolecularInputChecker.isCodon(codon)) {
                throw IllegalStateException("Not a valid codon: $codon")
            }
        }
        return OneGeneManyCodons(geneName = firstParameterAsGene(function), codons = codons)
    }

    fun createOneGeneManyProteinImpactsInput(function: EligibilityFunction): OneGeneManyProteinImpacts {
        assertParamConfig(function, FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS, 2)
        val gene = firstParameterAsGene(function)
        val proteinImpacts = toStringList(function.parameters[1]).toSet()
        for (proteinImpact in proteinImpacts) {
            if (!MolecularInputChecker.isProteinImpact(proteinImpact)) {
                throw IllegalStateException("Not a valid protein impact: $proteinImpact")
            }
        }
        return OneGeneManyProteinImpacts(geneName = gene, proteinImpacts = proteinImpacts)
    }

    fun createManyGenesInput(function: EligibilityFunction): ManyGenes {
        assertParamConfig(function, FunctionInput.MANY_GENES, 1)
        val geneNames = toStringList(function.parameters.first()).toSet()
        for (gene in geneNames) {
            if (!molecularInputChecker.isGene(gene)) {
                throw IllegalStateException("Not a valid gene: $gene")
            }
        }
        return ManyGenes(geneNames = geneNames)
    }

    fun createOneDoidTermInput(function: EligibilityFunction): String {
        assertParamConfig(function, FunctionInput.ONE_DOID_TERM, 1)
        val param = parameterAsString(function, 0)
        if (doidModel.resolveDoidForTerm(param) == null) {
            throw IllegalStateException("Not a valid DOID term: $param")
        }
        return param
    }

    fun createOneDoidTermOneIntegerInput(function: EligibilityFunction): OneDoidTermOneInteger {
        assertParamConfig(function, FunctionInput.ONE_DOID_TERM_ONE_INTEGER, 2)

        val doidString = parameterAsString(function, 0)
        if (doidModel.resolveDoidForTerm(doidString) == null) {
            throw IllegalStateException("Not a valid DOID term: $doidString")
        }

        return OneDoidTermOneInteger(
            doidTerm = doidString,
            integer = parameterAsInt(function, 1)
        )
    }

    fun createManyDoidTermsInput(function: EligibilityFunction): List<String> {
        assertParamConfig(function, FunctionInput.MANY_DOID_TERMS, 1)

        val doidStringList = toStringList(function.parameters.first())
        val invalidTerms = doidStringList.filter { doidModel.resolveDoidForTerm(it) == null }
        if (invalidTerms.isNotEmpty()) {
            throw IllegalStateException("DOID term(s) not valid: ${invalidTerms.joinToString(", ")}")
        }
        return doidStringList
    }

    fun createOneReceptorTypeInput(function: EligibilityFunction): ReceptorType {
        assertParamConfig(function, FunctionInput.ONE_RECEPTOR_TYPE, 1)
        return ReceptorType.valueOf(parameterAsString(function, 0))
    }

    fun createManyIntentsInput(function: EligibilityFunction): ManyIntents {
        assertParamConfig(function, FunctionInput.MANY_INTENTS, 1)

        return ManyIntents(toIntents(function.parameters.first()))
    }

    fun createManyIntentsOneIntegerInput(function: EligibilityFunction): ManyIntentsOneInteger {
        assertParamConfig(function, FunctionInput.MANY_INTENTS_ONE_INTEGER, 2)

        return ManyIntentsOneInteger(
            intents = toIntents(function.parameters.first()),
            integer = parameterAsInt(function, 1),
        )
    }

    fun createOneMedicationCategoryInput(function: EligibilityFunction): OneMedicationCategory {
        assertParamConfig(function, FunctionInput.ONE_MEDICATION_CATEGORY, 1)
        val categoryName = parameterAsString(function, 0)
        return OneMedicationCategory(categoryName, medicationCategories.resolve(categoryName))
    }

    fun createOneMedicationCategoryOneIntegerInput(function: EligibilityFunction): Pair<OneMedicationCategory, Int> {
        assertParamConfig(function, FunctionInput.ONE_MEDICATION_CATEGORY_ONE_INTEGER, 2)
        val categoryName = parameterAsString(function, 0)
        return Pair(OneMedicationCategory(categoryName, medicationCategories.resolve(categoryName)), parameterAsInt(function, 1))
    }

    fun createManyMedicationCategoriesOneIntegerInput(function: EligibilityFunction): Pair<Map<String, Set<AtcLevel>>, Int> {
        assertParamConfig(function, FunctionInput.MANY_MEDICATION_CATEGORIES_ONE_INTEGER, 2)
        return Pair(toStringList(function.parameters[0]).associate { cat -> toMedicationCategoryMap(cat) }, parameterAsInt(function, 1))
    }

    fun createManyMedicationCategoriesTwoIntegersInput(function: EligibilityFunction): Triple<Map<String, Set<AtcLevel>>, Int, Int> {
        assertParamConfig(function, FunctionInput.MANY_MEDICATION_CATEGORIES_TWO_INTEGERS, 3)
        return Triple(
            toStringList(function.parameters[0]).associate { cat -> toMedicationCategoryMap(cat) },
            parameterAsInt(function, 1),
            parameterAsInt(function, 2)
        )
    }

    private fun toMedicationCategoryMap(category: String): Pair<String, Set<AtcLevel>> {
        return medicationCategories.resolveCategoryName(category) to medicationCategories.resolve(category)
    }

    private fun toGender(genderName: String): Gender {
        try {
            return Gender.valueOf(genderName.uppercase(Locale.getDefault()))
        } catch (e: Exception) {
            throw IllegalStateException("Gender name not found: $genderName")
        }
    }

    private fun toIntents(input: Any): Set<Intent> {
        return toStringList(input).map(::toIntent).toSet()
    }

    private fun toIntent(intentName: String): Intent {
        try {
            return Intent.valueOf(intentName.uppercase(Locale.getDefault()))
        } catch (e: Exception) {
            throw IllegalStateException("Intent name not found: $intentName")
        }
    }

    fun createOneCypInput(function: EligibilityFunction): Cyp {
        assertParamConfig(function, FunctionInput.ONE_CYP, 1)

        val cyp = function.parameters.first() as String
        if (!MedicationInputChecker.isCyp(cyp)) {
            throw IllegalArgumentException("Not a proper CYP: $cyp")
        }

        return Cyp.valueOf(cyp)
    }

    fun createOneCypOneIntegerInput(function: EligibilityFunction): OneCypOneInteger {
        assertParamConfig(function, FunctionInput.ONE_CYP_ONE_INTEGER, 2)
        val cyp = parameterAsString(function, 0)
        if (!MedicationInputChecker.isCyp(cyp)) {
            throw IllegalArgumentException("Not a proper CYP: $cyp")
        }
        return OneCypOneInteger(cyp = Cyp.valueOf(cyp), integer = parameterAsString(function, 1).toInt())
    }

    fun createOneTransporterInput(function: EligibilityFunction): Transporter {
        assertParamConfig(function, FunctionInput.ONE_TRANSPORTER, 1)

        val transporter = function.parameters.first() as String
        if (!MedicationInputChecker.isTransporter(transporter)) {
            throw IllegalArgumentException("Not a proper transporter: $transporter")
        }
        return Transporter.valueOf(transporter)
    }

    private fun parameterAsString(function: EligibilityFunction, i: Int) = function.parameters[i] as String

    private fun parameterAsInt(function: EligibilityFunction, i: Int) = parameterAsString(function, i).toInt()

    private fun firstParameterAsGene(function: EligibilityFunction): String {
        val gene = parameterAsString(function, 0)
        if (!molecularInputChecker.isGene(gene)) {
            throw IllegalStateException("Not a valid gene: $gene")
        }
        return gene
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(FunctionInputResolver::class.java)
        private const val MANY_STRING_SEPARATOR: String = ";"

        private fun hasValidCompositeInputs(function: EligibilityFunction): Boolean {
            return try {
                when (CompositeRules.inputsForCompositeRule(function.rule)) {
                    CompositeInput.AT_LEAST_2 -> {
                        createAtLeastTwoCompositeParameters(function)
                    }

                    CompositeInput.EXACTLY_1 -> {
                        createOneCompositeParameter(function)
                    }
                }
                true
            } catch (exception: Exception) {
                LOGGER.warn(exception.message)
                false
            }
        }

        fun createOneCompositeParameter(function: EligibilityFunction): EligibilityFunction {
            assertParamCount(function, 1)
            return function.parameters.first() as EligibilityFunction
        }

        fun createAtLeastTwoCompositeParameters(function: EligibilityFunction): List<EligibilityFunction> {
            if (function.parameters.size < 2) {
                throw IllegalArgumentException(
                    "Not enough parameters passed into '${function.rule}': ${function.parameters.size}"
                )
            }
            return function.parameters.map { it as EligibilityFunction }
        }

        private fun toStringList(param: Any): List<String> {
            return (param as String).split(MANY_STRING_SEPARATOR.toRegex())
                .dropLastWhile(String::isEmpty)
                .map { str -> str.trim { it <= ' ' } }
        }

        private fun assertParamConfig(function: EligibilityFunction, requestedInput: FunctionInput, expectedCount: Int) {
            assertParamType(function, requestedInput)
            assertParamCount(function, expectedCount)
        }

        private fun assertParamType(function: EligibilityFunction, requestedInput: FunctionInput) {
            if (requestedInput != function.rule.input) {
                throw IllegalStateException("Incorrect type of inputs requested for '${function.rule}': $requestedInput")
            }
        }

        private fun assertParamCount(function: EligibilityFunction, expectedCount: Int) {
            if (function.parameters.size != expectedCount) {
                throw IllegalArgumentException(
                    "Invalid number of inputs passed to '${function.rule}': ${function.parameters.size}"
                )
            }
        }
    }
}
