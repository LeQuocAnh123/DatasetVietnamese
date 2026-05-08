# Vietnamese Spelling Dataset Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Python generator that analyzes Vietnamese syllables into SGK-style spelling records, exports a JSON schema, and produces a verified sample JSONL dataset.

**Architecture:** Keep the implementation in one focused Python module so parsing logic, dataset generation, and unit tests stay together. Use rule-based onset/tone/rime analysis with a small explicit exception table, then generate the schema and sample output from the same canonical code path to avoid drift.

**Tech Stack:** Python 3 standard library (`json`, `hashlib`, `pathlib`, `unicodedata`, `unittest`)

---

### Task 1: Scaffold the test-first dataset module

**Files:**
- Create: `generate_dataset.py`

- [ ] **Step 1: Write the failing tests for the public API and representative words**

Add `unittest` coverage for:
- `phan_tich_am_tiet(tu)` returning the required fields
- `tao_dataset(danh_sach_tu)` returning record dictionaries
- `xuat_json(records, output_path)` writing valid JSONL
- exact `danh_van` strings for at least 3 words
- at least 2 exception-driven words

- [ ] **Step 2: Run the test module to verify it fails**

Run: `python -m unittest generate_dataset -v`
Expected: FAIL because functions and parsing logic are not implemented yet

- [ ] **Step 3: Write the minimal production scaffolding**

Add:
- module constants for onset readings, tone maps, lesson groups, and sample words
- placeholder implementations for the three public functions and helper boundaries
- `if __name__ == "__main__"` entrypoint for generating `sample_output.jsonl`

- [ ] **Step 4: Run the targeted tests again**

Run: `python -m unittest generate_dataset -v`
Expected: still FAIL, but now due to missing behavior rather than missing symbols

- [ ] **Step 5: Commit the scaffold**

```bash
git add generate_dataset.py
git commit -m "test: scaffold Vietnamese spelling dataset module"
```

### Task 2: Implement syllable parsing and pedagogical formatting

**Files:**
- Modify: `generate_dataset.py`

- [ ] **Step 1: Write failing tests for tone stripping, onset parsing, and lesson metadata**

Cover:
- no-onset words such as `em`, `oanh`
- special onsets `gi`, `qu`, `gh`, `ngh`
- final consonant cases such as `học`, `cắt`
- difficulty cascade and multi-label `nhom_bai_hoc`

- [ ] **Step 2: Run the focused tests to verify the new cases fail**

Run: `python -m unittest generate_dataset.TestPhanTichAmTiet -v`
Expected: FAIL on the newly added assertions

- [ ] **Step 3: Implement the minimal parser to satisfy the tests**

Implement helpers for:
- Unicode normalization and tone detection
- stripping tone marks while preserving Vietnamese letter identity
- longest-prefix onset detection
- rime decomposition into `am_dem`, `am_chinh`, `am_cuoi`
- `danh_van` assembly using the approved Full SGK template
- exception overrides and deterministic `do_kho` / `nhom_bai_hoc`

- [ ] **Step 4: Re-run the parsing tests**

Run: `python -m unittest generate_dataset.TestPhanTichAmTiet -v`
Expected: PASS

- [ ] **Step 5: Commit the parser implementation**

```bash
git add generate_dataset.py
git commit -m "feat: implement Vietnamese syllable parsing rules"
```

### Task 3: Generate dataset records, schema, and sample output

**Files:**
- Modify: `generate_dataset.py`
- Create: `schema.json`
- Create: `sample_output.jsonl`

- [ ] **Step 1: Write failing tests for dataset export and schema-aligned record structure**

Add assertions for:
- required top-level keys `linguistics`, `pedagogy`, `metadata`
- `source` enum values and `ngoai_le` propagation
- JSONL export round-trip

- [ ] **Step 2: Run the export-focused tests to confirm they fail**

Run: `python -m unittest generate_dataset.TestDatasetOutput -v`
Expected: FAIL on record/export expectations

- [ ] **Step 3: Implement record assembly and export helpers**

Add:
- stable record ids
- record version/source defaults
- sample word list with about 20 varied words
- code path to emit `sample_output.jsonl`
- schema document generation content for `schema.json`

- [ ] **Step 4: Run the full test suite**

Run: `python -m unittest generate_dataset -v`
Expected: PASS

- [ ] **Step 5: Generate the deliverables**

Run:
- `python generate_dataset.py`

Expected:
- `sample_output.jsonl` created/updated
- `schema.json` created/updated

- [ ] **Step 6: Commit the deliverables**

```bash
git add generate_dataset.py schema.json sample_output.jsonl
git commit -m "feat: generate Vietnamese spelling dataset artifacts"
```

### Task 4: Final verification

**Files:**
- Verify: `generate_dataset.py`
- Verify: `schema.json`
- Verify: `sample_output.jsonl`

- [ ] **Step 1: Run the full verification commands**

Run:
- `python -m unittest generate_dataset -v`
- `python generate_dataset.py`

Expected:
- all tests pass
- artifact generation succeeds without errors

- [ ] **Step 2: Inspect the generated sample output**

Run:
- `python - <<'PY'`
- inspect first records or line count from `sample_output.jsonl`
- `PY`

Expected:
- 20 JSONL records with the expected structure

- [ ] **Step 3: Review git diff**

Run: `git diff --stat HEAD~1..HEAD`
Expected: only the intended files changed
