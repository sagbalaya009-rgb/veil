/** Calm Minimal Veil: image-free menu, persisted reset timing, opt-in completion notification, and local-only data. */
import { useEffect, useState } from "react";
import { Bell, Check, ChevronLeft, CirclePause, Coffee, Dumbbell, Footprints, Moon, PenLine, Shield, StretchHorizontal, Sun, TimerReset, Waves } from "lucide-react";

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
  "2 min": 120, "5 min": 300, "10 min": 600, "15 min": 900,
  "30 min": 1800, "45 min": 2700, "1 hour": 3600,
};

type SavedReset = { endsAt: number; duration: string; resetName: string };
const formatRemaining = (seconds: number) => {
  const safeSeconds = Math.max(0, seconds);
  return `${Math.floor(safeSeconds / 60)}:${(safeSeconds % 60).toString().padStart(2, "0")}`;
};

export default function Home() {
  const [view, setView] = useState<View>("home");
  const [duration, setDuration] = useState(durations[1]);
  const [reset, setReset] = useState(resetOptions[0]);
  const [note, setNote] = useState("");
  const [savedNote, setSavedNote] = useState("");
  const [saved, setSaved] = useState(false);
  const [dark, setDark] = useState(false);
  const [endsAt, setEndsAt] = useState<number | null>(null);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [completed, setCompleted] = useState(false);
  const [notificationsEnabled, setNotificationsEnabled] = useState(false);
  const [notificationPermission, setNotificationPermission] = useState<NotificationPermission | "unsupported">("unsupported");

  useEffect(() => {
    setDark(window.localStorage.getItem("veil-theme") === "dark");
    const recoveryNote = window.localStorage.getItem("veil-recovery-note") ?? "";
    setNote(recoveryNote);
    setSavedNote(recoveryNote);
    setSaved(Boolean(recoveryNote));
    if ("Notification" in window) {
      setNotificationPermission(Notification.permission);
      setNotificationsEnabled(Notification.permission === "granted" && window.localStorage.getItem("veil-reset-notifications") === "enabled");
    }

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
        if (notificationsEnabled && "Notification" in window && Notification.permission === "granted") {
          new Notification("Veil", { body: "Your reset is complete. You followed through.", silent: true });
        }
        setEndsAt(null);
        setCompleted(true);
        window.localStorage.removeItem("veil-active-reset");
      }
    };
    updateRemaining();
    const intervalId = window.setInterval(updateRemaining, 1000);
    return () => window.clearInterval(intervalId);
  }, [endsAt, notificationsEnabled]);

  const toggleTheme = () => setDark((current) => {
    const next = !current;
    window.localStorage.setItem("veil-theme", next ? "dark" : "light");
    return next;
  });

  const enableCompletionNotification = async () => {
    if (!("Notification" in window)) return setNotificationPermission("unsupported");
    if (Notification.permission === "denied") return setNotificationPermission("denied");
    const permission = Notification.permission === "granted" ? "granted" : await Notification.requestPermission();
    setNotificationPermission(permission);
    if (permission === "granted") {
      window.localStorage.setItem("veil-reset-notifications", "enabled");
      setNotificationsEnabled(true);
    }
  };

  const disableCompletionNotification = () => {
    window.localStorage.removeItem("veil-reset-notifications");
    setNotificationsEnabled(false);
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

  const notificationDescription = notificationPermission === "denied"
    ? "Notifications are blocked in this browser."
    : notificationPermission === "unsupported"
      ? "This browser does not support notifications."
      : "Optional. Only sent when this reset ends.";

  return (
    <main className={`veil-app ${dark ? "is-dark" : ""} ${view === "active" && !completed ? "is-cooldown" : ""}`}>
      <div className="veil-layout">
        <header className="veil-header">
          <button onClick={() => setView("home")} className="veil-brand" aria-label="Veil home"><span className="veil-mark"><Shield size={16} /></span><span>VEIL</span></button>
          <nav className="veil-menu" aria-label="Veil menu">
            <button onClick={() => setView("home")} className={view === "home" ? "selected" : ""}>Home</button>
            <button onClick={() => setView("reset")} className={view === "reset" || view === "active" ? "selected" : ""}>Reset</button>
            <button onClick={() => setView("note")} className={view === "note" ? "selected" : ""}>Note</button>
          </nav>
          <button className="veil-theme-toggle" onClick={toggleTheme} aria-label={dark ? "Use light mode" : "Use dark mode"} title={dark ? "Use light mode" : "Use dark mode"}>{dark ? <Sun size={16} /> : <Moon size={16} />}</button>
        </header>

        <section className="veil-content">
          {view === "home" && <div className="veil-home">
            <p className="veil-eyebrow">Private reset space</p><h1>What would help right now?</h1>
            <p className="veil-intro">Choose a small reset. Veil does not watch your device or send your activity anywhere.</p>
            <div className="veil-actions">
              <button onClick={() => setView("reset")} className="veil-action veil-action-primary"><TimerReset size={21} /><strong>Set your cooldown</strong><span>Pick time and one helpful action.</span></button>
              <button onClick={() => setView("note")} className="veil-action"><PenLine size={21} /><strong>Write a note</strong><span>Leave one line for yourself.</span></button>
            </div>
          </div>}

          {view === "reset" && <div className="veil-flow">
            <button onClick={() => setView("home")} className="veil-back"><ChevronLeft size={16} /> Back</button>
            <p className="veil-eyebrow">Cooldown plan</p><h2>Decide what will help.</h2>
            <div className="veil-step"><p>How long do you want this reset to last?</p><div className="veil-choice-row">{durations.map((item) => <button key={item} onClick={() => setDuration(item)} className={duration === item ? "selected" : ""}>{item}</button>)}</div></div>
            <div className="veil-step"><p>Choose one reset.</p><div className="veil-options">{resetOptions.map((option) => { const Icon = option.icon; return <button key={option.name} onClick={() => setReset(option)} className={reset.name === option.name ? "selected" : ""}><Icon size={17} /><span><strong>{option.name}</strong><small>{option.detail}</small></span></button>; })}</div></div>
            <div className="veil-notification-row"><div><Bell size={16} /><span><strong>Quiet completion notification</strong><small>{notificationDescription}</small></span></div>{notificationsEnabled ? <button onClick={disableCompletionNotification} className="veil-secondary-button">On</button> : <button onClick={enableCompletionNotification} disabled={notificationPermission === "denied" || notificationPermission === "unsupported"} className="veil-secondary-button">Turn on</button>}</div>
            <p className="veil-browser-disclaimer">Browser demonstration only: this button simulates a detection. A website cannot inspect other apps or blur your phone.</p>
            <div className="veil-flow-footer"><span>{duration} · {reset.name}</span><button onClick={beginReset} className="veil-primary-button">Test cooldown</button></div>
          </div>}

          {view === "active" && <div className="veil-flow veil-active">
            {completed ? <>
              <p className="veil-eyebrow">Reset complete</p><h2>You followed through.</h2>
              <div className="veil-status veil-status-complete"><p>Good choice</p><strong>Your pause is complete.</strong><span>Take the steadier next step when you are ready.</span></div>
              <div className="veil-button-row"><button onClick={() => { setCompleted(false); setView("home"); }} className="veil-primary-button">Return home</button><button onClick={() => { setCompleted(false); setView("reset"); }} className="veil-secondary-button">Start another</button></div>
            </> : <>
              <p className="veil-eyebrow">Cooldown started</p><h2>Good call. You chose a pause.</h2>
              <p className="veil-supporting-copy">Your {duration} {reset.name.toLowerCase()} cooldown has begun. You made a thoughtful choice for yourself.</p>
              <div className="veil-status"><p>Time remaining</p><strong className="veil-countdown" aria-live="polite">{formatRemaining(remainingSeconds)}</strong><span>{reset.name} · {reset.detail}</span></div>
              <p className="veil-supporting-copy">This reset stays active if you refresh the page. When you are ready, mark it complete or adjust the plan.</p>
              <div className="veil-button-row"><button onClick={completeReset} className="veil-primary-button">I’m done</button><button onClick={changePlan} className="veil-secondary-button">Change plan</button></div>
            </>}
          </div>}

          {view === "note" && <div className="veil-flow">
            <button onClick={() => setView("home")} className="veil-back"><ChevronLeft size={16} /> Back</button>
            <p className="veil-eyebrow">A note for yourself</p><h2>What do you want to remember?</h2>
            <textarea value={note} onChange={(event) => { setNote(event.target.value); setSaved(false); }} placeholder="One line is enough." className="veil-textarea" />
            <div className="veil-flow-footer"><span>Stored locally in this browser.</span><button onClick={() => { if (note.trim()) { window.localStorage.setItem("veil-recovery-note", note.trim()); setSavedNote(note.trim()); setSaved(true); } }} disabled={!note.trim()} className="veil-primary-button">{saved ? <><Check size={15} /> Saved</> : "Save note"}</button></div>
          </div>}
        </section>

        <footer className="veil-footer">No tracking · no screen capture · no device monitoring</footer>
      </div>
      {view === "active" && !completed && <aside className="veil-cooldown-screen" role="dialog" aria-modal="true" aria-label="Active Veil cooldown">
        <div className="veil-cooldown-panel">
          <p className="veil-eyebrow">Protective cooldown active</p><h2>Take this time back.</h2>
          <p className="veil-cooldown-copy">Veil has started your {duration} cooldown. The protective screen stays in place until the time ends or you finish deliberately.</p>
          <div className="veil-cooldown-timer"><span>Time remaining</span><strong aria-live="polite">{formatRemaining(remainingSeconds)}</strong></div>
          <section className="veil-cooldown-task"><p>Your reset activity</p><strong>{reset.name}</strong><span>{reset.detail}</span></section>
          {savedNote && <section className="veil-cooldown-note"><p>Your note</p><blockquote>“{savedNote}”</blockquote></section>}
          <p className="veil-native-disclaimer"><strong>Browser limitation:</strong> this page can blur only Veil itself. It cannot inspect other apps or blur the rest of your phone. Full-device detection and protection will be added in the native Android app with local TensorFlow Lite.</p>
          <div className="veil-button-row"><button onClick={completeReset} className="veil-primary-button">I’m done</button><button onClick={changePlan} className="veil-secondary-button">Change plan</button></div>
        </div>
      </aside>}
    </main>
  );
}
