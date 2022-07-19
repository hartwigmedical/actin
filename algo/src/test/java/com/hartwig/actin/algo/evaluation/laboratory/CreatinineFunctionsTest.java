package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class CreatinineFunctionsTest {

    private static final double EPSILON = 1.0E-2;

    @Test
    public void canCalcMDRD() {
        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        List<Double> maleValues = CreatinineFunctions.calcMDRD(1971, 2021, Gender.MALE, creatinine);
        assertEquals(103.54, maleValues.get(0), EPSILON);
        assertEquals(125.49, maleValues.get(1), EPSILON);

        List<Double> femaleValues = CreatinineFunctions.calcMDRD(1971, 2021, Gender.FEMALE, creatinine);
        assertEquals(76.83, femaleValues.get(0), EPSILON);
        assertEquals(93.11, femaleValues.get(1), EPSILON);
    }

    @Test
    public void canCalcCDKEPI() {
        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        List<Double> maleValues = CreatinineFunctions.calcCKDEPI(1971, 2021, Gender.MALE, creatinine);
        assertEquals(104.62, maleValues.get(0), EPSILON);
        assertEquals(121.25, maleValues.get(1), EPSILON);

        List<Double> femaleValues = CreatinineFunctions.calcCKDEPI(1971, 2021, Gender.FEMALE, creatinine);
        assertEquals(87.07, femaleValues.get(0), EPSILON);
        assertEquals(100.91, femaleValues.get(1), EPSILON);
    }

    @Test
    public void canEvaluateEGFREvaluations() {
        assertEquals(EvaluationResult.FAIL, CreatinineFunctions.interpretEGFREvaluations(Sets.newHashSet(EvaluationResult.FAIL)));
        assertEquals(EvaluationResult.UNDETERMINED,
                CreatinineFunctions.interpretEGFREvaluations(Sets.newHashSet(EvaluationResult.FAIL, EvaluationResult.PASS)));
        assertEquals(EvaluationResult.UNDETERMINED,
                CreatinineFunctions.interpretEGFREvaluations(Sets.newHashSet(EvaluationResult.UNDETERMINED, EvaluationResult.PASS)));
        assertEquals(EvaluationResult.PASS,
                CreatinineFunctions.interpretEGFREvaluations(Sets.newHashSet(EvaluationResult.PASS)));
    }

    @Test
    public void canCalcCockcroftGault() {
        LabValue creatinine = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70D).build();

        assertEquals(95.24, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, 60D, creatinine), EPSILON);
        assertEquals(80.95, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, 60D, creatinine), EPSILON);

        assertEquals(111.11, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, 70D, creatinine), EPSILON);
        assertEquals(94.44, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, 70D, creatinine), EPSILON);

        assertEquals(103.17, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, null, creatinine), EPSILON);
        assertEquals(67.46, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, null, creatinine), EPSILON);
    }

    @Test
    public void canDetermineWeight() {
        List<BodyWeight> weights = Lists.newArrayList();
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2020, 1, 1)).value(50D).unit(Strings.EMPTY).build());
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 1, 1)).value(60D).unit(Strings.EMPTY).build());

        assertEquals(60D, CreatinineFunctions.determineWeight(weights), EPSILON);

        assertNull(CreatinineFunctions.determineWeight(Lists.newArrayList()));
    }
}