# EXPERIMENTS.md

# Medicine Scanner — Experiment Log & Recognition Benchmark

> **Purpose:** Preserve evidence.  
> **Rule:** Never fabricate results. Planned experiments are marked `PLANNED`; only measured runs may be marked `COMPLETED`.

---

# 1. Why this document exists

This project contains technical questions that cannot be answered reliably by architecture discussion alone:

- How well does OCR read real Bangladeshi medicine packaging?
- Which fields survive glare, blur, rotation, and cut blister strips?
- Does barcode scanning materially improve identification?
- Which normalization rules help or hurt?
- Does OpenCV preprocessing improve OCR on the actual dataset?
- How accurately can a matcher distinguish same-brand strengths?
- When should the system abstain?

This document is the laboratory notebook for those questions.

---

# 2. Experiment rules

Every experiment must include:

1. ID and title
2. Status
3. Question
4. Hypothesis
5. Dataset/version
6. Baseline
7. Single main variable/change
8. Procedure
9. Metrics
10. Results
11. Failure analysis
12. Conclusion
13. Decision/action

When possible, change one important variable at a time.

---

# 3. Dataset protocol

## 3.1 Dataset goal

Create a real-world benchmark, not a gallery of perfect product photos.

Initial planning target:

- about 100 distinct products;
- about 400–600 images.

The exact count matters less than the coverage of difficult conditions.

---

## 3.2 Ground-truth record

Each image should have metadata similar to:

```yaml
image_id: IMG_000231
dataset_version: v0.1

product:
  canonical_product_id: local-test-id
  brand: Napa Extend
  generic: Paracetamol
  strength: 665 mg
  dosage_form: Tablet
  manufacturer: Beximco Pharmaceuticals

package:
  type: blister
  language:
    - en
  barcode_present: false

capture:
  device_model: ...
  lighting: low
  glare: medium
  rotation_bucket: 0-30
  perspective: mild
  blur: none
  distance: close
  partial_package: true

notes: ...
```

Do not infer ground truth from OCR output.

Ground truth must be manually verified from the actual product/package.

---

# 4. Dataset splits

As the dataset grows, separate:

- **development set** — used while designing normalization/matching;
- **test set** — kept aside for honest evaluation.

If the same physical package appears in many nearly identical photos, avoid splitting those near-duplicates across train/development and test in a way that makes results artificially easy.

For a non-trained OCR baseline, this still matters because tuning rules on near-identical images can overfit.

---

# 5. Required hard-case coverage

Include product groups with:

- same brand, different strength;
- same brand, different release/formulation;
- similar brand spelling;
- same generic, different manufacturers;
- similar manufacturer names;
- small fonts;
- reflective foil;
- partly cut strips;
- worn packages;
- rotated text;
- bilingual text;
- multiple text panels;
- barcode and no barcode.

Safety-relevant confusion groups should receive extra tests.

---

# 6. Failure taxonomy

Use consistent tags.

### Image acquisition
- `BLUR`
- `LOW_LIGHT`
- `OVEREXPOSED`
- `GLARE`
- `TOO_FAR`
- `PARTIAL_PACKAGE`
- `PERSPECTIVE`
- `ROTATION`

### OCR
- `BRAND_MISREAD`
- `GENERIC_MISREAD`
- `STRENGTH_MISREAD`
- `UNIT_MISREAD`
- `MANUFACTURER_MISREAD`
- `TEXT_MISSED`
- `SCRIPT_UNSUPPORTED`
- `TEXT_ORDER_CONFUSION`

### Matching
- `NO_CANDIDATE`
- `WRONG_TOP1`
- `CORRECT_IN_TOP3`
- `VARIANT_CONFUSION`
- `STRENGTH_CONFLICT`
- `MANUFACTURER_CONFLICT`
- `FALSE_CONFIDENT`
- `OVER_ABSTAIN`

### Data
- `CATALOG_MISSING_PRODUCT`
- `CATALOG_BAD_ALIAS`
- `BARCODE_UNMAPPED`
- `GROUND_TRUTH_UNCERTAIN`

Add new tags only when the existing taxonomy cannot describe the problem.

---

# 7. Core metrics

## OCR field recall

For a field such as brand:

```text
images where usable brand evidence was extracted
------------------------------------------------
images where brand is visible / expected
```

Track separately for:

- brand;
- generic;
- strength;
- manufacturer;
- dosage form.

---

## Top-1 accuracy

Correct canonical product is candidate #1.

---

## Top-3 recall

Correct product appears anywhere in top 3.

Useful because the product may safely ask the user to choose.

---

## High-confidence precision

Among cases where the policy presents a single high-confidence candidate, how often is it correct?

This matters more than generic overall accuracy.

---

## False-confident-match rate

Cases where the system behaves as though identity is sufficiently strong but the product is wrong.

This is the most safety-sensitive matcher metric.

---

## Abstention rate

How often the system refuses to identify.

High abstention is not automatically bad; it must be interpreted with false-confidence rate and user usability.

---

## Latency

Measure:

- OCR processing;
- matching;
- end-to-end scan-to-candidates.

Use distributions/percentiles rather than one average once the dataset is large enough.

---

# 8. Provisional feasibility bands

These are **engineering decision aids**, not medical/regulatory guarantees.

They may be changed after the first benchmark.

### GREEN tendency

- correct product is usually in top candidates;
- high-confidence decisions are extremely precise;
- ambiguous variants are usually detected rather than silently confused;
- failure categories appear tractable.

### YELLOW tendency

- product is often recoverable but major package categories fail;
- top-3 is useful but top-1 is weak;
- preprocessing/alternate OCR/data improvements may plausibly fix the failures.

### RED tendency

- correct candidate is frequently absent;
- strength/formulation confusion is common;
- high-confidence false matches cannot be controlled without excessive failure;
- success depends on perfect packaging conditions.

Do not force a numeric green light from a small dataset. Look for both aggregate metrics and systematic safety failures.

---

# 9. EXP-000 — Dataset Collection Protocol

**Status:** PLANNED

## Question

Can we create a benchmark that represents realistic medicine-scanning conditions rather than ideal product photography?

## Hypothesis

A deliberately varied dataset will reveal failure modes that are invisible in hand-picked scans.

## Dataset

Initial collection itself.

## Procedure

1. Select a varied medicine-product set.
2. Record canonical ground truth.
3. Photograph each product under several realistic conditions.
4. Tag conditions.
5. Identify safety-relevant confusion groups.
6. Reserve a held-out subset.

## Metrics

- product count;
- image count;
- packaging-type distribution;
- condition distribution;
- number of confusion groups;
- missing/uncertain ground-truth rate.

## Results

_To be filled._

## Conclusion

_To be filled._

## Decision

_To be filled._

---

# 10. EXP-001 — Raw ML Kit OCR Baseline

**Status:** PLANNED

## Question

How much useful identity evidence does raw on-device ML Kit OCR extract from real medicine packaging without preprocessing?

## Hypothesis

Raw OCR will identify brand/strength sufficiently often to justify a structured matcher, while reflective blister packaging will produce a meaningful failure cluster.

## Baseline

None — this becomes the OCR baseline.

## Treatment

```text
Camera image
    ↓
ML Kit Text Recognition
    ↓
raw text
```

No OpenCV preprocessing.

## Procedure

1. Run all benchmark images through the same OCR configuration.
2. Store raw output.
3. Record processing latency.
4. Evaluate field extraction manually or with a verified evaluation script.
5. Break down metrics by packaging and failure tags.

## Metrics

- brand field recall;
- generic field recall;
- strength field recall;
- manufacturer field recall;
- completely unusable OCR rate;
- latency;
- package-type breakdown.

## Results

_To be filled._

## Failure analysis

_To be filled._

## Conclusion

_To be filled._

## Decision

_To be filled._

---

# 11. EXP-002 — Normalization & Structured Field Extraction

**Status:** PLANNED

## Question

Can conservative domain-aware normalization recover useful signals from noisy OCR without introducing unsafe corrections?

## Hypothesis

Normalization will substantially improve matching-ready text, especially for casing, spacing, punctuation, manufacturer suffixes, and units; dosage-strength “correction” will require stricter rules.

## Baseline

Raw OCR tokens from EXP-001.

## Treatment

Add normalization/parser rules.

## Required tests

Examples should include OCR errors like:

```text
66S mg
5OO mg
Paracetamo
PharmaceuticaIs
NAPAEXTEND
```

But rules must not blindly rewrite ambiguous numeric/letter combinations.

## Metrics

- parsed brand recall;
- parsed strength recall;
- false normalization count;
- number of cases where normalization changes correct text into incorrect text.

## Results

_To be filled._

## Conclusion

_To be filled._

## Decision

_To be filled._

---

# 12. EXP-003 — Candidate Matcher v0

**Status:** PLANNED

## Question

Can noisy OCR observations be mapped to the correct medicine product using a small structured catalog?

## Hypothesis

Field-aware matching will outperform flat fuzzy search and will detect variant ambiguity better.

## Candidate catalog

Start with a controlled subset containing:

- common products;
- same-brand variants;
- same-generic competitors;
- similar names.

## Candidate features

Potentially:

- brand similarity;
- generic similarity;
- exact/conflicting strength;
- manufacturer similarity;
- dosage-form evidence;
- barcode mapping where available.

## Metrics

- top-1 accuracy;
- top-3 recall;
- false confident match;
- abstention;
- ambiguity detection;
- variant-confusion rate.

## Mandatory safety tests

Cases where OCR sees only:

```text
NAPA
```

must not automatically collapse into a specific strength/formulation if multiple products remain plausible.

## Results

_To be filled._

## Conclusion

_To be filled._

## Decision

_To be filled._

---

# 13. EXP-004 — Barcode Contribution

**Status:** PLANNED

## Question

How frequently do usable machine-readable codes exist on the target medicine packaging, and how much do they improve product resolution?

## Hypothesis

Barcodes will be highly useful when a trusted mapping exists, but coverage/mapping availability will prevent them from being the sole recognition strategy.

## Procedure

For each dataset item:

- record whether a code exists;
- whether ML Kit can decode it;
- whether it maps to a canonical product;
- whether it resolves an OCR ambiguity.

## Metrics

- code-present rate;
- decode-success rate;
- trusted-mapping rate;
- unique-resolution contribution;
- conflicting-code incidents.

## Results

_To be filled._

## Conclusion

_To be filled._

## Decision

_To be filled._

---

# 14. EXP-005 — High-Confidence / Abstention Policy v0

**Status:** PLANNED

## Question

Can the matcher distinguish “safe to present as leading candidate” from “must show ambiguity/retry”?

## Hypothesis

A policy that considers score margin, strength/form conflicts, and evidence completeness will sharply reduce false confident matches at the cost of some abstention.

## Inputs

EXP-003 candidate outputs.

## Policy candidates

Examples:

- minimum evidence count;
- minimum top score;
- minimum margin from candidate #2;
- no unresolved strength conflict;
- no barcode conflict;
- image-quality gate.

These are hypotheses, not final rules.

## Metrics

- high-confidence precision;
- false confident matches;
- abstention;
- correct-single-candidate coverage;
- user-selection burden proxy.

## Results

_To be filled._

## Conclusion

_To be filled._

## Decision

_To be filled._

---

# 15. EXP-006 — OpenCV Preprocessing: Only After Failure Analysis

**Status:** BLOCKED UNTIL EXP-001 FAILURE ANALYSIS

## Rule

Do not run a random bundle of image filters.

Create one sub-experiment per measured failure.

Possible examples:

### EXP-006A — CLAHE for low contrast

Hypothesis:
CLAHE improves OCR field recall on low-contrast reflective foil without materially harming normal images.

### EXP-006B — Deskew / perspective correction

Hypothesis:
Perspective correction improves text extraction for oblique packaging photos.

### EXP-006C — Blur quality gate

Hypothesis:
Rejecting severely blurred frames and requesting recapture improves safety more than attempting to sharpen them.

Each must compare to the unchanged raw baseline.

---

# 16. Experiment result template

Copy this section for new experiments:

```markdown
## EXP-XXX — Title

**Status:** PLANNED | RUNNING | COMPLETED | REJECTED

### Question
...

### Hypothesis
...

### Dataset/version
...

### Baseline
...

### Change
...

### Procedure
1. ...
2. ...

### Metrics
- ...

### Results
...

### Failure analysis
...

### Conclusion
...

### Decision / action
...
```

---

# 17. Experiment integrity rules

- Never delete a bad result because it is inconvenient.
- Keep dataset versions identifiable.
- Do not tune on the held-out test set repeatedly.
- Record dependency/model-version changes.
- If OCR/library behavior changes after an SDK upgrade, rerun relevant benchmarks.
- Separate “looks better” from “measures better.”
- Treat systematic strength/formulation confusion as a blocker even if aggregate accuracy looks good.
- A lower overall success rate can be acceptable if it meaningfully reduces false confident identification.

---

# 18. First report we want

At the end of Phase 1, summarize:

```text
Dataset
-------
products:
images:
package breakdown:

OCR
---
brand usable:
generic usable:
strength usable:
manufacturer usable:
unusable:

Matcher
-------
top-1:
top-3:
high-confidence precision:
false confident matches:
abstention:

Performance
-----------
OCR latency:
matching latency:
end-to-end:

Failure categories
------------------
glare:
blur:
partial strip:
rotation:
script:
variant confusion:
catalog missing:

Recommendation
--------------
GREEN / YELLOW / RED
reason:
next experiment:
```

Do not fill these values until measured.

---

# 19. Standing experiment rule

> **If we cannot state what changed, what was measured, and what decision the result supports, it is not an experiment — it is tinkering.**
