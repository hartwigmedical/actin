package com.hartwig.actin.algo

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.trial.DrugParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.ManyDrugsParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentTypesParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentsParameter
import com.hartwig.actin.datamodel.trial.TreatmentCategoryParameter
import com.hartwig.actin.datamodel.trial.TreatmentParameter
import com.hartwig.actin.trial.input.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityFactoryTest {

    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val factory = EligibilityFactory(treatmentDatabase)

    @Test
    fun `Should parse composite criterion with nested treatment criteria`() {
        val criterion = "AND(IS_MALE, OR(HAS_HAD_TREATMENT_NAME_X[${TestTreatmentDatabaseFactory.CISPLATIN}]," +
            " HAS_HAD_TREATMENT_NAME_X[${TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN}]))"

        val cisplatin = requireNotNull(treatmentDatabase.findTreatmentByName(TestTreatmentDatabaseFactory.CISPLATIN))
        val capox = requireNotNull(
            treatmentDatabase.findTreatmentByName(TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN)
        )
        val expected = EligibilityFunction(
            EligibilityRule.AND.name,
            listOf(
                FunctionParameter(EligibilityFunction(EligibilityRule.IS_MALE.name, emptyList())),
                FunctionParameter(
                    EligibilityFunction(
                        EligibilityRule.OR.name,
                        listOf(
                            FunctionParameter(
                                EligibilityFunction(
                                    EligibilityRule.HAS_HAD_TREATMENT_NAME_X.name,
                                    listOf(TreatmentParameter(cisplatin))
                                )
                            ),
                            FunctionParameter(
                                EligibilityFunction(
                                    EligibilityRule.HAS_HAD_TREATMENT_NAME_X.name,
                                    listOf(TreatmentParameter(capox))
                                )
                            )
                        )
                    )
                )
            )
        )

        assertThat(factory.generateEligibilityFunction(criterion)).isEqualTo(expected)
    }

    @Test
    fun `Should parse many drugs parameters from treatment database`() {
        val criterion = "${EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X.name}[" +
            "${TestTreatmentDatabaseFactory.CISPLATIN};${TestTreatmentDatabaseFactory.PEMBROLIZUMAB}]"

        val cisplatin = requireNotNull(treatmentDatabase.findDrugByName(TestTreatmentDatabaseFactory.CISPLATIN))
        val pembrolizumab = requireNotNull(treatmentDatabase.findDrugByName(TestTreatmentDatabaseFactory.PEMBROLIZUMAB))
        val expected = EligibilityFunction(
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X.name,
            listOf(ManyDrugsParameter(setOf(cisplatin, pembrolizumab)))
        )

        assertThat(factory.generateEligibilityFunction(criterion)).isEqualTo(expected)
    }

    @Test
    fun `Should parse systemic treatment parameters from treatment database`() {
        val criterion = "${EligibilityRule.HAS_HAD_FIRST_LINE_SYSTEMIC_TREATMENT_NAME_X.name}[" +
            TestTreatmentDatabaseFactory.CISPLATIN + "]"

        val cisplatin = requireNotNull(treatmentDatabase.findTreatmentByName(TestTreatmentDatabaseFactory.CISPLATIN))
        val expected = EligibilityFunction(
            EligibilityRule.HAS_HAD_FIRST_LINE_SYSTEMIC_TREATMENT_NAME_X.name,
            listOf(TreatmentParameter(cisplatin))
        )

        assertThat(factory.generateEligibilityFunction(criterion)).isEqualTo(expected)
    }

    @Test
    fun `Should parse many treatments and integer parameters`() {
        val criterion = "${EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES.name}[" +
            "${TestTreatmentDatabaseFactory.CISPLATIN};${TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN}, 2, 6]"

        val cisplatin = requireNotNull(treatmentDatabase.findTreatmentByName(TestTreatmentDatabaseFactory.CISPLATIN))
        val capox = requireNotNull(
            treatmentDatabase.findTreatmentByName(TestTreatmentDatabaseFactory.CAPECITABINE_OXALIPLATIN)
        )

        val function = factory.generateEligibilityFunction(criterion)

        assertThat(function.rule)
            .isEqualTo(EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES.name)
        assertThat(function.param<ManyTreatmentsParameter>(0).value).containsExactly(cisplatin, capox)
        assertThat(function.param<IntegerParameter>(1).value).isEqualTo(2)
        assertThat(function.param<IntegerParameter>(2).value).isEqualTo(6)
    }

    @Test
    fun `Should parse drug parameter with treatment category and types`() {
        val criterion = "${EligibilityRule.HAS_HAD_DRUG_X_COMBINED_WITH_CATEGORY_Y_TREATMENT_OF_TYPES_Z.name}[" +
            "${TestTreatmentDatabaseFactory.PEMBROLIZUMAB}, ${TreatmentCategory.CHEMOTHERAPY.name}, ${DrugType.ANTI_PD_1.name}]"

        val pembrolizumab = requireNotNull(treatmentDatabase.findDrugByName(TestTreatmentDatabaseFactory.PEMBROLIZUMAB))

        val function = factory.generateEligibilityFunction(criterion)

        assertThat(function.rule)
            .isEqualTo(EligibilityRule.HAS_HAD_DRUG_X_COMBINED_WITH_CATEGORY_Y_TREATMENT_OF_TYPES_Z.name)
        assertThat(function.param<DrugParameter>(0).value).isEqualTo(pembrolizumab)
        assertThat(function.param<TreatmentCategoryParameter>(1).value).isEqualTo(TreatmentCategory.CHEMOTHERAPY)
        assertThat(function.param<ManyTreatmentTypesParameter>(2).value).containsOnly(DrugType.ANTI_PD_1)
    }
}
