"""Preserve CI files in an unpublished draft release when Actions storage is unavailable."""
import argparse
import json
import os
from pathlib import Path
import time
import urllib.error
import urllib.parse
import urllib.request
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("kind", choices=("verify", "emulator", "model"))
args = parser.parse_args()
if os.environ.get("GITHUB_EVENT_NAME") != "push" or os.environ.get("GITHUB_REF") != "refs/heads/codex/veil-mvp":
    raise SystemExit("Draft asset delivery is restricted to pushes on codex/veil-mvp.")
token = os.environ["GH_TOKEN"]
repo = os.environ["GITHUB_REPOSITORY"]
sha = os.environ["GITHUB_SHA"]
base = f"https://api.github.com/repos/{repo}"
headers = {"Authorization": f"Bearer {token}", "Accept": "application/vnd.github+json", "X-GitHub-Api-Version": "2022-11-28"}


def request(url, payload=None, content_type="application/json"):
    body = json.dumps(payload).encode() if isinstance(payload, dict) else payload
    req = urllib.request.Request(url, data=body, headers={**headers, "Content-Type": content_type})
    with urllib.request.urlopen(req, timeout=180) as response:
        return json.load(response)


if not request(base)["private"]:
    raise SystemExit("Refusing CI draft asset delivery outside the private repository.")
tag = f"ci-{sha}"


def find_release():
    page = 1
    while True:
        releases = request(f"{base}/releases?per_page=100&page={page}")
        for candidate in releases:
            if candidate["tag_name"] == tag:
                return candidate
        if len(releases) < 100:
            return None
        page += 1


release = None
for attempt in range(5):
    release = find_release()
    if not release:
        try:
            release = request(f"{base}/releases", {"tag_name": tag, "target_commitish": sha, "name": f"Veil experimental build {sha[:12]}", "body": "Automated CI testing files for the exact source commit. Draft only; not a published release or production certification.", "draft": True, "prerelease": True})
        except urllib.error.HTTPError as create_error:
            if create_error.code != 422:
                raise
    if release:
        break
    time.sleep(2)
if not release or not release["draft"] or release["target_commitish"] != sha:
    raise SystemExit("A draft release for this exact commit could not be established.")

output = Path("work/ci-delivery")
output.mkdir(parents=True, exist_ok=True)
patterns = {
    "verify": ["app/build/reports/**/*", "app/build/test-results/**/*", "app/build/intermediates/merged_manifests/debug/**/*", "app/build/outputs/apk/**/*.apk"],
    "emulator": ["app/build/reports/androidTests/**/*", "app/build/outputs/androidTest-results/**/*", "emulator.log", "veil-emulator.png"],
    "model": ["work/ci-model-smoke.json"],
}[args.kind]
files = sorted({path for pattern in patterns for path in Path(".").glob(pattern) if path.is_file()})
if not files:
    raise SystemExit(f"No {args.kind} files exist to preserve.")
archive = output / f"veil-{args.kind}-evidence.zip"
with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in files:
        bundle.write(path, str(path))
assets = [archive]
if args.kind == "verify":
    assets.extend(Path("app/build/outputs/apk/debug").glob("*.apk"))
    assets.extend(Path("app/build/outputs/apk/release").glob("*.apk"))
upload_url = release["upload_url"].split("{")[0]
existing = {asset["name"]: asset for asset in request(f"{base}/releases/{release['id']}/assets")}
for path in assets:
    if path.name in existing:
        req = urllib.request.Request(f"{base}/releases/assets/{existing[path.name]['id']}", headers=headers, method="DELETE")
        with urllib.request.urlopen(req, timeout=60):
            pass
    asset = request(f"{upload_url}?name={urllib.parse.quote(path.name)}", path.read_bytes(), "application/octet-stream")
    print(f"Preserved draft asset: {asset['name']} ({asset['size']} bytes)")
print(f"Unpublished draft build: {release['html_url']}")
