#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

COMMON_FILE = "common-schema.json"

# Only refactor defs that are semantically identical across schemas.
SHARED_DEFS = {
    "Identifier",
    "IdentifierOrVanilla",
    "TagId",
    "Rarity",
    "TemperaturePreset",
    "EventList",
    "EventAction",
}

def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))

def save_json(path: Path, obj):
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

def walk(obj, fn):
    if isinstance(obj, dict):
        fn(obj)
        for v in obj.values():
            walk(v, fn)
    elif isinstance(obj, list):
        for v in obj:
            walk(v, fn)

def add_extension_keys(schema: dict):
    schema.setdefault("patternProperties", {})
    schema["patternProperties"]["^x-"] = {
        "type": ["object", "array", "string", "number", "integer", "boolean", "null"]
    }
    schema["patternProperties"]["^_debug$"] = {
        "type": "object",
        "additionalProperties": True
    }

def replace_shared_refs(schema: dict, common_ref: str):
    def _fn(d: dict):
        ref = d.get("$ref")
        if isinstance(ref, str) and ref.startswith("#/$defs/"):
            name = ref.split("/")[-1]
            if name in SHARED_DEFS:
                d["$ref"] = f"{common_ref}#/$defs/{name}"
    walk(schema, _fn)

    defs = schema.get("$defs")
    if isinstance(defs, dict):
        for k in list(defs.keys()):
            if k in SHARED_DEFS:
                defs.pop(k, None)

def patch_beverage_conditionals(schema: dict):
    defs = schema.setdefault("$defs", {})

    common_identifier_ref = f"{COMMON_FILE}#/$defs/Identifier"

    # Failure
    if "Failure" in defs:
        defs["Failure"] = {
            "oneOf": [
                {
                    "type": "object",
                    "additionalProperties": False,
                    "required": ["enabled"],
                    "properties": { "enabled": { "const": False } }
                },
                {
                    "type": "object",
                    "additionalProperties": False,
                    "required": ["enabled", "base_fail_chance", "outcome"],
                    "properties": {
                        "enabled": { "const": True },
                        "base_fail_chance": { "type": "number", "minimum": 0, "maximum": 1 },
                        "outcome": { "$ref": common_identifier_ref },
                        "extra_effects": {
                            "type": "array",
                            "items": { "$ref": "#/$defs/StatusEffect" },
                            "default": []
                        }
                    }
                }
            ]
        }

    # Aging
    if "Aging" in defs:
        defs["Aging"] = {
            "oneOf": [
                {
                    "type": "object",
                    "additionalProperties": False,
                    "required": ["supported"],
                    "properties": { "supported": { "const": False } }
                },
                {
                    "type": "object",
                    "additionalProperties": False,
                    "required": ["supported", "min_days", "max_days"],
                    "properties": {
                        "supported": { "const": True },
                        "min_days": { "type": "number", "minimum": 0 },
                        "max_days": { "type": "number", "minimum": 0 },
                        "preferred_container_tags": {
                            "type": "array",
                            "items": { "type": "string" },
                            "uniqueItems": True
                        },
                        "quality_bonus_per_day": { "type": "number", "minimum": 0 },
                        "risk_per_day_open": { "type": "number", "minimum": 0 }
                    }
                }
            ]
        }

    # Spoilage
    if "Spoilage" in defs:
        defs["Spoilage"] = {
            "oneOf": [
                {
                    "type": "object",
                    "additionalProperties": False,
                    "required": ["enabled"],
                    "properties": { "enabled": { "const": False } }
                },
                {
                    "type": "object",
                    "additionalProperties": False,
                    "required": ["enabled", "base_decay_per_day"],
                    "properties": {
                        "enabled": { "const": True },
                        "base_decay_per_day": { "type": "number", "minimum": 0 },
                        "opened_decay_multiplier": { "type": "number", "minimum": 0 },
                        "temperature": {
                            "type": "object",
                            "additionalProperties": False,
                            "properties": {
                                "preferred": { "$ref": f"{COMMON_FILE}#/$defs/TemperaturePreset" },
                                "hot_decay_multiplier": { "type": "number", "minimum": 0 },
                                "cold_decay_multiplier": { "type": "number", "minimum": 0 }
                            }
                        }
                    }
                }
            ]
        }

def patch_container_burst(schema: dict):
    defs = schema.get("$defs")
    if not isinstance(defs, dict):
        return

    # Containers schema commonly defines Pressure inline as $defs.Pressure.
    pressure = defs.get("Pressure")
    if not isinstance(pressure, dict):
        return

    props = pressure.get("properties")
    if not isinstance(props, dict) or "burst" not in props:
        return

    props["burst"] = {
        "oneOf": [
            {
                "type": "object",
                "additionalProperties": False,
                "required": ["enabled"],
                "properties": { "enabled": { "const": False } }
            },
            {
                "type": "object",
                "additionalProperties": False,
                "required": ["enabled", "pressure_threshold", "drop_contents_on_burst", "burst_sound"],
                "properties": {
                    "enabled": { "const": True },
                    "pressure_threshold": { "type": "number", "minimum": 0 },
                    "drop_contents_on_burst": { "type": "boolean" },
                    "burst_sound": { "type": "string", "minLength": 1 },
                    "burst_particles": { "type": "array", "items": { "type": "string" } }
                }
            }
        ]
    }

def main():
    repo_root = Path(__file__).resolve().parents[1]
    schemas_dir = repo_root / "src" / "main" / "resources" / "data" / "brewing" / "schemas"

    if len(sys.argv) == 2:
        schemas_dir = Path(sys.argv[1]).resolve()

    if not schemas_dir.exists():
        raise SystemExit(f"Schema directory does not exist: {schemas_dir}")

    # Ensure common schema exists (this script does not generate it).
    common_path = schemas_dir / COMMON_FILE
    if not common_path.exists():
        raise SystemExit(f"Missing {COMMON_FILE} at: {common_path}")

    schema_files = [
        "alcohol-types-schema.json",
        "beverages-schema.json",
        "containers-schema.json",
        "equipment-schema.json",
        "ingredients-schema.json",
        "methods-schema.json",
    ]

    for name in schema_files:
        path = schemas_dir / name
        if not path.exists():
            print(f"Skip missing: {name}")
            continue

        s = load_json(path)

        add_extension_keys(s)
        replace_shared_refs(s, COMMON_FILE)

        if name == "beverages-schema.json":
            patch_beverage_conditionals(s)
        if name == "containers-schema.json":
            patch_container_burst(s)

        save_json(path, s)
        print(f"Updated: {path}")

if __name__ == "__main__":
    main()
