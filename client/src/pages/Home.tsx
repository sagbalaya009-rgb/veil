/** Calm Minimal Veil: image-free menu, local-only reset flow, and a compact persisted theme toggle. */
import { useEffect, useState } from "react";
import { Check, ChevronLeft, CirclePause, Coffee, Dumbbell, Footprints, Moon, PenLine, Shield, StretchHorizontal, Sun, TimerReset, Waves } from "lucide-react";

type View = "home" | "reset" | "active" | "note";

const durations = ["2 min", "5 min", "10 min", "15 min", "30 min", "45 min", "1 hour"];
const resetOptions = [
  { name: "Push-ups", detail: "10 gentle reps", icon: Dumbbell },
  { name: "Breathing", detail: "2 minutes", icon: Waves },
  { name: "Walk", detail: "5 minutes", icon: Footprints },
  { name: "Journal", detail: "One honest line", icon: PenLine },
  { name: "Water", detail: "Fill a glass", icon: Coffee },
  { name: "Stretch", detail: "3 slow minutes", icon: StretchHorizontal },
  { name: "Offline pause", detail: "Put the phone down", icon: CirclePause },
];

const durationToSeconds: Record<string, number> = {
  "2 min": 120,
  "5 min": 300,
  "10 min": 600,
  "15 min": 900,
  "30 min": 1800,
  "45 min": 2700,
  "1 hour": 3600,
};

type SavedReset = { endsAt: number; duration: string; resetName: string };

const formatRemaining = (seconds: number) => {
  const safeSeconds = Math.max(0, seconds);
  const minutes = Math.floor(safeSeconds / 60);
  const remainder = safeSeconds % 60;
  return `${minutes}:${remainder.toString().padStart(2, "0")}`;
};

export default function Home() {
  const [view, setView] = useState<View>("home");
  const [duration, setDuration] = useState(durations[1]);
  const [reset, setReset] = useState(resetOptions[0]);
  const [note, setNote] = useState("");
  const [saved, setSaved] = useState(false);
  const [dark, setDark] = useState(false);
  const [endsAt, setEndsAt] = useState<number | null>(null);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [completed, setCompleted] = useState(false);

  useEffect(() => {
    setDark(window.localStorage.getItem("veil-theme") === "dark");
    const savedReset = window.localStorage.getItem("veil-active-reset");
    if (!savedReset) return;

    try {
      const parsed = JSON.parse(savedReset) as SavedReset;
      const resetOption = resetOptions.find((option) => option.name === parsed.resetName);
      if (parsed.endsAt > Date.now() && resetOption) {
        setDuration(parsed.duration);
        setReset(resetOption);
        setEndsAt(parsed.endsAt);
        setRemainingSeconds(Math.ceil((parsed.endsAt - Date.now()) / 1000));
        setView("active");
      } else {
        window.localStorage.removeItem("veil-active-reset");
      }
    } catch {
      window.localStorage.removeItem("veil-active-reset");
    }
  }, []);

  useEffect(() => {
    if (!endsAt) return;

    const updateRemaining = () => {
      const nextRemaining = Math.max(0, Math.ceil((endsAt - Date.now()) / 1000));
      setRemainingSeconds(nextRemaining);
      if (nextRemaining === 0) {
        setEndsAt(null);
        setCompleted(true);
        window.localStorage.removeItem("veil-active-reset");
      }
    };

    updateRemaining();
    const intervalId = window.setInterval(updateRemaining, 1000);
    return () => window.clearInterval(intervalId);
  }, [endsAt]);

  const toggleTheme = () => {
    setDark((current) => {
      const next = !current;
      window.localStorage.setItem("veil-theme", next ? "dark" : "light");
      return next;
    });
  };

  const beginReset = () => {
    const nextEnd = Date.now() + durationToSeconds[duration] * 1000;
    const resetToSave: SavedReset = { endsAt: nextEnd, duration, resetName: reset.name };
    window.localStorage.setItem("veil-active-reset", JSON.stringify(resetToSave));
    setEndsAt(nextEnd);
    setRemainingSeconds(durationToSeconds[duration]);
    setCompleted(false);
    setView("active");
  };

  const completeReset = () => {
    window.localStorage.removeItem("veil-active-reset");
    setEndsAt(null);
    setRemainingSeconds(0);
    setCompleted(true);
  };

  const changePlan = () => {
    window.localStorage.removeItem("veil-active-reset");
    setEndsAt(null);
    setRemainingSeconds(0);
    setCompleted(false);
    setView("reset");
  };

  return (
    <main className={`veil-app ${dark ? "is-dark" : ""}`}>
      <div className="veil-layout">
        <header className="veil-header">
          <button onClick={() => setView("home")} className="veil-brand" aria-label="Veil home">
            <span className="veil-mark"><Shield size={16} /></span><span>VEIL</span>
          </button>
          <nav className="veil-menu" aria-label="Veil menu">
            <button onClick={() => setView("home")} className={view === "home" ? "selected" : ""}>Home</button>
            <button onClick={() => setView("reset")} className={view === "reset" || view === "active" ? "selected" : ""}>Reset</button>
            <button onClick={() => setView("note")} className={view === "note" ? "selected" : ""}>Note</button>
          </nav>
          <button className="veil-theme-toggle" onClick={toggleTheme} aria-label={dark ? "Use light mode" : "Use dark mode"} title={dark ? "Use light mode" : "Use dark mode"}>
            {dark ? <Sun size={16} /> : <Moon size={16} />}
          </button>
        </header>

        <section className="veil-content">
          {view === "home" && (
            <div className="veil-home">
              <p className="veil-eyebrow">Private reset space</p>
              <h1>What would help right now?</h1>
              <p className="veil-intro">Choose a small reset. Veil does not watch your device or send your activity anywhere.</p>
              <div className="veil-actions">
                <button onClick={() => setView("reset")} className="veil-action veil-action-primary"><TimerReset size={21} /><strong>Start a reset</strong><span>Pick time and one helpful action.</span></button>
                <button onClick={() => setView("note")} className="veil-action"><PenLine size={21} /><strong>Write a note</strong><span>Leave one line for yourself.</span></button>
              </div>
            </div>
          )}

          {view === "reset" && (
            <div className="veil-flow">
              <button onClick={() => setView("home")} className="veil-back"><ChevronLeft size={16} /> Back</button>
              <p className="veil-eyebrow">Reset</p><h2>Make a small plan.</h2>
              <div className="veil-step"><p>How long do you want this reset to last?</p><div className="veil-choice-row">{durations.map((item) => <button key={item} onClick={() => setDuration(item)} className={duration === item ? "selected" : ""}>{item}</button>)}</div></div>
              <div className="veil-step"><p>Choose one reset.</p><div className="veil-options">{resetOptions.map((option) => { const Icon = option.icon; const selected = reset.name === option.name; return <button key={option.name} onClick={() => setReset(option)} className={selected ? "selected" : ""}><Icon size={17} /><span><strong>{option.name}</strong><small>{option.detail}</small></span></button>; })}</div></div>
              <div className="veil-flow-footer"><span>{duration} · {reset.name}</span><button onClick={beginReset} className="veil-primary-button">Begin reset</button></div>
            </div>
          )}

          {view === "active" && (
            <div className="veil-flow veil-active">
              {completed ? (
                <>
                  <p className="veil-eyebrow">Reset complete</p><h2>You followed through.</h2>
                  <div className="veil-status veil-status-complete"><p>Good choice</p><strong>Your pause is complete.</strong><span>Take the steadier next step when you are ready.</span></div>
                  <div className="veil-button-row"><button onClick={() => { setCompleted(false); setView("home"); }} className="veil-primary-button">Return home</button><button onClick={() => { setCompleted(false); setView("reset"); }} className="veil-secondary-button">Start another</button></div>
                </>
              ) : (
                <>
                  <p className="veil-eyebrow">Reset started</p><h2>Good call. You chose a pause.</h2>
                  <p className="veil-supporting-copy">Your {duration} {reset.name.toLowerCase()} reset has begun. You made a thoughtful choice for yourself.</p>
                  <div className="veil-status"><p>Time remaining</p><strong className="veil-countdown" aria-live="polite">{formatRemaining(remainingSeconds)}</strong><span>{reset.name} · {reset.detail}</span></div>
                  <p className="veil-supporting-copy">This reset stays active if you refresh the page. When you are ready, mark it complete or adjust the plan.</p>
                  <div className="veil-button-row"><button onClick={completeReset} className="veil-primary-button">I’m done</button><button onClick={changePlan} className="veil-secondary-button">Change plan</button></div>
                </>
              )}
            </div>
          )}

          {view === "note" && (
            <div className="veil-flow">
              <button onClick={() => setView("home")} className="veil-back"><ChevronLeft size={16} /> Back</button>
              <p className="veil-eyebrow">A note for yourself</p><h2>What do you want to remember?</h2>
              <textarea value={note} onChange={(event) => { setNote(event.target.value); setSaved(false); }} placeholder="One line is enough." className="veil-textarea" />
              <div className="veil-flow-footer"><span>Stored only in this browser session.</span><button onClick={() => note.trim() && setSaved(true)} disabled={!note.trim()} className="veil-primary-button">{saved ? <><Check size={15} /> Saved</> : "Save note"}</button></div>
            </div>
          )}
        </section>

        <footer className="veil-footer">No tracking · no screen capture · no device monitoring</footer>
      </div>
    </main>
  );
}
