## ACTIN-Molecular

ACTIN-Molecular interprets molecular results and maps to the datamodel described below. This molecular model is written to a per-sample json
file. Currently, ACTIN-Molecular only supports interpretation of [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange)
results as produced by [HMF Platinum](https://github.com/hartwigmedical/platinum)

The ORANGE interpreter application requires Java 11+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.molecular.orange.OrangeInterpreterApplication \
   -orange_json /path/to/orange.json \
   -serve_directory /path/to/serve_directory \
   -known_genes_tsv /path/to/known_genes.tsv \
   -external_trial_mapping_tsv /path/to/external_trial_mapping.tsv \
   -clinical_json /path/to/actin_clinical.json \
   -output_directory /path/to/where/molecular_json_file_is_written
```

An optional `log_debug` parameter can be provided to generate extra logging.

The following assumptions are made about the inputs:

- The ORANGE json is the json output from [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange)
- The SERVE directory is the output of [SERVE](https://github.com/hartwigmedical/serve/tree/master/algo) and is used for annotation and
  interpretation of the genomic findings from ORANGE.
- The known genes is a TSV with gene and geneRole columns and is a resource that will be moved into SERVE in the future. An example can be
  found [here](https://github.com/hartwigmedical/actin/blob/master/common/src/test/resources/known_genes/example_known_genes.tsv)
- The clinical json is the output of [ACTIN Clinical](https://github.com/hartwigmedical/actin/tree/master/clinical) and is used to extract
  the primary tumor doids from, in order to determine whether evidence is on-label or off-label.
- The external trial mapping enables mapping of external trial acronyms to those used within ACTIN, in case there is duplication of
  trials across both sources. An example can be
  found [here](https://github.com/hartwigmedical/actin/blob/master/molecular/src/test/resources/curation/external_trial_mapping.tsv)

## Molecular Datamodel

Evidence is attached to mutations or characteristics and has the following datamodel:

| Field                          | Example Value | Details                                                                                                                                  |
|--------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------|
| approvedTreatments             | Pembrolizumab | A set of treatment names which are approved based on tumor type and mutation                                                             |
| externalEligibleTrials         | Trial A       | A set of trials for which patient may be eligible based on tumor type and mutation                                                       |
| onLabelExperimentalTreatments  | Olaparib      | A set of treatment names which are considered on-label experimental based on tumor type and mutation                                     |
| offLabelExperimentalTreatments | Olaparib      | A set of treatment names which are off-label experimental for specific tumor type and mutation                                           |
| preClinicalTreatments          | New Drug A    | A set of treatment names which have not yet been introduced experimentally yet have some supportive evidence for mutation and tumor type |
| knownResistantTreatments       | Erlotinib     | A set of treatment names which are known to be resisted by the mutation for the specific tumor type                                      |
| suspectResistantTreatments     | Erlotinib     | A set of treatment names for which there is some evidence that they may be resisted by the mutation for the specific tumor type          |

A molecular record belongs to a `sampleId` which in turn belongs to a `patientId`. Every molecular record has the following base data:

| Field                | Example Value | Details                                                                                                       |
|----------------------|---------------|---------------------------------------------------------------------------------------------------------------|
| type                 | WGS           | The type of molecular experiment done, either `WGS` or `PANEL`                                                |
| refGenomeVersion     | V37           | The version of the reference genome used in this record, either `V37` or `V38`                                |
| date                 | 2022-01-14    | The date on which the molecular results were obtained (optional field)                                        |
| evidenceSource       | CKB           | The name of the provider of the evidence. Currently always `CKB`                                              |
| externalTrialSource  | ICLUSION      | The name of the provider of external trials (not known in ACTIN trial database). Currently always `ICLUSION`  |
| containsTumorCells   | true          | If false, implies that the tumor cell percentage in the biopsy was lower than the lowest detectable threshold |
| hasSufficientQuality | true          | If false, implies that the quality of the sample was not sufficient (e.g. too much DNA damage)                |

### 1 molecular characteristics

| Field                         | Example Value  | Details                                                                                                                |
|-------------------------------|----------------|------------------------------------------------------------------------------------------------------------------------|
| purity                        | 78%            | The percentage of cells in the sequenced biopsy that originated from the tumor                                         |
| ploidy                        | 3.1            | The average number of copies of any chromosome in the tumor                                                            |
| predictedTumorType            | Melanoma (87%) | The tumor type of origin predicted based on the molecular data along with a likelihood                                 |
| isMicrosatelliteUnstable      | false          | If true, sample is considered microsatellite unstable. Can be left blank in case experiment does not determine MSI     |
| microsatelliteEvidencce       | See evidence   | The evidence determined for the microsatellite status of specific tumor sample                                         |                                         |
| isHomologousRepairDeficient   | false          | If true, sample is considered homologous repair deficient. Can be left blank in case experiment does not determine HRD |
| homologousRepairEvidence      | See evidence   | The evidence determined for the homologous repair status of specific tumor sample                                      |                                      |
| tumorMutationalBurden         | 14.2           | Number of mutations in the genome per Mb. Can be left blank in case experiment does not determine TMB                  |
| hasHighTumorMutationalBurden  | true           | Indicates whether the `tumorMutationalBurden` is considered high or not                                                |
| tumorMutationalBurdenEvidence | See evidence   | The evidence determined for the tumor mutational burden status of specific tumor sample.                               |
| tumorMutationalLoad           | 115            | Number of missense mutations across the genome. Can be left blank in case experiment does not determine TML            |
| hasHighTumorMutationalLoad    | false          | Indicates whether the `tumorMutationalLoad` is considered high or not                                                  |
| tumorMutationalLoadEvidence   | See evidence   | The evidence determined for the tumor mutational load of specific tumor sample                                         |

### N drivers

Every molecular driver has the following fields:

| Field            | Example Value | Details                                                                                                     |
|------------------|---------------|-------------------------------------------------------------------------------------------------------------|
| isReportable     | true          | Indicates whether this driver is considered relevant enough to be explicitly mentioned in a report          |
| event            | BRAF V600E    | A human readable string summarizing the driver event                                                        |
| driverLikelihood | HIGH          | An optional field that indicates the likelihood that the event is a driver (either `HIGH`, `MEDIUM`, `LOW`) |
| evidence         | See evidence  | The evidence determined for this driver in the specific tumor sample                                        |

Furthermore, every driver on a specific gene (variant, copy number, disruption) has the following fields:

| Field                          | Example Value    | Details                                                                                                                                                                                                               |
|--------------------------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| gene                           | BRAF             | The name of the gene                                                                                                                                                                                                  |
| geneRole                       | ONCO             | The role of the gene in cancer (either `BOTH`, `ONCO`, `TSG`, or `UNKNOWN`                                                                                                                                            |
| proteinEffect                  | GAIN_OF_FUNCTION | The effect the specific driver has on the gene (one of `UNKNOWN`, `AMBIGIOUS`, `NO_EFFECT`, `NO_EFFECT_PREDICTED`, `LOSS_OF_FUNCTION`, `LOSS_OF_FUNCTION_PREDICTED`, `GAIN_OF_FUNCTION`, `GAIN_OF_FUNCTION_PREDICTED` |
| isAssociatedWithDrugResistance | true             | An optional boolean indicating the specific driver is associated with some form of drug resistance                                                                                                                    |

#### N variants

In addition to the driver and gene fields, the following data is captured per variant:

| Field             | Example Value | Details                                                                                             |
|-------------------|---------------|-----------------------------------------------------------------------------------------------------|
| type              | SNV           | The type of variant (one of `SNV`, `MNV`, `INSERT`, `DELETE`)                                       |
| variantCopyNumber | 3.8           | The number of copies of this variant in the tumor                                                   |
| totalCopyNumber   | 4.0           | The total number of copies in the tumor on the mutated position                                     |
| isBiallelic       | false         | Indicates whether all alleles in the tumor are affected by this variant or not                      |
| isHotspot         | true          | Indicates whether this specific variant is a known (pathogenic) hotspot                             | 
| clonalLikelihood  | 100%          | Likelihood that the variant exists in every tumor cell (is clonal)                                  |
| phaseGroups       | 1, 2          | The phasing groups this variant belongs to. Variants that are phased share at least one phase group |
| canonicalImpact   | See impact    | The impact of this variant on the canonical transcipt of the gene                                   |
| otherImpacts      | See impact    | A set of impacts on transcripts other than the canonical transcript of the gene                     | 

The following data is captured as impact of a variant on a specific transcript

| Field             | Example Value | Details                                                                  |
|-------------------|---------------|--------------------------------------------------------------------------|
| transcriptId      | ENST00001     | The ensembl ID of the transcript                                         | 
| hgvsCodingImpact  | c.123G>T      | The HGVS coding impact on the transcript                                 |
| hgvsProteinImpact | p.V41E        | The HGVS protein impact on the transcript                                |
| affectedCodon     | 41            | Optional field, the codon that is affected by the variant                |
| affectedExon      | 2             | Optional field, the exon that is affected by the variant                 |
| isSpliceRegion    | false         | Indicates whether this variant affects a splice region of the transcript |
| effects           | MISSENSE      | A set of effects that this variant has on the transcript                 |
| codingEffect      | MISSENSE      | A single, summarized coding effect this variant has on the transcript    |

#### N copy numbers

In addition to the driver and gene fields, the following data is captured per copy number:

| Field     | Example Value | Details                                                                        |
|-----------|---------------|--------------------------------------------------------------------------------|
| type      | FULL_GAIN     | The type of copy number (either `FULL_GAIN`, `PARTIAL_GAIN`, `LOSS` or `NONE`) |
| minCopies | 12            | The minimum copy number of the gene along the canonical transcript of the gene |
| maxCopies | 18            | The maximum copy number of the gene along the canonical transcript of the gene |

#### N homozygous disruptions

For homozygous disruptions, no additional data is captured beyond the driver and gene fields.

#### N disruptions

In addition to the driver and gene fields, the following data is captured per disruption:

| Field                 | Example Value | Details                                                                                                          |
|-----------------------|---------------|------------------------------------------------------------------------------------------------------------------|
| type                  | BND           | The type of disruption (either `BND`, `DEL`, `DUP`, `INF`, `INS`, `INV` or `SGL`)                                |
| junctionCopyNumber    | 1.1           | Number of copies affected by this disruption                                                                     |
| undisruptedCopyNumber | 1.8           | Remaining number of copies not impacted by this disruption                                                       |
| regionType            | INTRONIC      | The region whether this disruption starts or ends (either `INTRONIC`, `EXONIC`, `UPSTREAM`, `DOWNSTREAM` or `IG` |
| codingContext         | UTR_5P        | The coding context of the disruption (either `ENHANCER`, `UTR_5P`, `CODING`, `NON_CODING`, `UTR_3P`              |
| clusterGroup          | 3             | The ID of the cluster this disruption belongs to                                                                 |

#### N fusions

In addition to the driver fields, the following data is captured per fusion:

| Field                          | Example Value    | Details                                                                        |
|--------------------------------|------------------|--------------------------------------------------------------------------------|
| geneStart                      | EML4             | The gene that makes up the 5' part of the fusion                               |
| geneTranscriptStart            | ENST001          | The ensembl ID of the transcript that makes up the 5' part of the fusion       |
| fusedExonUp                    | 10               | The last exon of the 5' gene included in the fusion                            |
| geneEnd                        | ALK              | The gene that makes up the 3' part of the fusion                               |
| geneTranscriptEnd              | ENST002          | The ensembl ID of the transcript that makes up the 3' part of the fusion       |
| fusedExonDown                  | 22               | The first exon of the 3' gene included in the fusion                           |
| driverType                     | KNOWN_PAIR       | The type of driver fusion                                                      |
| proteinEffect                  | GAIN_OF_FUNCTION | The type of protein effect of the fusion product                               |
| isAssociatedWithDrugResistance | null             | Optional field, indiates whether the fusion is associated with drug resistance |

#### N viruses

In addition to the driver fields, the following data is captured per virus:

| Field        | Example Value                | Details                                                                                      |
|--------------|------------------------------|----------------------------------------------------------------------------------------------|
| name         | Human papillomavirus type 16 | Name of the virus found in the tumor sample                                                  |
| type         | HUMAN_PAPILLOMA_VIRUS        | The type of virus found in the tumor sample                                                  |
| isReliable   | false                        | Boolean indicated whether the virus has reliably been found and could be considered a driver | 
| integrations | 3                            | Number of integrations of detected virus in the tumor sample                                 |

#### N HLA alleles (with a single `isReliable` boolean indicating whether the HLA results are reliable)

| Field               | Example Value | Details                                                                              |
|---------------------|---------------|--------------------------------------------------------------------------------------|
| name                | A*02:01       | Name of the HLA allele                                                               |
| tumorCopyNumber     | 1.2           | The number of copies of this HLA allele in the tumor sample.                         |
| hasSomaticMutations | false         | A boolean indicating whether any mutations have occurred in this allele in the tumor |

#### N pharmaco

| Field             | Example Value                              | Details                                             |
|-------------------|--------------------------------------------|-----------------------------------------------------|
| gene              | DPYD                                       | The gene for which the pharmaco entry is applicable |
| haplotype         | 1* HOM                                     | Haplotypes found for the gene                       |
| haplotypeFunction | Function impact of corresponding haplotype |                                                     |



### Interpretation of ORANGE results

THIS IS WORK IN PROGRESS

The ORANGE interpreter application maps the ORANGE output to the molecular datamodel as follows:

| Field                | Mapping                               |
|----------------------|---------------------------------------|
| sampleId             | The ORANGE field `sampleId`           |
| type                 | Hard-coded to `WGS`                   |
| date                 | The ORANGE field `experimentDate`     |
| containsTumorCells   | The PURPLE field `hasReliablePurity`  |
| hasSufficientQuality | The PURPLE field `hasReliableQuality` |

The characteristics are extracted as follows:

| Field                       | Mapping                                                         |
|-----------------------------|-----------------------------------------------------------------|
| purity                      | The PURPLE field `purity`                                       |
| predictedTumorOrigin        | The CUPPA best cancer-type prediction along with the likelihood |
| isMicrosatelliteUnstable    | The interpretation of PURPLE `microsatelliteStabilityStatus`    |
| isHomologousRepairDeficient | The interpretation of CHORD `hrStatus`                          |
| tumorMutationalBurden       | The PURPLE field `tumorMutationalBurden`                        |
| tumorMutationalLoad         | The PURPLE field `tumorMutationalLoad`                          |

The drivers are extracted from the following algorithms:

| Driver Type           | Algo             | Details                                                                          |
|-----------------------|------------------|----------------------------------------------------------------------------------|
| variants              | PURPLE           | Union of reported somatic variants and reported germline variants.               |
| amplifications        | PURPLE           | Union of reported somatic full gains and reported somatic partial gains.         |
| losses                | PURPLE           | Union of reported somatic full losses and reported somatic partial losses.       |
| homozygousDisruptions | LINX             | All reported homozygous disruptions.                                             |
| disruptions           | LINX             | All reported somatic gene disruptions.                                           |
| fusions               | LINX             | All reported fusions with driver type mapped to either `KNOWN` or `PROMISCUOUS`. |
| viruses               | VirusInterpreter | All reported viruses.                                                            |

Note that all floating point numbers are rounded to 3 digits when ingesting data into ACTIN:

- variants: `variantCopyNumber`, `totalCopyNumber`, `clonalLikelihood`
- disruptions: `junctionCopyNumber`, `undisruptedCopyNumber`

The pharmaco entries are extracted from PEACH.

The wild-type genes are extracted from the ORANGE list of wild-type genes.
In case the ORANGE results were not reliable, no wild-type genes will be set.

The evidence is extracted from PROTECT in the following steps:

1. Evidence is filtered for applicability based on ACTIN's internal applicability model. This applicability model removes evidence
   from any source that is never considered to be applicable.
2. Reported evidence is used exclusively for the inputs to the steps defined below

For ACTIN trial evidence, the following mapping is performed based on the assumption that PROTECT has been run against a SERVE database
containing all ACTIN molecular criteria:

- `trialAcronym` and `cohortId` are extracted from the `treatment` field in PROTECT
- `isUsedAsInclusion` is set to true in case the evidence from PROTECT is responsive
- `type`, `gene` and `mutation` are extracted from the PROTECT `ACTIN` source data

For external trial evidence, treatments are mapped to trials from the `ICLUSION` source in PROTECT

For treatment evidence, the following categorization is done based on evidence from the `CKB` source in PROTECT:

| Field                        | Filter                                                                                                                                                                                 |
|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| approvedEvidence             | A-level on-label non-predicted responsive evidence.                                                                                                                                    |
| onLabelExperimentalEvidence  | Union of A-level that is either predicted or off-label responsive with B-level on-label non-predicted responsive evidence.                                                             |
| offLabelExperimentalEvidence | B-level off-label non-predicted responsive evidence.                                                                                                                                   |
| preClinicalEvidence          | All responsive evidence that is neither approved nor experimental.                                                                                                                     |
| knownResistanceEvidence      | A or B-level non-predicted resistance evidence for a treatment for which non-preclinical evidence exists with equal or lower evidence level compared to the resistance evidence level. |
| suspectResistanceEvidence    | Any other resistance evidence for a treatment with evidence with equal or lower evidence level compared to the resistance evidence level.                                              |

Note that in case of no tumor cells, both evidence and drivers are wiped empty. Any driver (and related evidence) is considered to be
unreliable in such a case.

### Version History and Download Links

- Upcoming (first release) 
