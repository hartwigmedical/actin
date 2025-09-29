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
    val inputTranslator = InputTranslator()
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
            ApiRule(
                EligibilityRule.AND.name,
                listOf(EligibilityRuleParameter(countFloor = 2, countCeiling = Integer.MAX_VALUE, EligibilityRuleParameter.Type.RULE))
            ),
            ApiRule(
                EligibilityRule.OR.name,
                listOf(EligibilityRuleParameter(countFloor = 2, countCeiling = Integer.MAX_VALUE, EligibilityRuleParameter.Type.RULE))
            ),
            ApiRule(
                EligibilityRule.NOT.name,
                listOf(EligibilityRuleParameter(countFloor = 1, countCeiling = 1, EligibilityRuleParameter.Type.RULE))
            ),
            ApiRule(
                EligibilityRule.WARN_IF.name,
                listOf(EligibilityRuleParameter(countFloor = 1, countCeiling = 1, EligibilityRuleParameter.Type.RULE))
            )
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

class InputTranslator {
    private val mappings = mutableMapOf<FunctionInput, List<EligibilityRuleParameter>>()

    init {
        val albiGradeValues = EligibilityRuleParameterValueConstraint(allowedValues = listOf("a", "l", "b", "i"))
        val bodyLocationValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val codonValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val cypValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val doidTermValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val drugValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val geneValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val haplotypeValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val hlaAlleleValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val hlaGroupValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val icdTitleValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val intentValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val medicationCategoryValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val nyhaValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val proteinImpactValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val proteinValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val receptorTypeValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val specificDrugValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val specificTreatmentValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val systemicTreatmentValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val tnmtValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val transporterValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val treatmentCategoryOrTypeValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val treatmentCategoryValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val treatmentResponseValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val treatmentTypesValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val treatmentTypeValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val tumorStagesValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val tumorTypeValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val typeValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())
        val variantTypeValues = EligibilityRuleParameterValueConstraint(allowedValues = emptyList())

        mappings[FunctionInput.NONE] = listOf(param(countFloor = 0, countCeiling = 0, Type.NONE))
        mappings[FunctionInput.ONE_INTEGER] = listOf(one(Type.INTEGER))
        mappings[FunctionInput.TWO_INTEGERS] = listOf(two(Type.INTEGER))
        mappings[FunctionInput.MANY_INTEGERS] = listOf(many(Type.INTEGER))
        mappings[FunctionInput.ONE_DOUBLE] = listOf(one(Type.DOUBLE))
        mappings[FunctionInput.TWO_DOUBLES] = listOf(two(Type.DOUBLE))
        mappings[FunctionInput.ONE_ALBI_GRADE] = listOf(one(Type.STRING, albiGradeValues))
        mappings[FunctionInput.ONE_SYSTEMIC_TREATMENT] = listOf(one(Type.STRING, systemicTreatmentValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE] = listOf(one(Type.STRING, treatmentCategoryOrTypeValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER] =
            listOf(one(Type.STRING, treatmentCategoryOrTypeValues), one(Type.INTEGER))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES] =
            listOf(one(Type.STRING, treatmentCategoryValues), many(Type.STRING, treatmentTypesValues))
        mappings[FunctionInput.TWO_TREATMENT_CATEGORIES_MANY_TYPES] =
            listOf(two(Type.STRING, treatmentCategoryValues), many(Type.STRING, typeValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER] =
            listOf(one(Type.STRING, treatmentCategoryValues), many(Type.STRING, treatmentTypesValues), one(Type.INTEGER))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS] =
            listOf(one(Type.STRING, treatmentCategoryValues), many(Type.STRING, intentValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS_ONE_INTEGER] =
            listOf(one(Type.STRING, treatmentCategoryValues), many(Type.STRING, intentValues), one(Type.INTEGER))
        mappings[FunctionInput.ONE_TREATMENT_TYPE_ONE_INTEGER] = listOf(one(Type.STRING, treatmentTypeValues), one(Type.INTEGER))
        mappings[FunctionInput.MANY_TREATMENT_CATEGORIES] = listOf(many(Type.STRING, treatmentCategoryValues))
        mappings[FunctionInput.ONE_SPECIFIC_TREATMENT] = listOf(one(Type.STRING, specificTreatmentValues))
        mappings[FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER] = listOf(one(Type.STRING, specificTreatmentValues), one(Type.INTEGER))

        mappings[FunctionInput.MANY_SPECIFIC_TREATMENTS] = listOf(many(Type.STRING, specificTreatmentValues))
        mappings[FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS] =
            listOf(many(Type.STRING, specificTreatmentValues), two(Type.INTEGER))
        mappings[FunctionInput.ONE_SPECIFIC_DRUG_ONE_TREATMENT_CATEGORY_MANY_TYPES] =
            listOf(one(Type.STRING, specificDrugValues), one(Type.STRING, treatmentCategoryValues), many(Type.STRING, typeValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS] =
            listOf(one(Type.STRING, treatmentCategoryValues), many(Type.STRING, drugValues))
        mappings[FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS] = listOf(
            one(Type.STRING, treatmentCategoryValues),
            many(Type.STRING, treatmentTypesValues),
            many(Type.STRING, drugValues)
        )
        mappings[FunctionInput.MANY_DRUGS] = listOf(many(Type.STRING, drugValues))
        mappings[FunctionInput.MANY_DRUGS_ONE_INTEGER] = listOf(many(Type.STRING, drugValues), one(Type.INTEGER))
        mappings[FunctionInput.MANY_DRUGS_TWO_INTEGERS] = listOf(many(Type.STRING, drugValues), two(Type.INTEGER))
        mappings[FunctionInput.ONE_ICD_TITLE] = listOf(one(Type.STRING, icdTitleValues))
        mappings[FunctionInput.MANY_ICD_TITLES] = listOf(many(Type.STRING, icdTitleValues))
        mappings[FunctionInput.ONE_NYHA_CLASS] = listOf(one(Type.STRING, nyhaValues))
        mappings[FunctionInput.ONE_TUMOR_TYPE] = listOf(one(Type.STRING, tumorTypeValues))
        mappings[FunctionInput.ONE_STRING] = listOf(one(Type.STRING))
        mappings[FunctionInput.TWO_STRINGS] = listOf(two(Type.STRING))
        mappings[FunctionInput.MANY_STRINGS] = listOf(many(Type.STRING))
        mappings[FunctionInput.ONE_STRING_ONE_INTEGER] = listOf(one(Type.STRING), one(Type.INTEGER))
        mappings[FunctionInput.MANY_TNM_T] = listOf(many(Type.STRING, tnmtValues))
        mappings[FunctionInput.MANY_BODY_LOCATIONS] = listOf(many(Type.STRING, bodyLocationValues))
        mappings[FunctionInput.ONE_INTEGER_ONE_BODY_LOCATION] = listOf(one(Type.INTEGER), one(Type.STRING, bodyLocationValues))
        mappings[FunctionInput.ONE_INTEGER_MANY_DOID_TERMS] = listOf(one(Type.INTEGER), many(Type.STRING, doidTermValues))
        mappings[FunctionInput.ONE_INTEGER_MANY_ICD_TITLES] = listOf(one(Type.INTEGER), many(Type.STRING, icdTitleValues))
        mappings[FunctionInput.ONE_GENE] = listOf(one(Type.STRING, geneValues))
        mappings[FunctionInput.MANY_GENES] = listOf(many(Type.STRING, geneValues))
        mappings[FunctionInput.ONE_GENE_ONE_INTEGER] = listOf(one(Type.STRING, geneValues), one(Type.INTEGER))
        mappings[FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE] =
            listOf(one(Type.STRING, geneValues), one(Type.INTEGER), one(Type.STRING, variantTypeValues))
        mappings[FunctionInput.ONE_GENE_TWO_INTEGERS] = listOf(one(Type.STRING, geneValues), two(Type.INTEGER))
        mappings[FunctionInput.ONE_GENE_MANY_CODONS] = listOf(one(Type.STRING, geneValues), many(Type.STRING, codonValues))
        mappings[FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS] = listOf(one(Type.STRING, geneValues), many(Type.STRING, proteinImpactValues))
        mappings[FunctionInput.MANY_HLA_ALLELES] = listOf(many(Type.STRING, hlaAlleleValues))
        mappings[FunctionInput.ONE_HLA_GROUP] = listOf(one(Type.STRING, hlaGroupValues))
        mappings[FunctionInput.ONE_DOID_TERM] = listOf(one(Type.STRING, doidTermValues))
        mappings[FunctionInput.MANY_DOID_TERMS] = listOf(many(Type.STRING, doidTermValues))
        mappings[FunctionInput.ONE_ICD_TITLE_ONE_INTEGER] = listOf(one(Type.STRING, icdTitleValues), one(Type.INTEGER))
        mappings[FunctionInput.MANY_TUMOR_STAGES] = listOf(many(Type.STRING, tumorStagesValues))
        mappings[FunctionInput.ONE_HAPLOTYPE] = listOf(one(Type.STRING, haplotypeValues))
        mappings[FunctionInput.ONE_RECEPTOR_TYPE] = listOf(one(Type.STRING, receptorTypeValues))
        mappings[FunctionInput.MANY_INTENTS] = listOf(many(Type.STRING, intentValues))
        mappings[FunctionInput.MANY_INTENTS_ONE_INTEGER] = listOf(many(Type.STRING, intentValues), one(Type.INTEGER))
        mappings[FunctionInput.ONE_MEDICATION_CATEGORY] = listOf(one(Type.STRING, medicationCategoryValues))
        mappings[FunctionInput.ONE_MEDICATION_CATEGORY_ONE_INTEGER] = listOf(one(Type.STRING, medicationCategoryValues), one(Type.INTEGER))
        mappings[FunctionInput.MANY_MEDICATION_CATEGORIES_ONE_INTEGER] =
            listOf(many(Type.STRING, medicationCategoryValues), one(Type.INTEGER))
        mappings[FunctionInput.MANY_MEDICATION_CATEGORIES_TWO_INTEGERS] =
            listOf(many(Type.STRING, medicationCategoryValues), two(Type.INTEGER))
        mappings[FunctionInput.ONE_CYP] = listOf(one(Type.STRING, cypValues))
        mappings[FunctionInput.ONE_CYP_ONE_INTEGER] = listOf(one(Type.STRING, cypValues), one(Type.INTEGER))
        mappings[FunctionInput.ONE_TRANSPORTER] = listOf(one(Type.STRING, transporterValues))
        mappings[FunctionInput.ONE_PROTEIN] = listOf(one(Type.STRING, proteinValues))
        mappings[FunctionInput.ONE_PROTEIN_ONE_INTEGER] = listOf(one(Type.STRING, proteinValues), one(Type.INTEGER))
        mappings[FunctionInput.ONE_PROTEIN_ONE_STRING] = listOf(one(Type.STRING, proteinValues), one(Type.STRING))
        mappings[FunctionInput.ONE_TREATMENT_RESPONSE_ONE_TREATMENT_CATEGORY_MANY_TYPES] = listOf(
            one(Type.STRING, treatmentResponseValues),
            one(Type.STRING, treatmentCategoryValues), many(Type.STRING, typeValues)
        )

        val unmapped = FunctionInput.entries - mappings.keys
        if (unmapped.isNotEmpty()) {
            throw IllegalStateException("Missing entries for inputs:\n${unmapped.joinToString("\n") { "  ${it.name}" }}")
        }
    }

    fun one(type: Type, allowedValues: EligibilityRuleParameterValueConstraint? = null): EligibilityRuleParameter {
        return param(1, 1, type, allowedValues)
    }

    fun two(type: Type, allowedValues: EligibilityRuleParameterValueConstraint? = null): EligibilityRuleParameter {
        return param(2, 2, type, allowedValues)
    }

    fun many(
        type: Type,
        allowedValues: EligibilityRuleParameterValueConstraint? = null
    ): EligibilityRuleParameter {
        return param(2, Integer.MAX_VALUE, type, allowedValues)
    }

    fun param(
        countFloor: Int,
        countCeiling: Int,
        type: Type,
        allowedValues: EligibilityRuleParameterValueConstraint? = null
    ): EligibilityRuleParameter {
        return EligibilityRuleParameter(
            countFloor = countFloor,
            countCeiling = countCeiling,
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