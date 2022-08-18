package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.doid.config.AdenoSquamousMapping;
import com.hartwig.actin.doid.config.DoidManualConfig;
import com.hartwig.actin.doid.config.ImmutableAdenoSquamousMapping;
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class PrimaryTumorLocationBelongsToDoidTest {

    @Test
    public void canEvaluateNonExclusiveNonExact() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100", false, false);

        assertEvaluation(EvaluationResult.PASS, function100.evaluate(withConcreteTumorType("100")));
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(withConcreteTumorType("200")));
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(withConcreteTumorType("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(withConcreteTumorType("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(TumorTestFactory.withDoids((Set<String>) null)));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200", false, false);

        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("100")));
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(withConcreteTumorType("200")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(TumorTestFactory.withDoids(Sets.newHashSet())));
    }

    @Test
    public void canEvaluateExclusiveNonExact() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100", true, false);

        assertEvaluation(EvaluationResult.PASS, function100.evaluate(withConcreteTumorType("100")));
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(withConcreteTumorType("200")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(withConcreteTumorType("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(withConcreteTumorType("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(TumorTestFactory.withDoids((Set<String>) null)));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200", true, false);

        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("100")));
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(withConcreteTumorType("200")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(TumorTestFactory.withDoids(Sets.newHashSet())));
    }

    @Test
    public void canEvaluateNonExclusiveExact() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100", false, true);

        assertEvaluation(EvaluationResult.PASS, function100.evaluate(withConcreteTumorType("100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(withConcreteTumorType("200")));
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(withConcreteTumorType("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(withConcreteTumorType("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(withConcreteTumorType((Set<String>) null)));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200", false, true);

        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("100")));
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(withConcreteTumorType("200")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(withConcreteTumorType(Sets.newHashSet())));
    }

    @Test
    public void canEvaluateExclusiveExact() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100", true, true);

        assertEvaluation(EvaluationResult.PASS, function100.evaluate(withConcreteTumorType("100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(withConcreteTumorType("200")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(withConcreteTumorType("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(withConcreteTumorType("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(withConcreteTumorType((Set<String>) null)));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200", true, true);

        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("100")));
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(withConcreteTumorType("200")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(withConcreteTumorType("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(withConcreteTumorType(Sets.newHashSet())));
    }

    @Test
    public void canResolveToMainCancerType() {
        String stomachCancer = "10534";
        String stomachAdenocarcinoma = "5517";
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentMainCancerTypeChild(stomachCancer, stomachAdenocarcinoma);

        PrimaryTumorLocationBelongsToDoid function = new PrimaryTumorLocationBelongsToDoid(doidModel, stomachAdenocarcinoma, true, true);
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withTumorTypeAndDoids("concrete cancer", "concrete subtype", stomachCancer)));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorTypeAndDoids("carcinoma", Strings.EMPTY, stomachCancer)));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorTypeAndDoids(Strings.EMPTY, Strings.EMPTY, stomachCancer)));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorTypeAndDoids(null, null, stomachCancer)));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorTypeAndDoids(null, null, stomachAdenocarcinoma)));
    }

    @Test
    public void canResolveToAdenoSquamousType() {
        AdenoSquamousMapping mapping =
                ImmutableAdenoSquamousMapping.builder().adenoSquamousDoid("1").squamousDoid("2").adenoDoid("3").build();
        DoidManualConfig config = TestDoidManualConfigFactory.createWithOneAdenoSquamousMapping(mapping);
        DoidModel doidModel = TestDoidModelFactory.createWithDoidManualConfig(config);

        PrimaryTumorLocationBelongsToDoid function = new PrimaryTumorLocationBelongsToDoid(doidModel, "2", true, true);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("4")));
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoids("1")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids("2")));
    }

    @NotNull
    private static PatientRecord withConcreteTumorType(@NotNull String... doids) {
        return TumorTestFactory.withTumorTypeAndDoids("concrete type", "concrete sub type", doids);
    }

    @NotNull
    private static PatientRecord withConcreteTumorType(@Nullable Set<String> doids) {
        return TumorTestFactory.withTumorTypeAndDoids("concrete type", "concrete sub type", doids);
    }
}