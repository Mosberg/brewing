#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Dict, Iterator

try:
    from jsonschema import Draft202012Validator
    from referencing import Registry, Resource
except Exception as e:  # pragma: no cover
    Draft202012Validator = None  # type: ignore[assignment]
    Registry = None  # type: ignore[assignment]
    Resource = None  # type: ignore[assignment]
    _JSONSCHEMA_IMPORT_ERROR = e
else:
    _JSONSCHEMA_IMPORT_ERROR = None

REPO_ROOT = Path(__file__).resolve().parents[1]
DATA_ROOT = REPO_ROOT / "src" / "main" / "resources" / "data" / "brewing"
SCHEMAS_DIR = DATA_ROOT / "schemas"

SCHEMA_BY_FOLDER = {
    "beverages": "beverages-schema.json",
    "containers": "containers-schema.json",
    "equipment": "equipment-schema.json",
    "ingredients": "ingredients-schema.json",
    "methods": "methods-schema.json",
    "alcohol_types": "alcohol-types-schema.json",
}

def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))

def schema_store() -> Dict[str, dict]:
    store: Dict[str, dict] = {}
    for p in SCHEMAS_DIR.glob("*.json"):
        doc = load_json(p)
        # Store by $id if present, and by filename for local refs like "common-schema.json"
        if isinstance(doc, dict) and "$id" in doc:
            store[doc["$id"]] = doc
        store[p.name] = doc
    return store

def _registry_for(store: Dict[str, dict]) -> Registry:
    registry: Registry = Registry()  # type: ignore[no-redef]
    for uri, doc in store.items():
        resource = Resource.from_contents(doc)  # type: ignore[no-redef]
        registry = registry.with_resource(uri, resource)
    return registry


def validator_for(schema_filename: str, store: Dict[str, dict]) -> Draft202012Validator:
    schema_path = SCHEMAS_DIR / schema_filename
    schema = load_json(schema_path)
    registry = _registry_for(store)
    return Draft202012Validator(schema, registry=registry)

def iter_content_files() -> Iterator[tuple[str, Path]]:
    for folder, schema_name in SCHEMA_BY_FOLDER.items():
        base = DATA_ROOT / folder
        if not base.exists():
            continue
        for p in base.rglob("*.json"):
            # Skip schema files and docs accidentally placed under data/
            if p.name.endswith("-schema.json") or p.name == "containers.md":
                continue
            yield schema_name, p

def main() -> int:
    parser = argparse.ArgumentParser(description="Validate brewing data JSON against JSON Schemas")
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Print [OK] lines for validated files.",
    )
    args = parser.parse_args()

    if (
        _JSONSCHEMA_IMPORT_ERROR is not None
        or Draft202012Validator is None
        or Registry is None
        or Resource is None
    ):
        print("Missing dependency: jsonschema")
        print("Install with: pip install -r tools/requirements.txt")
        print(f"Import error: {_JSONSCHEMA_IMPORT_ERROR}")
        return 2

    if not DATA_ROOT.exists():
        print(f"Missing data root: {DATA_ROOT}")
        return 2
    if not SCHEMAS_DIR.exists():
        print(f"Missing schemas dir: {SCHEMAS_DIR}")
        return 2

    store = schema_store()

    validators = {}
    for schema_name in set(SCHEMA_BY_FOLDER.values()):
        validators[schema_name] = validator_for(schema_name, store)

    errors = 0
    for schema_name, path in iter_content_files():
        try:
            data = load_json(path)
        except Exception as e:
            errors += 1
            print(f"[INVALID JSON] {path}: {e}")
            continue

        v = validators[schema_name]
        file_errors = sorted(v.iter_errors(data), key=lambda e: list(e.absolute_path))
        if file_errors:
            errors += 1
            print(f"[SCHEMA FAIL] {path} (schema={schema_name})")
            for e in file_errors[:50]:
                loc = "/" + "/".join(map(str, e.absolute_path))
                print(f"  - {loc}: {e.message}")
            if len(file_errors) > 50:
                print(f"  - ... {len(file_errors) - 50} more")
        else:
            if args.verbose:
                print(f"[OK] {path}")

    if errors:
        print(f"\nFAILED: {errors} file(s) did not validate.")
        return 1

    print("\nSUCCESS: all files validated.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
