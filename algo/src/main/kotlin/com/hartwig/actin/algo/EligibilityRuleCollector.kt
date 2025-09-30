package com.hartwig.actin.algo

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.FunctionInput
import com.hartwig.actin.trial.client.TrialApiClientFactory
import com.hartwig.actin.trial.client.apis.ActinEligibilityRulesApi
import com.hartwig.actin.trial.client.models.EligibilityRuleParameter
import com.hartwig.actin.trial.client.models.EligibilityRuleParameter.Type
import com.hartwig.actin.trial.client.models.EligibilityRuleParameterValueConstraint
import com.hartwig.actin.trial.client.models.EligibilityRulesetCreateRequest
import com.hartwig.actin.trial.input.composite.CompositeRules
import com.hartwig.actin.trial.client.models.EligibilityRule as ApiRule

class EligibilityRuleCollector(private val eligibilityApi: ActinEligibilityRulesApi) {
    val inputTranslator = InputTranslator(eligibilityApi)
    fun publish() {
        val version = EligibilityRuleCollector::class.java.getPackage().implementationVersion ?: "local-SNAPSHOT"
        val rules = collectRules()
        eligibilityApi.createEligibilityRuleset(EligibilityRulesetCreateRequest(version, rules))
    }

    private fun collectRules(): List<ApiRule> {
        val allRules = compositeRules() + EligibilityRule.entries.filter { !CompositeRules.isComposite(it) }.map {
            ApiRule(
                name = it.name,
                parameters = inputTranslator.translate(it.input!!)
            )
        }
        val mappedNames = allRules.map { it.name }
        val unmapped =
            EligibilityRule.entries.map { it.name } - mappedNames
        if (unmapped.isNotEmpty()) {
            throw IllegalStateException("Some rules unmapped: ${unmapped.joinToString("\n") { "  $it" }}")
        }
        return allRules
    }

    private fun compositeRules(): List<ApiRule> {
        val allComposites = EligibilityRule.entries.filter { CompositeRules.isComposite(it) }
        val mapped = listOf(
            ApiRule(EligibilityRule.AND.name, listOf(EligibilityRuleParameter(Type.MULTINOMIAL_RULE))),
            ApiRule(EligibilityRule.OR.name, listOf(EligibilityRuleParameter(Type.MULTINOMIAL_RULE))),
            ApiRule(EligibilityRule.NOT.name, listOf(EligibilityRuleParameter(Type.UNARY_RULE))),
            ApiRule(EligibilityRule.WARN_IF.name, listOf(EligibilityRuleParameter(Type.UNARY_RULE)))
        )
        val mappedNames = mapped.map { it.name }
        val unmapped = allComposites - allComposites.filter { it.name in mappedNames }
        if (unmapped.isNotEmpty()) {
            throw IllegalStateException("Some composite rules unmapped: ${unmapped.joinToString("\n") { "  ${it.name}" }}")
        }
        return mapped
    }

    companion object
}

class InputTranslator(private val api: ActinEligibilityRulesApi) {
    private val mappings = mutableMapOf<FunctionInput, List<EligibilityRuleParameter>>()


    init {
        val albiGradeValues = save("albiGrade", listOf("a", "l", "b", "i"))
        val bodyLocationValues = save(allowedValues = emptyList(), name = "bodyLocation")
        val codonValues = save(allowedValues = emptyList(), name = "codon")
        val cypValues = save(allowedValues = emptyList(), name = "cyp")
        val doidTermValues = save(allowedValues = emptyList(), name = "doidTerm")
        val drugValues = save(allowedValues = emptyList(), name = "drug")
        val geneValues = save(allowedValues = emptyList(), name = "gene")
        val haplotypeValues = save(allowedValues = emptyList(), name = "haplotype")
        val hlaAlleleValues = save(allowedValues = emptyList(), name = "hlaAllele")
        val hlaGroupValues = save(allowedValues = emptyList(), name = "hlaGroup")
        val icdTitleValues = save(allowedValues = emptyList(), name = "icdTitle")
        val intentValues = save(allowedValues = emptyList(), name = "intent")
        val medicationCategoryValues = save(allowedValues = emptyList(), name = "medicationCategory")
        val nyhaValues = save(allowedValues = emptyList(), name = "nyha")
        val proteinImpactValues = save(allowedValues = emptyList(), name = "proteinImpact")
        val proteinValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList(), name = "protein")
        val receptorTypeValues = save(allowedValues = emptyList(), name = "receptorType")
        val specificDrugValues = save(allowedValues = emptyList(), name = "specificDrug")
        val specificTreatmentValues = save(allowedValues = emptyList(), name = "specificTreatment")
        val systemicTreatmentValues = save(allowedValues = emptyList(), name = "systemicTreatment")
        val tnmtValues = save(allowedValues = emptyList(), name = "tnmt")
        val transporterValues = save(allowedValues = emptyList(), name = "transporter")
        val treatmentCategoryOrTypeValues =
            save(allowedValues = emptyList(), name = "treatmentCategoryOrType")
        val treatmentCategoryValues = save(allowedValues = emptyList(), name = "treatmentCategory")
        val treatmentResponseValues = save(allowedValues = emptyList(), name = "treatmentResponse")
        val treatmentTypesValues = save(allowedValues = emptyList(), name = "treatmentTypes")
        val treatmentTypeValues = save(allowedValues = emptyList(), name = "treatmentType")
        val tumorStagesValues = save(allowedValues = emptyList(), name = "tumorStages")
        val tumorTypeValues = save(allowedValues = emptyList(), name = "tumorType")
        val typeValues = save(allowedValues = emptyList(), name = "type")
        val variantTypeValues = save(allowedValues = emptyList(), name = "variantType")

        mappings[FunctionInput.NONE] = emptyList()
        mappings[FunctionInput.ONE_INTEGER] = listOf(param(Type.INTEGER))
        mappings[FunctionInput.TWO_INTEGERS] = listOf(param(Type.INTEGER), param(Type.INTEGER))
        mappings[FunctionInput.MANY_INTEGERS] = listOf(param(Type.MULTI_INTEGER))
        mappings[FunctionInput.ONE_DOUBLE] = listOf(param(Type.DOUBLE))
        mappings[FunctionInput.TWO_DOUBLES] = listOf(param(Type.DOUBLE), param(Type.DOUBLE))
        mappings[FunctionInput.ONE_ALBI_GRADE] = listOf(manyStringsFrom(albiGradeValues))
        mappings[FunctionInput.ONE_SYSTEMIC_TREATMENT] = listOf(manyStringsFrom(systemicTreatmentValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE] = listOf(manyStringsFrom(treatmentCategoryOrTypeValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER] =
            listOf(manyStringsFrom(treatmentCategoryOrTypeValues), param(Type.INTEGER))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES] =
            listOf(manyStringsFrom(treatmentCategoryValues), manyStringsFrom(treatmentTypesValues))
        mappings[FunctionInput.TWO_TREATMENT_CATEGORIES_MANY_TYPES] =
            listOf(
                manyStringsFrom(treatmentCategoryValues), manyStringsFrom(typeValues),
                manyStringsFrom(treatmentCategoryValues), manyStringsFrom(typeValues)
            )
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER] =
            listOf(manyStringsFrom(treatmentCategoryValues), manyStringsFrom(treatmentTypesValues), param(Type.INTEGER))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS] =
            listOf(manyStringsFrom(treatmentCategoryValues), manyStringsFrom(intentValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS_ONE_INTEGER] =
            listOf(manyStringsFrom(treatmentCategoryValues), manyStringsFrom(intentValues), param(Type.INTEGER))
        mappings[FunctionInput.ONE_TREATMENT_TYPE_ONE_INTEGER] = listOf(manyStringsFrom(treatmentTypeValues), param(Type.INTEGER))
        mappings[FunctionInput.MANY_TREATMENT_CATEGORIES] = listOf(manyStringsFrom(treatmentCategoryValues))
        mappings[FunctionInput.ONE_SPECIFIC_TREATMENT] = listOf(manyStringsFrom(specificTreatmentValues))
        mappings[FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER] =
            listOf(manyStringsFrom(specificTreatmentValues), param(Type.INTEGER))

        mappings[FunctionInput.MANY_SPECIFIC_TREATMENTS] = listOf(manyStringsFrom(specificTreatmentValues))
        mappings[FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS] =
            listOf(manyStringsFrom(specificTreatmentValues), param(Type.INTEGER), param(Type.INTEGER))
        mappings[FunctionInput.ONE_SPECIFIC_DRUG_ONE_TREATMENT_CATEGORY_MANY_TYPES] =
            listOf(manyStringsFrom(specificDrugValues), manyStringsFrom(treatmentCategoryValues), manyStringsFrom(typeValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS] =
            listOf(manyStringsFrom(treatmentCategoryValues), manyStringsFrom(drugValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS] = listOf(
            manyStringsFrom(treatmentCategoryValues),
            manyStringsFrom(treatmentTypesValues),
            manyStringsFrom(drugValues)
        )
        mappings[FunctionInput.MANY_DRUGS] = listOf(manyStringsFrom(drugValues))
        mappings[FunctionInput.MANY_DRUGS_ONE_INTEGER] = listOf(manyStringsFrom(drugValues), param(Type.INTEGER))
        mappings[FunctionInput.MANY_DRUGS_TWO_INTEGERS] = listOf(manyStringsFrom(drugValues), param(Type.INTEGER), param(Type.INTEGER))
        mappings[FunctionInput.ONE_ICD_TITLE] = listOf(param(Type.STRING, icdTitleValues))
        mappings[FunctionInput.MANY_ICD_TITLES] = listOf(manyStringsFrom(icdTitleValues))
        mappings[FunctionInput.ONE_NYHA_CLASS] = listOf(param(Type.STRING, nyhaValues))
        mappings[FunctionInput.ONE_TUMOR_TYPE] = listOf(param(Type.STRING, tumorTypeValues))
        mappings[FunctionInput.ONE_STRING] = listOf(param(Type.STRING))
        mappings[FunctionInput.TWO_STRINGS] = listOf(param(Type.STRING), param(Type.STRING))
        mappings[FunctionInput.MANY_STRINGS] = listOf(param(Type.MULTI_STRING))
        mappings[FunctionInput.ONE_STRING_ONE_INTEGER] = listOf(param(Type.STRING), param(Type.INTEGER))
        mappings[FunctionInput.MANY_TNM_T] = listOf(manyStringsFrom(tnmtValues))
        mappings[FunctionInput.MANY_BODY_LOCATIONS] = listOf(manyStringsFrom(bodyLocationValues))
        mappings[FunctionInput.ONE_INTEGER_ONE_BODY_LOCATION] = listOf(param(Type.INTEGER), param(Type.STRING, bodyLocationValues))
        mappings[FunctionInput.ONE_INTEGER_MANY_DOID_TERMS] = listOf(param(Type.INTEGER), manyStringsFrom(doidTermValues))
        mappings[FunctionInput.ONE_INTEGER_MANY_ICD_TITLES] = listOf(param(Type.INTEGER), manyStringsFrom(icdTitleValues))
        mappings[FunctionInput.ONE_GENE] = listOf(param(Type.STRING, geneValues))
        mappings[FunctionInput.MANY_GENES] = listOf(manyStringsFrom(geneValues))
        mappings[FunctionInput.ONE_GENE_ONE_INTEGER] = listOf(param(Type.STRING, geneValues), param(Type.INTEGER))
        mappings[FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE] =
            listOf(param(Type.STRING, geneValues), param(Type.INTEGER), param(Type.STRING, variantTypeValues))
        mappings[FunctionInput.ONE_GENE_TWO_INTEGERS] = listOf(param(Type.STRING, geneValues), param(Type.INTEGER), param(Type.INTEGER))
        mappings[FunctionInput.ONE_GENE_MANY_CODONS] = listOf(param(Type.STRING, geneValues), manyStringsFrom(codonValues))
        mappings[FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS] =
            listOf(param(Type.STRING, geneValues), manyStringsFrom(proteinImpactValues))
        mappings[FunctionInput.MANY_HLA_ALLELES] = listOf(manyStringsFrom(hlaAlleleValues))
        mappings[FunctionInput.ONE_HLA_GROUP] = listOf(param(Type.STRING, hlaGroupValues))
        mappings[FunctionInput.ONE_DOID_TERM] = listOf(param(Type.STRING, doidTermValues))
        mappings[FunctionInput.MANY_DOID_TERMS] = listOf(manyStringsFrom(doidTermValues))
        mappings[FunctionInput.ONE_ICD_TITLE_ONE_INTEGER] = listOf(param(Type.STRING, icdTitleValues), param(Type.INTEGER))
        mappings[FunctionInput.MANY_TUMOR_STAGES] = listOf(manyStringsFrom(tumorStagesValues))
        mappings[FunctionInput.ONE_HAPLOTYPE] = listOf(param(Type.STRING, haplotypeValues))
        mappings[FunctionInput.ONE_RECEPTOR_TYPE] = listOf(param(Type.STRING, receptorTypeValues))
        mappings[FunctionInput.MANY_INTENTS] = listOf(manyStringsFrom(intentValues))
        mappings[FunctionInput.MANY_INTENTS_ONE_INTEGER] = listOf(manyStringsFrom(intentValues), param(Type.INTEGER))
        mappings[FunctionInput.ONE_MEDICATION_CATEGORY] = listOf(param(Type.STRING, medicationCategoryValues))
        mappings[FunctionInput.ONE_MEDICATION_CATEGORY_ONE_INTEGER] =
            listOf(param(Type.STRING, medicationCategoryValues), param(Type.INTEGER))
        mappings[FunctionInput.MANY_MEDICATION_CATEGORIES_ONE_INTEGER] =
            listOf(manyStringsFrom(medicationCategoryValues), param(Type.INTEGER))
        mappings[FunctionInput.MANY_MEDICATION_CATEGORIES_TWO_INTEGERS] =
            listOf(manyStringsFrom(medicationCategoryValues), param(Type.INTEGER), param(Type.INTEGER))
        mappings[FunctionInput.ONE_CYP] = listOf(param(Type.STRING, cypValues))
        mappings[FunctionInput.ONE_CYP_ONE_INTEGER] = listOf(param(Type.STRING, cypValues), param(Type.INTEGER))
        mappings[FunctionInput.ONE_TRANSPORTER] = listOf(param(Type.STRING, transporterValues))
        mappings[FunctionInput.ONE_PROTEIN] = listOf(param(Type.STRING, proteinValues))
        mappings[FunctionInput.ONE_PROTEIN_ONE_INTEGER] = listOf(param(Type.STRING, proteinValues), param(Type.INTEGER))
        mappings[FunctionInput.ONE_PROTEIN_ONE_STRING] = listOf(param(Type.STRING, proteinValues), param(Type.STRING))
        mappings[FunctionInput.ONE_TREATMENT_RESPONSE_ONE_TREATMENT_CATEGORY_MANY_TYPES] = listOf(
            param(Type.STRING, treatmentResponseValues),
            param(Type.STRING, treatmentCategoryValues), manyStringsFrom(typeValues)
        )

        val unmapped = FunctionInput.entries - mappings.keys
        if (unmapped.isNotEmpty()) {
            throw IllegalStateException("Missing entries for inputs:\n${unmapped.joinToString("\n") { "  ${it.name}" }}")
        }
    }

    fun save(name: String, allowedValues: List<String>): EligibilityRuleParameterValueConstraint {
        return api.createEligibilityRuleParameterValueConstraint(EligibilityRuleParameterValueConstraint(name, allowedValues))
    }

    fun manyStringsFrom(allowedValues: EligibilityRuleParameterValueConstraint): EligibilityRuleParameter {
        return param(Type.MULTI_STRING, allowedValues)
    }

    fun param(
        type: Type,
        allowedValues: EligibilityRuleParameterValueConstraint? = null
    ): EligibilityRuleParameter {
        return EligibilityRuleParameter(
            type = type,
            allowedValues = allowedValues
        )
    }

    fun translate(input: FunctionInput): List<EligibilityRuleParameter> {
        return mappings[input]!!
    }
}

fun main() {
    EligibilityRuleCollector(TrialApiClientFactory("http://localhost:8081").createEligibilityApi()).publish()
}