/** Minimal Veil: one quiet page for starting a private browser-based commitment. */
import { useMemo, useState } from "react";
import { Check, Shield, X } from "lucide-react";

const durations = ["2 hours", "8 hours", "24 hours"];
const recoveries = ["Breathing", "Walk", "Journal"];

export default function Home() {
  const [duration, setDuration] = useState(durations[0]);
  const [recovery, setRecovery] = useState(recoveries[0]);
  const [active, setActive] = useState(false);
  const [note, setNote] = useState("");
  const [saved, setSaved] = useState(false);

  const sessionCopy = useMemo(
    () => (active ? `Active for ${duration}. Recovery: ${recovery}.` : "Choose a boundary for the next part of your day."),
    [active, duration, recovery],
  );

  return (
    <main className="min-h-screen bg-[#101114] px-5 py-7 text-[#f1eee8] sm:px-8 sm:py-10">
      <div className="veil-room relative mx-auto flex min-h-[calc(100vh-56px)] max-w-xl flex-col lg:ml-[18%]">
        <div className="veil-signal" aria-hidden="true" />
        <header className="flex items-center justify-between border-b border-white/10 pb-5">
          <div className="flex items-center gap-2.5">
            <div className="veil-mark flex h-8 w-8 items-center justify-center rounded-lg bg-[#151820] text-[#5b84c6]"><Shield className="h-4 w-4" /></div>
            <span className="text-sm font-semibold tracking-[0.22em]">VEIL</span>
          </div>
          <span className="text-xs text-[#9b9995]">Private browser session</span>
        </header>

        <section className="flex flex-1 flex-col justify-center py-14 sm:py-20">
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-[#7194ce]">A small pause</p>
          <h1 className="mt-4 font-serif text-5xl leading-[0.94] tracking-[-0.045em] text-white sm:text-6xl">Keep your next promise.</h1>
          <p className="mt-4 max-w-md text-base leading-7 text-[#b2afa9]">Set one clear boundary. Veil stays inside this browser and does not monitor your device.</p>

          <div className="veil-plane mt-10 border-y border-white/10 py-7">
            <fieldset>
              <legend className="text-sm font-medium text-white">How long?</legend>
              <div className="mt-3 flex flex-wrap gap-2">
                {durations.map((item) => (
                  <button key={item} onClick={() => { setDuration(item); setActive(false); }} className={`rounded-md border px-3 py-2 text-sm transition ${duration === item ? "border-[#5b84c6] bg-[#385c98]/20 text-white" : "border-white/10 text-[#b2afa9] hover:border-white/25"}`}>{item}</button>
                ))}
              </div>
            </fieldset>

            <fieldset className="mt-7">
              <legend className="text-sm font-medium text-white">If you need a reset?</legend>
              <div className="mt-3 flex flex-wrap gap-2">
                {recoveries.map((item) => (
                  <button key={item} onClick={() => { setRecovery(item); setActive(false); }} className={`rounded-md border px-3 py-2 text-sm transition ${recovery === item ? "border-[#5b84c6] bg-[#385c98]/20 text-white" : "border-white/10 text-[#b2afa9] hover:border-white/25"}`}>{item}</button>
                ))}
              </div>
            </fieldset>
          </div>

          <div className="mt-7 flex items-center justify-between gap-4">
            <p className="text-sm leading-6 text-[#aaa7a2]">{sessionCopy}</p>
            <button onClick={() => setActive((value) => !value)} className={`shrink-0 rounded-md px-4 py-2.5 text-sm font-medium transition active:scale-[0.98] ${active ? "border border-white/15 text-[#d9d6d1] hover:bg-white/5" : "bg-[#385c98] text-white hover:bg-[#486fb0]"}`}>
              {active ? "End session" : "Start session"}
            </button>
          </div>

          {active && (
            <div className="mt-6 flex items-start gap-3 border-l-2 border-[#5b84c6] bg-white/[0.025] px-4 py-3 text-sm leading-6 text-[#c6c2bb]">
              <Check className="mt-1 h-4 w-4 shrink-0 text-[#84ac8e]" />
              <span>Your boundary is active in this page. If you want a reset, try: <strong className="font-medium text-white">{recovery}</strong>.</span>
            </div>
          )}

          <div className="mt-12">
            <label htmlFor="reflection" className="text-sm font-medium text-white">Optional note</label>
            <textarea id="reflection" value={note} onChange={(event) => { setNote(event.target.value); setSaved(false); }} placeholder="What do you want to remember?" className="mt-3 min-h-28 w-full rounded-md border border-white/10 bg-transparent px-3 py-3 text-sm text-white outline-none placeholder:text-[#706e6b] focus:border-[#5b84c6]" />
            <div className="mt-3 flex items-center justify-between">
              <p className="text-xs text-[#7e7b76]">Nothing is sent anywhere.</p>
              <button onClick={() => note.trim() && setSaved(true)} disabled={!note.trim()} className="text-xs text-[#bfcce6] hover:text-white disabled:opacity-40">{saved ? "Saved for this session" : "Save note"}</button>
            </div>
          </div>
        </section>

        <footer className="flex items-center justify-between border-t border-white/10 pt-5 text-xs text-[#7e7b76]">
          <span>No tracking · no screen capture · no device monitoring</span>
          {active && <button onClick={() => setActive(false)} aria-label="End active session" className="text-[#aaa7a2] hover:text-white"><X className="h-4 w-4" /></button>}
        </footer>
      </div>
    </main>
  );
}
