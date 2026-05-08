import argparse
import json
import sys
from pathlib import Path

from generate_dataset import validate_records

TRACKING_AM_CHINH = {"uô", "ươ", "iê"}


def load_records(path):
    return [json.loads(line) for line in Path(path).read_text(encoding="utf-8").splitlines() if line.strip()]


def collect_tracking_notes(records):
    notes = []
    for record in records:
        word = record["metadata"]["word"]
        am_chinh = record["linguistics"]["am_chinh"]
        if am_chinh in TRACKING_AM_CHINH:
            notes.append(
                {
                    "word": word,
                    "field": "linguistics.am_chinh",
                    "current": am_chinh,
                    "note": "Theo doi nhom nguyen am doi uô/ươ/iê theo quy uoc SGK khi scale dataset.",
                }
            )
    return notes


def main():
    parser = argparse.ArgumentParser(description="Validate Vietnamese spelling dataset JSONL.")
    parser.add_argument("input_path", help="Path to input .jsonl file")
    args = parser.parse_args()

    records = load_records(args.input_path)
    errors = validate_records(records)
    tracking_notes = collect_tracking_notes(records)

    print(f"records={len(records)}")
    print(f"errors={len(errors)}")
    for error in errors:
        print(
            json.dumps(
                {
                    "word": error["word"],
                    "field": error["field"],
                    "current": error["current"],
                    "expected": error["expected"],
                },
                ensure_ascii=False,
            )
        )

    print(f"tracking_notes={len(tracking_notes)}")
    for note in tracking_notes:
        print(json.dumps(note, ensure_ascii=False))

    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
