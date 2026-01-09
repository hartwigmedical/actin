package com.hartwig.actin.trial.input

import com.hartwig.actin.datamodel.trial.Parameter


object FunctionInput {
    val NONE = emptyList<Parameter.Type>()
    val ONE_INTEGER = listOf(Parameter.Type.INTEGER)
    val TWO_INTEGERS = listOf(Parameter.Type.INTEGER, Parameter.Type.INTEGER)
    val MANY_INTEGERS = listOf(Parameter.Type.MANY_INTEGERS)
    val ONE_DOUBLE = listOf(Parameter.Type.DOUBLE)
    val TWO_DOUBLES = listOf(Parameter.Type.DOUBLE, Parameter.Type.DOUBLE)
    val ONE_ALBI_GRADE = listOf(Parameter.Type.ALBI_GRADE)
    val ONE_SYSTEMIC_TREATMENT = listOf(Parameter.Type.SYSTEMIC_TREATMENT)
    val ONE_TREATMENT_CATEGORY_OR_TYPE = listOf(Parameter.Type.TREATMENT_CATEGORY_OR_TYPE)
    val ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER = listOf(Parameter.Type.TREATMENT_CATEGORY_OR_TYPE, Parameter.Type.INTEGER)
    val ONE_TREATMENT_CATEGORY_MANY_TYPES = listOf(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
    val TWO_TREATMENT_CATEGORIES_MANY_TYPES = listOf(
        Parameter.Type.TREATMENT_CATEGORY,
        Parameter.Type.MANY_TREATMENT_TYPES,
        Parameter.Type.TREATMENT_CATEGORY,
        Parameter.Type.MANY_TREATMENT_TYPES
    )
    val ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER =
        listOf(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES, Parameter.Type.INTEGER)
    val ONE_TREATMENT_CATEGORY_MANY_INTENTS = listOf(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_INTENTS)
    val ONE_TREATMENT_CATEGORY_MANY_INTENTS_ONE_INTEGER =
        listOf(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_INTENTS, Parameter.Type.INTEGER)
    val ONE_TREATMENT_TYPE_ONE_INTEGER = listOf(Parameter.Type.TREATMENT_TYPE, Parameter.Type.INTEGER)
    val MANY_TREATMENT_CATEGORIES = listOf(Parameter.Type.MANY_TREATMENT_CATEGORIES)
    val ONE_SPECIFIC_TREATMENT = listOf(Parameter.Type.TREATMENT)
    val ONE_SPECIFIC_TREATMENT_ONE_INTEGER = listOf(Parameter.Type.TREATMENT, Parameter.Type.INTEGER)
    val MANY_SPECIFIC_TREATMENTS = listOf(Parameter.Type.MANY_TREATMENTS)
    val MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS =
        listOf(Parameter.Type.MANY_TREATMENTS, Parameter.Type.INTEGER, Parameter.Type.INTEGER)
    val ONE_SPECIFIC_DRUG_ONE_TREATMENT_CATEGORY_MANY_TYPES =
        listOf(Parameter.Type.DRUG, Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
    val ONE_TREATMENT_CATEGORY_MANY_DRUGS = listOf(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_DRUGS)
    val ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS =
        listOf(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES, Parameter.Type.MANY_DRUGS)
    val MANY_DRUGS = listOf(Parameter.Type.MANY_DRUGS)
    val MANY_DRUGS_ONE_INTEGER = listOf(Parameter.Type.MANY_DRUGS, Parameter.Type.INTEGER)
    val MANY_DRUGS_TWO_INTEGERS = listOf(Parameter.Type.MANY_DRUGS, Parameter.Type.INTEGER, Parameter.Type.INTEGER)
    val MANY_DRUG_INTERACTION_TYPES = listOf(Parameter.Type.MANY_DRUG_INTERACTION_TYPES)
    val ONE_ICD_TITLE = listOf(Parameter.Type.ICD_TITLE)
    val MANY_ICD_TITLES = listOf(Parameter.Type.MANY_ICD_TITLES)
    val ONE_NYHA_CLASS = listOf(Parameter.Type.NYHA_CLASS)
    val ONE_TUMOR_TYPE = listOf(Parameter.Type.TUMOR_TYPE)
    val ONE_STRING = listOf(Parameter.Type.STRING)
    val MANY_TNM_T = listOf(Parameter.Type.MANY_TNM_T)
    val TWO_STRINGS = listOf(Parameter.Type.STRING, Parameter.Type.STRING)
    val ONE_STRING_ONE_INTEGER = listOf(Parameter.Type.STRING, Parameter.Type.INTEGER)
    val MANY_STRINGS = listOf(Parameter.Type.MANY_STRINGS)
    val MANY_BODY_LOCATIONS = listOf(Parameter.Type.MANY_BODY_LOCATIONS)
    val ONE_INTEGER_ONE_BODY_LOCATION = listOf(Parameter.Type.INTEGER, Parameter.Type.BODY_LOCATION)
    val ONE_INTEGER_MANY_DOID_TERMS = listOf(Parameter.Type.INTEGER, Parameter.Type.MANY_DOID_TERMS)
    val ONE_INTEGER_MANY_ICD_TITLES = listOf(Parameter.Type.INTEGER, Parameter.Type.MANY_ICD_TITLES)
    val ONE_GENE = listOf(Parameter.Type.GENE)
    val ONE_GENE_ONE_INTEGER = listOf(Parameter.Type.GENE, Parameter.Type.INTEGER)
    val ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE =
        listOf(Parameter.Type.GENE, Parameter.Type.INTEGER, Parameter.Type.VARIANT_TYPE)
    val ONE_GENE_TWO_INTEGERS = listOf(Parameter.Type.GENE, Parameter.Type.INTEGER, Parameter.Type.INTEGER)
    val ONE_GENE_MANY_CODONS = listOf(Parameter.Type.GENE, Parameter.Type.MANY_CODONS)
    val ONE_GENE_MANY_PROTEIN_IMPACTS = listOf(Parameter.Type.GENE, Parameter.Type.MANY_PROTEIN_IMPACTS)
    val MANY_GENES = listOf(Parameter.Type.MANY_GENES)
    val MANY_HLA_ALLELES = listOf(Parameter.Type.MANY_HLA_ALLELES)
    val ONE_HLA_GROUP = listOf(Parameter.Type.HLA_GROUP)
    val ONE_DOID_TERM = listOf(Parameter.Type.DOID_TERM)
    val ONE_ICD_TITLE_ONE_INTEGER = listOf(Parameter.Type.ICD_TITLE, Parameter.Type.INTEGER)
    val MANY_DOID_TERMS = listOf(Parameter.Type.MANY_DOID_TERMS)
    val MANY_TUMOR_STAGES = listOf(Parameter.Type.MANY_TUMOR_STAGES)
    val ONE_HAPLOTYPE = listOf(Parameter.Type.HAPLOTYPE)
    val ONE_RECEPTOR_TYPE = listOf(Parameter.Type.RECEPTOR_TYPE)
    val MANY_INTENTS = listOf(Parameter.Type.MANY_INTENTS)
    val MANY_INTENTS_ONE_INTEGER = listOf(Parameter.Type.MANY_INTENTS, Parameter.Type.INTEGER)
    val ONE_MEDICATION_CATEGORY = listOf(Parameter.Type.MEDICATION_CATEGORY)
    val ONE_MEDICATION_CATEGORY_ONE_INTEGER = listOf(Parameter.Type.MEDICATION_CATEGORY, Parameter.Type.INTEGER)
    val MANY_MEDICATION_CATEGORIES_ONE_INTEGER = listOf(Parameter.Type.MANY_MEDICATION_CATEGORIES, Parameter.Type.INTEGER)
    val MANY_MEDICATION_CATEGORIES_TWO_INTEGERS =
        listOf(Parameter.Type.MANY_MEDICATION_CATEGORIES, Parameter.Type.INTEGER, Parameter.Type.INTEGER)
    val ONE_CYP = listOf(Parameter.Type.CYP)
    val ONE_CYP_ONE_INTEGER = listOf(Parameter.Type.CYP, Parameter.Type.INTEGER)
    val ONE_TRANSPORTER = listOf(Parameter.Type.TRANSPORTER)
    val ONE_PROTEIN = listOf(Parameter.Type.PROTEIN)
    val ONE_PROTEIN_ONE_INTEGER = listOf(Parameter.Type.PROTEIN, Parameter.Type.INTEGER)
    val ONE_PROTEIN_ONE_STRING = listOf(Parameter.Type.PROTEIN, Parameter.Type.STRING)
    val ONE_TREATMENT_RESPONSE_ONE_TREATMENT_CATEGORY_MANY_TYPES =
        listOf(Parameter.Type.TREATMENT_RESPONSE, Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
}
