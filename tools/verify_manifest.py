"""Verify privacy properties of the actual merged debug manifest after an Android build."""
from pathlib import Path
import xml.etree.ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"
repo = Path(__file__).resolve().parent.parent
manifests = list((repo / "app/build/intermediates/merged_manifests/debug").rglob("AndroidManifest.xml"))
if len(manifests) != 1:
    raise SystemExit(f"Expected one merged debug manifest, found {len(manifests)}. Build first.")
manifest = ET.parse(manifests[0]).getroot()
permissions = {node.get(ANDROID + "name") for node in manifest.findall("uses-permission")}
forbidden = {
    "android.permission.INTERNET",
    "android.permission.QUERY_ALL_PACKAGES",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
    "android.permission.READ_CONTACTS",
    "android.permission.ACCESS_FINE_LOCATION",
}
errors = [f"Forbidden permission: {permission}" for permission in sorted(permissions & forbidden)]
app = manifest.find("application")
if app is None:
    raise SystemExit("Merged manifest has no application.")
for attribute in ("allowBackup", "usesCleartextTraffic"):
    if app.get(ANDROID + attribute) != "false":
        errors.append(f"application {attribute} must be false")
if app.get(ANDROID + "dataExtractionRules") != "@xml/data_extraction_rules":
    errors.append("Explicit cloud-backup and device-transfer exclusion rules are required")
rules = ET.parse(repo / "app/src/main/res/xml/data_extraction_rules.xml").getroot()
for transport in ("cloud-backup", "device-transfer"):
    node = rules.find(transport)
    excluded = set() if node is None else {entry.get("domain") for entry in node.findall("exclude") if entry.get("path") == "."}
    if not {"root", "file", "database", "sharedpref", "external"}.issubset(excluded):
        errors.append(f"Incomplete {transport} private-data exclusions")
for service in app.findall("service"):
    if service.get(ANDROID + "exported") == "true" and service.get(ANDROID + "permission") != "android.permission.BIND_ACCESSIBILITY_SERVICE":
        errors.append(f"Unprotected exported service: {service.get(ANDROID + 'name')}")
if errors:
    raise SystemExit("\n".join(errors))
print(f"Merged manifest privacy checks passed: {manifests[0].relative_to(repo)}")
print("Declared permissions: " + ", ".join(sorted(permissions)))
