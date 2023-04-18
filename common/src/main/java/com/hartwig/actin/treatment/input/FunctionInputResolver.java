package com.hartwig.actin.treatment.input;

import static com.hartwig.actin.treatment.input.FunctionInputMapping.RULE_INPUT_MAP;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.input.composite.CompositeInput;
import com.hartwig.actin.treatment.input.composite.CompositeRules;
import com.hartwig.actin.treatment.input.datamodel.ImmutableTreatmentInputWithName;
import com.hartwig.actin.treatment.input.datamodel.TreatmentInput;
import com.hartwig.actin.treatment.input.datamodel.TreatmentInputWithName;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;
import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput;
import com.hartwig.actin.treatment.input.single.FunctionInput;
import com.hartwig.actin.treatment.input.single.ImmutableManyGenes;
import com.hartwig.actin.treatment.input.single.ImmutableManyTreatmentsWithName;
import com.hartwig.actin.treatment.input.single.ImmutableOneGene;
import com.hartwig.actin.treatment.input.single.ImmutableOneGeneManyCodons;
import com.hartwig.actin.treatment.input.single.ImmutableOneGeneManyProteinImpacts;
import com.hartwig.actin.treatment.input.single.ImmutableOneGeneOneInteger;
import com.hartwig.actin.treatment.input.single.ImmutableOneGeneOneIntegerOneVariantType;
import com.hartwig.actin.treatment.input.single.ImmutableOneGeneTwoIntegers;
import com.hartwig.actin.treatment.input.single.ImmutableOneHlaAllele;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerOneString;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentOneInteger;
import com.hartwig.actin.treatment.input.single.ImmutableOneTypedTreatmentManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneTypedTreatmentManyStringsOneInteger;
import com.hartwig.actin.treatment.input.single.ImmutableTwoDoubles;
import com.hartwig.actin.treatment.input.single.ImmutableTwoIntegers;
import com.hartwig.actin.treatment.input.single.ImmutableTwoIntegersManyStrings;
import com.hartwig.actin.treatment.input.single.ManyGenes;
import com.hartwig.actin.treatment.input.single.ManyTreatmentsWithName;
import com.hartwig.actin.treatment.input.single.OneGene;
import com.hartwig.actin.treatment.input.single.OneGeneManyCodons;
import com.hartwig.actin.treatment.input.single.OneGeneManyProteinImpacts;
import com.hartwig.actin.treatment.input.single.OneGeneOneInteger;
import com.hartwig.actin.treatment.input.single.OneGeneOneIntegerOneVariantType;
import com.hartwig.actin.treatment.input.single.OneGeneTwoIntegers;
import com.hartwig.actin.treatment.input.single.OneHlaAllele;
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneInteger;
import com.hartwig.actin.treatment.input.single.OneTypedTreatmentManyStrings;
import com.hartwig.actin.treatment.input.single.OneTypedTreatmentManyStringsOneInteger;
import com.hartwig.actin.treatment.input.single.TwoDoubles;
import com.hartwig.actin.treatment.input.single.TwoIntegers;
import com.hartwig.actin.treatment.input.single.TwoIntegersManyStrings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FunctionInputResolver {

    private static final Logger LOGGER = LogManager.getLogger(FunctionInputResolver.class);

    private static final String MANY_STRING_SEPARATOR = ";";

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final MolecularInputChecker molecularInputChecker;

    public FunctionInputResolver(@NotNull final DoidModel doidModel, @NotNull final MolecularInputChecker molecularInputChecker) {
        this.doidModel = doidModel;
        this.molecularInputChecker = molecularInputChecker;
    }

    @Nullable
    public Boolean hasValidInputs(@NotNull EligibilityFunction function) {
        if (CompositeRules.isComposite(function.rule())) {
            return hasValidCompositeInputs(function);
        } else {
            return hasValidSingleInputs(function);
        }
    }

    private static boolean hasValidCompositeInputs(@NotNull EligibilityFunction function) {
        try {
            CompositeInput requiredInputs = CompositeRules.inputsForCompositeRule(function.rule());
            if (requiredInputs == CompositeInput.AT_LEAST_2) {
                createAtLeastTwoCompositeParameters(function);
            } else if (requiredInputs == CompositeInput.EXACTLY_1) {
                createOneCompositeParameter(function);
            } else {
                throw new IllegalStateException(
                        "Could not interpret composite inputs for rule '" + function.rule() + "': " + requiredInputs);
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Nullable
    private Boolean hasValidSingleInputs(@NotNull EligibilityFunction function) {
        try {
            switch (RULE_INPUT_MAP.get(function.rule())) {
                case NONE: {
                    return function.parameters().isEmpty();
                }
                case ONE_INTEGER: {
                    createOneIntegerInput(function);
                    return true;
                }
                case TWO_INTEGERS: {
                    createTwoIntegersInput(function);
                    return true;
                }
                case ONE_DOUBLE: {
                    createOneDoubleInput(function);
                    return true;
                }
                case TWO_DOUBLES: {
                    createTwoDoublesInput(function);
                    return true;
                }
                case ONE_TREATMENT: {
                    createOneTreatmentInput(function);
                    return true;
                }
                case ONE_TREATMENT_ONE_INTEGER: {
                    createOneTreatmentOneIntegerInput(function);
                    return true;
                }
                case ONE_TYPED_TREATMENT_MANY_STRINGS: {
                    createOneTypedTreatmentManyStringsInput(function);
                    return true;
                }
                case ONE_TYPED_TREATMENT_MANY_STRINGS_ONE_INTEGER: {
                    createOneTypedTreatmentManyStringsOneIntegerInput(function);
                    return true;
                }
                case ONE_TUMOR_TYPE: {
                    createOneTumorTypeInput(function);
                    return true;
                }
                case ONE_STRING: {
                    createOneStringInput(function);
                    return true;
                }
                case ONE_STRING_ONE_INTEGER: {
                    createOneStringOneIntegerInput(function);
                    return true;
                }
                case MANY_STRINGS_ONE_INTEGER: {
                    createManyStringsOneIntegerInput(function);
                    return true;
                }
                case MANY_STRINGS_TWO_INTEGERS: {
                    createManyStringsTwoIntegersInput(function);
                    return true;
                }
                case MANY_TREATMENTS_WITH_NAME: {
                    createManyTreatmentsWithNames(function);
                    return true;
                }
                case ONE_INTEGER_ONE_STRING: {
                    createOneIntegerOneStringInput(function);
                    return true;
                }
                case ONE_INTEGER_MANY_STRINGS: {
                    createOneIntegerManyStringsInput(function);
                    return true;
                }
                case ONE_TUMOR_STAGE: {
                    createOneTumorStageInput(function);
                    return true;
                }
                case ONE_HLA_ALLELE: {
                    createOneHlaAlleleInput(function);
                    return true;
                }
                case ONE_GENE: {
                    createOneGeneInput(function);
                    return true;
                }
                case ONE_GENE_ONE_INTEGER: {
                    createOneGeneOneIntegerInput(function);
                    return true;
                }
                case ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE: {
                    createOneGeneOneIntegerOneVariantTypeInput(function);
                    return true;
                }
                case ONE_GENE_TWO_INTEGERS: {
                    createOneGeneTwoIntegersInput(function);
                    return true;
                }
                case ONE_GENE_MANY_CODONS: {
                    createOneGeneManyCodonsInput(function);
                    return true;
                }
                case ONE_GENE_MANY_PROTEIN_IMPACTS: {
                    createOneGeneManyProteinImpactsInput(function);
                    return true;
                }
                case MANY_GENES: {
                    createManyGenesInput(function);
                    return true;
                }
                case ONE_DOID_TERM: {
                    createOneDoidTermInput(function);
                    return true;
                }
                default: {
                    LOGGER.warn("Rule '{}' not defined in parameter type map!", function.rule());
                    return null;
                }
            }
        } catch (Exception exception) {
            LOGGER.warn(exception.getMessage());
            return false;
        }
    }

    public int createOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_INTEGER, 1);

        return Integer.parseInt((String) function.parameters().get(0));
    }

    @NotNull
    public TwoIntegers createTwoIntegersInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.TWO_INTEGERS, 2);

        return ImmutableTwoIntegers.builder()
                .integer1(Integer.parseInt((String) function.parameters().get(0)))
                .integer2(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    public double createOneDoubleInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_DOUBLE, 1);

        return Double.parseDouble((String) function.parameters().get(0));
    }

    @NotNull
    public TwoDoubles createTwoDoublesInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.TWO_DOUBLES, 2);

        return ImmutableTwoDoubles.builder()
                .double1(Double.parseDouble((String) function.parameters().get(0)))
                .double2(Double.parseDouble((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public TreatmentInput createOneTreatmentInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT, 1);

        return TreatmentInput.fromString((String) function.parameters().get(0));
    }

    @NotNull
    public OneTreatmentOneInteger createOneTreatmentOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_ONE_INTEGER, 2);

        return ImmutableOneTreatmentOneInteger.builder()
                .treatment(TreatmentInput.fromString((String) function.parameters().get(0)))
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public OneTypedTreatmentManyStrings createOneTypedTreatmentManyStringsInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TYPED_TREATMENT_MANY_STRINGS, 2);

        return ImmutableOneTypedTreatmentManyStrings.builder()
                .category(toTypedCategory((String) function.parameters().get(0)))
                .strings(toStringList(function.parameters().get(1)))
                .build();
    }

    @NotNull
    public OneTypedTreatmentManyStringsOneInteger createOneTypedTreatmentManyStringsOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TYPED_TREATMENT_MANY_STRINGS_ONE_INTEGER, 3);

        return ImmutableOneTypedTreatmentManyStringsOneInteger.builder()
                .category(toTypedCategory((String) function.parameters().get(0)))
                .strings(toStringList(function.parameters().get(1)))
                .integer(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    private TreatmentCategory toTypedCategory(@NotNull String string) {
        TreatmentCategory category = TreatmentCategoryResolver.fromString(string);
        if (!category.hasType()) {
            throw new IllegalStateException("Not a typed category: " + category.display());
        }
        return category;
    }

    @NotNull
    public TumorTypeInput createOneTumorTypeInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TUMOR_TYPE, 1);

        return TumorTypeInput.fromString((String) function.parameters().get(0));
    }

    @NotNull
    public String createOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_STRING, 1);

        return (String) function.parameters().get(0);
    }

    @NotNull
    public OneIntegerOneString createOneStringOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_STRING_ONE_INTEGER, 2);

        return ImmutableOneIntegerOneString.builder()
                .string((String) function.parameters().get(0))
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public OneIntegerManyStrings createManyStringsOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.MANY_STRINGS_ONE_INTEGER, 2);

        return ImmutableOneIntegerManyStrings.builder()
                .strings(toStringList(function.parameters().get(0)))
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public TwoIntegersManyStrings createManyStringsTwoIntegersInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.MANY_STRINGS_TWO_INTEGERS, 3);

        return ImmutableTwoIntegersManyStrings.builder()
                .strings(toStringList(function.parameters().get(0)))
                .integer1(Integer.parseInt((String) function.parameters().get(1)))
                .integer2(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public ManyTreatmentsWithName createManyTreatmentsWithNames(@NotNull EligibilityFunction function) {
        assertParamType(function, FunctionInput.MANY_TREATMENTS_WITH_NAME);

        List<TreatmentInputWithName> treatmentsWithName = Lists.newArrayList();
        for (Object param : function.parameters()) {
            treatmentsWithName.add(toTreatmentWithName(param));
        }

        if (treatmentsWithName.isEmpty()) {
            throw new IllegalStateException("Missing treatment with name input for function: " + function.rule());
        }

        return ImmutableManyTreatmentsWithName.builder().treatmentsWithName(treatmentsWithName).build();
    }

    @NotNull
    public OneIntegerOneString createOneIntegerOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_ONE_STRING, 2);

        return ImmutableOneIntegerOneString.builder()
                .integer(Integer.parseInt((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public OneIntegerManyStrings createOneIntegerManyStringsInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_MANY_STRINGS, 2);

        return ImmutableOneIntegerManyStrings.builder()
                .integer(Integer.parseInt((String) function.parameters().get(0)))
                .strings(toStringList(function.parameters().get(1)))
                .build();
    }

    @NotNull
    public TumorStage createOneTumorStageInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TUMOR_STAGE, 1);

        return TumorStage.valueOf((String) function.parameters().get(0));
    }

    @NotNull
    public OneHlaAllele createOneHlaAlleleInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_HLA_ALLELE, 1);

        String allele = (String) function.parameters().get(0);
        if (!MolecularInputChecker.isHlaAllele(allele)) {
            throw new IllegalArgumentException("Not a proper HLA allele: " + allele);
        }

        return ImmutableOneHlaAllele.builder().allele(allele).build();
    }

    @NotNull
    public OneGene createOneGeneInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_GENE, 1);

        String gene = (String) function.parameters().get(0);
        if (!molecularInputChecker.isGene(gene)) {
            throw new IllegalStateException("Not a valid gene: " + gene);
        }

        return ImmutableOneGene.builder().geneName(gene).build();
    }

    @NotNull
    public OneGeneOneInteger createOneGeneOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_GENE_ONE_INTEGER, 2);

        String gene = (String) function.parameters().get(0);
        if (!molecularInputChecker.isGene(gene)) {
            throw new IllegalStateException("Not a valid gene: " + gene);
        }

        return ImmutableOneGeneOneInteger.builder().geneName(gene).integer(Integer.parseInt((String) function.parameters().get(1))).build();
    }

    @NotNull
    public OneGeneOneIntegerOneVariantType createOneGeneOneIntegerOneVariantTypeInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE, 3);

        String gene = (String) function.parameters().get(0);
        if (!molecularInputChecker.isGene(gene)) {
            throw new IllegalStateException("Not a valid gene: " + gene);
        }

        return ImmutableOneGeneOneIntegerOneVariantType.builder()
                .geneName(gene)
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .variantType(VariantTypeInput.valueOf((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public OneGeneTwoIntegers createOneGeneTwoIntegersInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_GENE_TWO_INTEGERS, 3);

        String gene = (String) function.parameters().get(0);
        if (!molecularInputChecker.isGene(gene)) {
            throw new IllegalStateException("Not a valid gene: " + gene);
        }

        return ImmutableOneGeneTwoIntegers.builder()
                .geneName(gene)
                .integer1(Integer.parseInt((String) function.parameters().get(1)))
                .integer2(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public OneGeneManyCodons createOneGeneManyCodonsInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_GENE_MANY_CODONS, 2);

        String gene = (String) function.parameters().get(0);
        if (!molecularInputChecker.isGene(gene)) {
            throw new IllegalStateException("Not a valid gene: " + gene);
        }

        List<String> codons = toStringList(function.parameters().get(1));
        for (String codon : codons) {
            if (!MolecularInputChecker.isCodon(codon)) {
                throw new IllegalStateException("Not a valid codon: " + codon);
            }
        }

        return ImmutableOneGeneManyCodons.builder().geneName(gene).codons(codons).build();
    }

    @NotNull
    public OneGeneManyProteinImpacts createOneGeneManyProteinImpactsInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS, 2);

        String gene = (String) function.parameters().get(0);
        if (!molecularInputChecker.isGene(gene)) {
            throw new IllegalStateException("Not a valid gene: " + gene);
        }

        List<String> proteinImpacts = toStringList(function.parameters().get(1));
        for (String proteinImpact : proteinImpacts) {
            if (!MolecularInputChecker.isProteinImpact(proteinImpact)) {
                throw new IllegalStateException("Not a valid protein impact: " + proteinImpact);
            }
        }
        return ImmutableOneGeneManyProteinImpacts.builder().geneName(gene).proteinImpacts(proteinImpacts).build();
    }

    @NotNull
    public ManyGenes createManyGenesInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.MANY_GENES, 1);

        List<String> genes = toStringList(function.parameters().get(0));
        for (String gene : genes) {
            if (!molecularInputChecker.isGene(gene)) {
                throw new IllegalStateException("Not a valid gene: " + gene);
            }
        }

        return ImmutableManyGenes.builder().geneNames(genes).build();
    }

    @NotNull
    public String createOneDoidTermInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_DOID_TERM, 1);

        String param = (String) function.parameters().get(0);
        if (doidModel.resolveDoidForTerm(param) == null) {
            throw new IllegalStateException("Not a valid DOID term: " + param);
        }
        return param;
    }

    @NotNull
    public static EligibilityFunction createOneCompositeParameter(@NotNull EligibilityFunction function) {
        assertParamCount(function, 1);

        return (EligibilityFunction) function.parameters().get(0);
    }

    @NotNull
    public static List<EligibilityFunction> createAtLeastTwoCompositeParameters(@NotNull EligibilityFunction function) {
        if (function.parameters().size() < 2) {
            throw new IllegalArgumentException(
                    "Not enough parameters passed into '" + function.rule() + "': " + function.parameters().size());
        }

        List<EligibilityFunction> functions = Lists.newArrayList();
        for (Object input : function.parameters()) {
            functions.add((EligibilityFunction) input);
        }
        return functions;
    }

    @NotNull
    private static TreatmentInputWithName toTreatmentWithName(@NotNull Object param) {
        String[] parts = ((String) param).split(MANY_STRING_SEPARATOR);

        if (parts.length > 2) {
            throw new IllegalStateException("No valid treatment with name input: " + param);
        }

        TreatmentInput treatment = TreatmentInput.fromString(parts[0].trim());
        String name = null;
        if (parts.length > 1) {
            String nameInput = parts[1].trim();
            name = !nameInput.isEmpty() ? nameInput : null;
        }

        return ImmutableTreatmentInputWithName.builder().treatment(treatment).name(name).build();
    }

    @NotNull
    private static List<String> toStringList(@NotNull Object param) {
        List<String> strings = Lists.newArrayList();
        for (String input : ((String) param).split(MANY_STRING_SEPARATOR)) {
            strings.add(input.trim());
        }
        return strings;
    }

    private static void assertParamConfig(@NotNull EligibilityFunction function, @NotNull FunctionInput requestedInput, int expectedCount) {
        assertParamType(function, requestedInput);
        assertParamCount(function, expectedCount);
    }

    private static void assertParamType(@NotNull EligibilityFunction function, @NotNull FunctionInput requestedInput) {
        if (requestedInput != RULE_INPUT_MAP.get(function.rule())) {
            throw new IllegalStateException("Incorrect type of inputs requested for '" + function.rule() + "': " + requestedInput);
        }
    }

    private static void assertParamCount(@NotNull EligibilityFunction function, int expectedCount) {
        if (function.parameters().size() != expectedCount) {
            throw new IllegalArgumentException(
                    "Invalid number of inputs passed to '" + function.rule() + "': " + function.parameters().size());
        }
    }
}
