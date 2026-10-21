"""Check source whitespace without rewriting user files or downloading a formatter."""
from pathlib import Path
import subprocess

repo = Path(__file__).resolve().parent.parent
tracked = subprocess.check_output(["git", "ls-files", "-z"], cwd=repo).decode().split("\0")
extensions = {".kt", ".kts", ".xml", ".yml", ".yaml", ".py", ".ps1"}
errors = []
checked = 0
for name in tracked:
    path = repo / name
    if not name or path.suffix not in extensions or not path.is_file():
        continue
    checked += 1
    text = path.read_text(encoding="utf-8-sig")
    if text and not text.endswith("\n"):
        errors.append(f"{name}: missing final newline")
    for number, line in enumerate(text.splitlines(), 1):
        if line != line.rstrip():
            errors.append(f"{name}:{number}: trailing whitespace")
if errors:
    raise SystemExit("\n".join(errors))
print(f"Source whitespace checks passed ({checked} tracked source files).")
