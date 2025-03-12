package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

class HasLimitedTotalBilirubinULNOrLimitedOtherMeasureULNDependingOnGilbertDisease(
    private val maxULNWithoutGilbertDisease: Double,
    private val labMeasureWithGilbertDisease: LabMeasurement,
    private val maxULNWithGilbertDisease: Double,
    private val minValidLabDate: LocalDate,
    private val minPassLabDate: LocalDate,
    private val icdModel: IcdModel
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (applicableMeasure, applicableULN) = if (icdModel.findInstancesMatchingAnyIcdCode(
                record.otherConditions,
                setOf(IcdCode(IcdConstants.GILBERT_SYNDROME_CODE))
            ).fullMatches.isNotEmpty()
        ) {
            labMeasureWithGilbertDisease to maxULNWithGilbertDisease
        } else {
            LabMeasurement.TOTAL_BILIRUBIN to maxULNWithoutGilbertDisease
        }

        return LabMeasurementEvaluator(applicableMeasure, HasLimitedLabValueULN(applicableULN), minValidLabDate, minPassLabDate).evaluate(
            record
        )
    }
}
