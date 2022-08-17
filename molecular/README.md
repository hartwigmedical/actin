## ACTIN-Molecular

ACTIN-Molecular interprets molecular results and maps to the datamodel described below. This molecular model is written to a per-sample 
json file. Currently, ACTIN-Molecular only supports interpretation of [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange) 
results as produced by [HMF Platinum](https://github.com/hartwigmedical/platinum) 

The ORANGE interpreter application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.molecular.orange.OrangeInterpreterApplication \
   -orange_json /path/to/orange.json \
   -external_trial_mapping_tsv /path/to/external_trial_mapping.tsv \
   -output_directory /path/to/where/molecular_json_file_is_written
```

The following assumptions are made about the inputs:
 - The ORANGE json is the json output from [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange)
 
The external trial mapping enables mapping of external trial acronyms to those used within ACTIN, in case there is duplication of 
trials across both sources.

## Molecular Datamodel

Every sample, uniquely defined by their sample ID, has a molecular record with the following data:

Field | Example Value | Details
---|---|---
type | WGS | The type of molecular experiment done. Currently only 'WGS' is supported.
date | 2022-01-14 | The date on which the molecular results were obtained (optional field).
containsTumorCells | true | If false, implies that the tumor cell percentage in the biopsy was lower than the lowest detectable threshold.
hasSufficientQuality | true | If false, implies that the quality of the sample was not sufficient (e.g. too much DNA damage).  
 
1 molecular characteristics

Field | Example Value | Details
---|---|---
purity | 78% | The percentage of cells in the sequenced biopsy that originated from the tumor.
predictedTumorType | Melanoma (87%) | The tumor type of origin predicted based on the molecular data along with a likelihood. 
isMicrosatelliteUnstable | false | If true, sample is considered microsatellite unstable. Can be left blank in case experiment does not determine MSI.
isHomologousRepairDeficient | false | If true, sample is considered homologous repair deficient. Can be left blank in case experiment does not determine HRD.
tumorMutationalBurden | 14.2 | Number of mutations in the genome per Mb. Can be left blank in case experiment does not determine TMB.
tumorMutationalLoad | 115 | Number of missense mutations across the genome. Can be left blank in case experiment does not determine TML.

N variants  

Field | Example Value | Details
---|---|---
event | APC D1174fs | A single representation of the event, expected to match with the event string from evidence section.
driverLikelihood | HIGH | Either `HIGH`, `MEDIUM` or `LOW`
gene | APC | The gene impacted by the variant
impact | p.D1174fs | The impact of the variant on the gene 
variantCopyNumber | 3.8 | The number of copies of this variant in the tumor
totalCopyNumber | 4.0 | The total number of copies in the tumor on the mutated position
driverType | BIALLELIC | Either `HOTSPOT`, `BIALLELIC` or `VUS`
clonalLikelihood | 100% | Likelihood that the variant exists in every tumor cell (is clonal).

N amplifications

Field | Example Value | Details
---|---|---
event | MYC amp | A single representation of the event, expected to match with the event string from evidence section.
driverLikelihood | HIGH | Either `HIGH`, `MEDIUM` or `LOW`
gene | MYC | The gene that has been amplified
copies | 150 | Number of copies of this gene in the tumor
isPartial | false| Indicates whether the gene has been partially or fully amplified in the tumor

N losses

Field | Example Value | Details
---|---|---
event | TPS del | A single representation of the event, expected to match with the event string from evidence section.
driverLikelihood | HIGH | Either `HIGH`, `MEDIUM` or `LOW`
gene | TP53 | The gene that has been lost in the tumor 
isPartial | true | Indicates whether the gene has been partially or fully lost in the tumor 

N homozygous disruptions

Field | Example Value | Details
---|---|---
event | PTEN disruption | A single representation of the event, expected to match with the event string from evidence section.
driverLikelihood | HIGH | Either `HIGH`, `MEDIUM` or `LOW`
gene | PTEN | The gene that has been homozygously disrupted in the tumor  

N disruptions

Field | Example Value | Details
---|---|---
event | BRCA1 disruption | A single representation of the event, expected to match with the event string from evidence section.
driverLikelihood | LOW | Either `HIGH`, `MEDIUM` or `LOW`
gene | BRCA1 | The gene that has been disrupted
type | DUP | Type of disruption
junctionCopyNumber | 1.1 | Number of copies affected by this disruption
undisruptedCopyNumber | 1.8 | Remaining number of copies not impacted by this disruption
range | Intron 12 downstream | Provides additional details about the range impacted by the disruption.

N fusions

Field | Example Value | Details
---|---|---
event | EML4-ALK fusion | A single representation of the event, expected to match with the event string from evidence section.
driverLikelihood | HIGH | Either `HIGH`, `MEDIUM` or `LOW`
fiveGene | EML4 | The gene that makes up the 5' part of the fusion
threeGene | ALK | The gene that makes up the 3' part of the fusion
details | Exon 2 - Exon 5 | Additional details about the fusion
driverType | KNOWN | Either `KNOWN` or `PROMISCUOUS`

N viruses

Field | Example Value | Details
---|---|---
event | HPV positive | A single representation of the event, expected to match with the event string from evidence section.
driverLikelihood | HIGH | Either `HIGH`, `MEDIUM` or `LOW`
name | Human papillomavirus type 16 | Name of the virus found in the tumor sample
integrations | 3 | Number of integrations of detected virus in the tumor sample

N HLA alleles (with a single isReliable boolean indicating whether the HLA results are reliable)

Field | Example Value | Details
---|---|---
name | A*02:01 | Name of the HLA allele
tumorCopyNumber | 1.2 | The number of copies of this HLA allele in the tumor sample. 
hasSomaticMutations | false | A boolean indicating whether any mutations have occurred in this allele in the tumor

N pharmaco

Field | Example Value | Details
---|---|---
gene | DPYD | The gene for which the pharmaco entry is applicable
haplotype | 1* HOM | Haplotypes found for the gene  
haplotypeFunction | Function impact of corresponding haplotype

N wild-type genes (optional)

If the molecular experiment was not suitable to call wild-type genes this list can be omitted.

N actin trial evidences (with a single configured source name)

Field | Example Value | Details
---|---|---
event | BRAF V600E | The molecular event against which the evidence has been matched.
trialAcronym | Trial A | The acronym of the trial for which the evidence holds.
cohortCode | A | The ID of the cohort for which the evidence holds (optional, in case evidence holds for all cohorts within a trial).
isInclusionCriterion | 1 | Whether the evidence is involved in an inclusion or exclusion criterion for this trial.
type | ACTIVATED_GENE | The type of molecular event required by the trial. Can be `SIGNATURE`, `ACTIVATED_GENE`, `INACTIVATED_GENE`, `AMPLIFIED_GENE`, `FUSED_GENE`, `MUTATED_GENE`, `WILD_TYPE_GENE` and `HLA_ALLELE`
gene | BRAF | The gene required to be mutated by the trial (optional, empty in case of `SIGNATURE` and `HLA_ALLELE`)
mutation | null | The mutation required to be present by the trial (optional, only in case of `MUTATED_GENE` and `HLA_ALLELE`).

N external trial evidences (with a single configured source name)

Field | Example Value | Details
---|---|---
event | High TMB | The molecular event against which the evidence has been matched again.
trial | Trial A | The name of the trial for which the evidence holds.

N treatment evidences of various types, with a single configured source name. The types are as follows:
 
Type | Details
---|---
approvedEvidence |  Treatments which are approved for the sample tumor type.
onLabelExperimentalEvidence | Treatments which are experimental for the specific sample tumor type.
offLabelExperimentalEvidence | Treatment which are experimental for a different tumor type.
preClinicalEvidence | Treatments that have some evidence but haven't reached experimental state yet.
knownResistanceEvidence | Evidence of resistance for treatments that are considered reliable.  
suspectResistanceEvidence | Evidence of resistance for treatments that has not yet been confirmed to be reliable.

### Interpretation of ORANGE results

The ORANGE interpreter application maps the ORANGE output to the molecular datamodel as follows:

Field | Mapping
---|---
sampleId | The ORANGE field `sampleId`
type | Hard-coded to `WGS` 
date | The ORANGE field `experimentDate`
containsTumorCells | The PURPLE field `hasReliablePurity`
hasSufficientQuality | The PURPLE field `hasReliableQuality` 

The characteristics are extracted as follows:

Field | Mapping
---|---
purity | The PURPLE field `purity`
predictedTumorOrigin | The CUPPA best cancer-type prediction along with the likelihood
isMicrosatelliteUnstable | The interpretation of PURPLE `microsatelliteStabilityStatus`
isHomologousRepairDeficient | The interpretation of CHORD `hrStatus`
tumorMutationalBurden | The PURPLE field `tumorMutationalBurden`
tumorMutationalLoad | The PURPLE field `tumorMutationalLoad`

The drivers are extracted from the following algorithms:

Driver Type | Algo | Details
---|---|---
variants | PURPLE | Union of reported somatic variants and reported germline variants. 
amplifications | PURPLE | Union of reported somatic full gains and reported somatic partial gains.
losses | PURPLE | Union of reported somatic full losses and reported somatic partial losses.
homozygousDisruptions | LINX | All reported homozygous disruptions. 
disruptions | LINX | All reported somatic gene disruptions.
fusions | LINX | All reported fusions with driver type mapped to either `KNOWN` or `PROMISCUOUS`.
viruses | VirusInterpreter | All reported viruses.

Note that all floating point numbers are rounded to 3 digits when ingesting data into ACTIN:
 - variants: `variantCopyNumber`, `totalCopyNumber`, `clonalLikelihood`
 - disruptions: `junctionCopyNumber`, `undisruptedCopyNumber`

The pharmaco entries are extracted from PEACH.

The wild-type genes are extracted from the ORANGE list of wild-type genes. 
In case the ORANGE results were not reliable, no wild-type genes will be set.

The evidence is extracted from PROTECT in the following steps:
 1. Evidence is filtered for applicability based on ACTIN's internal applicability model. This applicability model removes evidence 
 from any source that is never considered to be applicable. 
 1. Reported evidence is used exclusively for the inputs to the steps defined below

For ACTIN trial evidence, the following mapping is performed based on the assumption that PROTECT has been run against a SERVE database 
containing all ACTIN molecular criteria:
 - `trialAcronym` and `cohortId` are extracted from the `treatment` field in PROTECT
 - `isUsedAsInclusion` is set to true in case the evidence from PROTECT is responsive
 - `type`, `gene` and `mutation` are extracted from the PROTECT `ACTIN` source data 
 
For external trial evidence, treatments are mapped to trials from the `ICLUSION` source in PROTECT
  
For treatment evidence, the following categorization is done based on evidence from the `CKB` source in PROTECT:
 
Field | Filter 
---|---
approvedEvidence | A-level on-label non-predicted responsive evidence. 
onLabelExperimentalEvidence | Union of A-level that is either predicted or off-label responsive with B-level on-label non-predicted responsive evidence.
offLabelExperimentalEvidence | B-level off-label non-predicted responsive evidence.
preClinicalEvidence | All responsive evidence that is neither approved nor experimental. 
knownResistanceEvidence | A or B-level non-predicted resistance evidence for a treatment for which non-preclinical evidence exists with equal or lower evidence level compared to the resistance evidence level.  
suspectResistanceEvidence | Any other resistance evidence for a treatment with evidence with equal or lower evidence level compared to the resistance evidence level.

Note that in case of no tumor cells, both evidence and drivers are wiped empty. Any driver (and related evidence) is considered to be unreliable in such a case.

### Version History and Download Links
 - Upcoming (first release) 
