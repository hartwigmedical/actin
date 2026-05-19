package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import java.time.LocalDate

internal class LabMeasurementEvaluator(
    private val selector: LabValueSelector,
    private val function: LabEvaluationFunction,
    private val minValidDate: LocalDate,
    private val minPassDate: LocalDate,
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpretation = LabInterpretation.interpret(record.labValues)
        return when (val result = selector.select(interpretation, minValidDate)) {
            is LabValueSelectionResult.NotFound -> result.evaluation
            is LabValueSelectionResult.Found -> {
                val evaluation = function.evaluate(record, result.values)
                val oldestDate = result.values.values.minOf { it.date }
                val conversionNoteMessages = result.conversionNotes.map { StaticMessage(it) }.toSet()
                if (evaluation.result == EvaluationResult.PASS && !oldestDate.isAfter(minPassDate)) {
                    Evaluation(
                        result = EvaluationResult.PASS,
                        recoverable = true,
                        passMessages = evaluation.passMessages
                            .map { StaticMessage("$it but measurement occurred before ${Format.date(minPassDate)}") }
                            .toSet() + conversionNoteMessages
                    )
                } else {
                    evaluationWithConversionNotes(evaluation, conversionNoteMessages)
                }
            }
        }
    }

    private fun evaluationWithConversionNotes(evaluation: Evaluation, notes: Set<StaticMessage>): Evaluation = when (evaluation.result) {
        EvaluationResult.PASS -> evaluation.copy(passMessages = evaluation.passMessages + notes)
        EvaluationResult.FAIL -> evaluation.copy(failMessages = evaluation.failMessages + notes)
        EvaluationResult.WARN -> evaluation.copy(warnMessages = evaluation.warnMessages + notes)
        EvaluationResult.UNDETERMINED -> evaluation.copy(undeterminedMessages = evaluation.undeterminedMessages + notes)
    }
}
