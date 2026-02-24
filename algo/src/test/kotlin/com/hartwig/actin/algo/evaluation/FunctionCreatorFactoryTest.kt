package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.evaluation.FunctionCreatorFactory.create
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.Cyp
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.datamodel.clinical.IhcTestResult
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.datamodel.clinical.TnmT
import com.hartwig.actin.datamodel.clinical.Transporter
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import com.hartwig.actin.datamodel.trial.DrugParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.ManyDrugsParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentsParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.SystemicTreatmentParameter
import com.hartwig.actin.datamodel.trial.TreatmentParameter
import com.hartwig.actin.datamodel.trial.VariantTypeInput
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.icd.TestIcdFactory
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.trial.input.composite.CompositeRules
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FunctionCreatorFactoryTest {

    @Test
    fun `Should create every function`() {
        val doidTerm = "term 1"
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("doid 1", doidTerm)
        val icdModel = TestIcdFactory.createTestModel()
        val icdTitle = icdModel.titleToCodeMap.keys.first()
        val map = create(RuleMappingResourcesTestFactory.create(doidModel = doidModel, icdModel = icdModel))
        val treatment = TreatmentTestFactory.treatment("Treatment", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val systemicTreatment = TreatmentTestFactory.treatment("Systemic treatment", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val drug = Drug("Drug", setOf(DrugType.ANTI_PD_1), TreatmentCategory.IMMUNOTHERAPY, "")
        EligibilityRule.entries.forEach { rule ->
            if (!CompositeRules.isComposite(rule)) {
                val function = EligibilityFunction(
                    rule.name,
                    rule.input.map { type -> parameterFor(type, doidTerm, icdTitle, treatment, systemicTreatment, drug) }
                )
                val creator = map[rule]
                assertThat(creator?.invoke(function)).isNotNull
            }
        }
    }

    private fun parameterFor(
        type: Parameter.Type,
        doidTerm: String,
        icdTitle: String,
        treatment: Treatment,
        systemicTreatment: Treatment,
        drug: Drug
    ): Parameter<*> {
        return when (type) {
            Parameter.Type.TREATMENT -> TreatmentParameter(treatment)
            Parameter.Type.SYSTEMIC_TREATMENT -> SystemicTreatmentParameter(systemicTreatment)
            Parameter.Type.MANY_TREATMENTS -> ManyTreatmentsParameter(listOf(treatment))
            Parameter.Type.DRUG -> DrugParameter(drug)
            Parameter.Type.MANY_DRUGS -> ManyDrugsParameter(setOf(drug))
            Parameter.Type.FUNCTION -> FunctionParameter(EligibilityFunction(EligibilityRule.IS_MALE.name, emptyList()))
            else -> type.create(stringValueFor(type, doidTerm, icdTitle))
        }
    }

    private fun stringValueFor(type: Parameter.Type, doidTerm: String, icdTitle: String): String {
        return when (type) {
            Parameter.Type.INTEGER -> "1"
            Parameter.Type.DOUBLE -> "1.0"
            Parameter.Type.STRING -> "value"
            Parameter.Type.DOID_TERM -> doidTerm
            Parameter.Type.TUMOR_TYPE -> "CARCINOMA"
            Parameter.Type.ALBI_GRADE -> "1"
            Parameter.Type.MANY_INTEGERS -> "1"
            Parameter.Type.MANY_STRINGS -> "value"
            Parameter.Type.ICD_TITLE -> icdTitle
            Parameter.Type.MANY_ICD_TITLES -> icdTitle
            Parameter.Type.NYHA_CLASS -> "I"
            Parameter.Type.TREATMENT_CATEGORY -> TreatmentCategory.CHEMOTHERAPY.name
            Parameter.Type.TREATMENT_CATEGORY_OR_TYPE -> TreatmentCategory.CHEMOTHERAPY.name
            Parameter.Type.TREATMENT_TYPE -> DrugType.ANTI_PD_1.name
            Parameter.Type.MANY_TREATMENT_TYPES -> DrugType.ANTI_PD_1.name
            Parameter.Type.MANY_TREATMENT_CATEGORIES -> TreatmentCategory.CHEMOTHERAPY.name
            Parameter.Type.MANY_DRUG_INTERACTION_TYPES -> DrugInteraction.Type.INHIBITOR.name
            Parameter.Type.MANY_TNM_T -> TnmT.T1.name
            Parameter.Type.BODY_LOCATION -> BodyLocationCategory.LUNG.name
            Parameter.Type.MANY_BODY_LOCATIONS -> BodyLocationCategory.LUNG.name
            Parameter.Type.GENE -> "BRAF"
            Parameter.Type.MANY_GENES -> "KRAS"
            Parameter.Type.VARIANT_TYPE -> VariantTypeInput.INSERT.name
            Parameter.Type.MANY_CODONS -> "12"
            Parameter.Type.MANY_PROTEIN_IMPACTS -> "V600E"
            Parameter.Type.HLA_GROUP -> "HLA-A"
            Parameter.Type.MANY_HLA_ALLELES -> "HLA-A*01:01"
            Parameter.Type.MANY_TUMOR_STAGES -> TumorStage.I.name
            Parameter.Type.HAPLOTYPE -> "HLA-A*02:01"
            Parameter.Type.RECEPTOR_TYPE -> ReceptorType.ER.name
            Parameter.Type.MANY_INTENTS -> Intent.ADJUVANT.name
            Parameter.Type.MEDICATION_CATEGORY -> "Chemotherapy"
            Parameter.Type.MANY_MEDICATION_CATEGORIES -> "Chemotherapy"
            Parameter.Type.CYP -> Cyp.CYP3A4_5.name
            Parameter.Type.TRANSPORTER -> Transporter.BCRP.name
            Parameter.Type.PROTEIN -> "TP53"
            Parameter.Type.TREATMENT_RESPONSE -> TreatmentResponse.COMPLETE_RESPONSE.name
            Parameter.Type.MANY_DOID_TERMS -> doidTerm
            Parameter.Type.IHC_TEST_RESULT -> IhcTestResult.LOW.name
            Parameter.Type.TREATMENT,
            Parameter.Type.SYSTEMIC_TREATMENT,
            Parameter.Type.MANY_TREATMENTS,
            Parameter.Type.DRUG,
            Parameter.Type.MANY_DRUGS,
            Parameter.Type.FUNCTION -> error("Type handled separately: $type")
        }
    }
}
