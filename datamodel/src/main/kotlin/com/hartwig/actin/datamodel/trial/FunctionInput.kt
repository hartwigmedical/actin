package com.hartwig.actin.datamodel.trial

enum class FunctionInput {
    NONE,
    ONE_INTEGER,
    TWO_INTEGERS,
    MANY_INTEGERS,
    ONE_DOUBLE,
    ONE_DOUBLE_ONE_GENDER,
    TWO_DOUBLES,
    ONE_TREATMENT_CATEGORY_OR_TYPE,
    ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER,
    ONE_TREATMENT_CATEGORY_MANY_TYPES,
    TWO_TREATMENT_CATEGORIES_MANY_TYPES,
    ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER,
    ONE_TREATMENT_CATEGORY_MANY_INTENTS,
    ONE_SPECIFIC_TREATMENT,
    ONE_SPECIFIC_TREATMENT_ONE_INTEGER,
    MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS,
    ONE_SPECIFIC_DRUG_ONE_TREATMENT_CATEGORY_MANY_TYPES,
    ONE_TREATMENT_CATEGORY_MANY_DRUGS,
    ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS,
    MANY_DRUGS,
    MANY_DRUGS_ONE_INTEGER,
    MANY_DRUGS_TWO_INTEGERS,
    ONE_ICD_TITLE,
    MANY_ICD_TITLES,
    ONE_NYHA_CLASS,
    ONE_TUMOR_TYPE,
    ONE_STRING,
    TWO_STRINGS,
    ONE_STRING_ONE_INTEGER,
    MANY_STRINGS,
    MANY_STRINGS_ONE_INTEGER,
    ONE_INTEGER_ONE_STRING,
    ONE_INTEGER_MANY_DOID_TERMS,
    ONE_INTEGER_MANY_ICD_TITLES,
    ONE_GENE,
    ONE_GENE_ONE_INTEGER,
    ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE,
    ONE_GENE_TWO_INTEGERS,
    ONE_GENE_MANY_CODONS,
    ONE_GENE_MANY_PROTEIN_IMPACTS,
    MANY_GENES,
    ONE_HLA_ALLELE,
    ONE_HLA_GROUP,
    ONE_DOID_TERM,
    ONE_ICD_TITLE_ONE_INTEGER,
    MANY_DOID_TERMS,
    MANY_TUMOR_STAGES,
    ONE_HAPLOTYPE,
    ONE_RECEPTOR_TYPE,
    MANY_INTENTS,
    MANY_INTENTS_ONE_INTEGER,
    ONE_MEDICATION_CATEGORY,
    ONE_MEDICATION_CATEGORY_ONE_INTEGER,
    MANY_MEDICATION_CATEGORIES_ONE_INTEGER,
    MANY_MEDICATION_CATEGORIES_TWO_INTEGERS,
    ONE_CYP,
    ONE_CYP_ONE_INTEGER,
    ONE_TRANSPORTER,
}
