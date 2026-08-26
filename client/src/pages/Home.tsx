/** Calm Minimal Veil: an image-free menu with a gentle reset chooser and local-only session state. */
import { useState } from "react";
import { Check, ChevronLeft, CirclePause, Coffee, Dumbbell, Footprints, PenLine, Shield, Sparkles, StretchHorizontal, TimerReset, Waves } from "lucide-react";

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

export default function Home() {
  const [view, setView] = useState<View>("home");
  const [duration, setDuration] = useState(durations[1]);
  const [reset, setReset] = useState(resetOptions[0]);
  const [note, setNote] = useState("");
  const [saved, setSaved] = useState(false);

  const beginReset = () => setView("active");

  return (
    <main className="min-h-screen bg-[#f7f6f3] text-[#1c1d20]">
      <div className="veil-room mx-auto flex min-h-screen max-w-3xl flex-col px-5 py-5 sm:px-8 sm:py-7">
        <div className="veil-signal" aria-hidden="true" />
        <header className="flex items-center justify-between border-b border-[#deddd8] pb-5">
          <button onClick={() => setView("home")} className="flex items-center gap-2 text-left" aria-label="Veil home">
            <span className="flex h-8 w-8 items-center justify-center rounded-md bg-[#385c98] text-white"><Shield className="h-4 w-4" /></span>
            <span className="text-sm font-semibold tracking-[0.2em]">VEIL</span>
          </button>
          <nav className="flex items-center gap-1 text-sm" aria-label="Veil menu">
            <button onClick={() => setView("home")} className={`rounded-md px-3 py-2 transition ${view === "home" ? "bg-[#e9edf5] text-[#385c98]" : "text-[#68686b] hover:bg-[#ecebe7]"}`}>Home</button>
            <button onClick={() => setView("reset")} className={`rounded-md px-3 py-2 transition ${view === "reset" || view === "active" ? "bg-[#e9edf5] text-[#385c98]" : "text-[#68686b] hover:bg-[#ecebe7]"}`}>Reset</button>
            <button onClick={() => setView("note")} className={`rounded-md px-3 py-2 transition ${view === "note" ? "bg-[#e9edf5] text-[#385c98]" : "text-[#68686b] hover:bg-[#ecebe7]"}`}>Note</button>
          </nav>
        </header>

        <section className="flex flex-1 items-center py-12 sm:py-16">
          {view === "home" && (
            <div className="w-full max-w-xl">
              <p className="text-xs font-medium uppercase tracking-[0.2em] text-[#5474ab]">Private reset space</p>
              <h1 className="mt-4 max-w-lg font-serif text-5xl leading-[0.94] tracking-[-0.045em] sm:text-6xl">What would help right now?</h1>
              <p className="mt-4 max-w-lg text-base leading-7 text-[#656569]">Choose a small reset. Veil does not watch your device or send your activity anywhere.</p>
              <div className="mt-10 grid gap-3 sm:grid-cols-2">
                <button onClick={() => setView("reset")} className="group border border-[#9eb1d1] border-l-2 border-l-[#385c98] bg-white p-5 text-left shadow-[0_1px_2px_rgba(0,0,0,.03)] transition hover:border-[#6384ba] hover:shadow-[0_4px_12px_rgba(56,92,152,.08)] active:scale-[0.99]">
                  <TimerReset className="h-5 w-5 text-[#385c98]" />
                  <p className="mt-7 text-base font-medium">Start a reset</p>
                  <p className="mt-1 text-sm leading-6 text-[#747477]">Pick time and one helpful action.</p>
                </button>
                <button onClick={() => setView("note")} className="border border-[#cfcec9] bg-white p-5 text-left shadow-[0_1px_2px_rgba(0,0,0,.03)] transition hover:border-[#9eb1d1] hover:shadow-[0_4px_12px_rgba(56,92,152,.08)] active:scale-[0.99]">
                  <PenLine className="h-5 w-5 text-[#385c98]" />
                  <p className="mt-7 text-base font-medium">Write a note</p>
                  <p className="mt-1 text-sm leading-6 text-[#747477]">Leave one line for yourself.</p>
                </button>
              </div>
            </div>
          )}

          {view === "reset" && (
            <div className="w-full">
              <button onClick={() => setView("home")} className="mb-8 inline-flex items-center gap-1 text-sm text-[#68686b] hover:text-[#385c98]"><ChevronLeft className="h-4 w-4" /> Back</button>
              <p className="text-xs font-medium uppercase tracking-[0.2em] text-[#5474ab]">Reset</p>
              <h1 className="mt-3 text-3xl font-semibold tracking-[-0.04em] sm:text-4xl">Make a small plan.</h1>
              <div className="mt-8 border-y border-[#deddd8] py-6">
                <p className="text-sm font-medium">How long do you want this reset to last?</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {durations.map((item) => <button key={item} onClick={() => setDuration(item)} className={`rounded-md border px-3 py-2 text-sm ${duration === item ? "border-[#385c98] bg-[#e9edf5] text-[#385c98]" : "border-[#d6d5d0] bg-white text-[#5f5f63] hover:border-[#a6a5a1]"}`}>{item}</button>)}
                </div>
              </div>
              <div className="pt-6">
                <p className="text-sm font-medium">Choose one reset.</p>
                <div className="mt-3 grid gap-2 sm:grid-cols-2">
                  {resetOptions.map((option) => {
                    const Icon = option.icon;
                    const chosen = reset.name === option.name;
                    return <button key={option.name} onClick={() => setReset(option)} className={`flex items-center gap-3 rounded-md border p-3 text-left transition ${chosen ? "border-[#385c98] bg-[#e9edf5]" : "border-[#d6d5d0] bg-white hover:border-[#a6a5a1]"}`}>
                      <Icon className={`h-4 w-4 ${chosen ? "text-[#385c98]" : "text-[#77777b]"}`} />
                      <span><span className="block text-sm font-medium">{option.name}</span><span className="block text-xs text-[#77777b]">{option.detail}</span></span>
                    </button>;
                  })}
                </div>
              </div>
              <div className="mt-8 flex items-center justify-between border-t border-[#deddd8] pt-5">
                <p className="text-sm text-[#69696d]">{duration} · {reset.name}</p>
                <button onClick={beginReset} className="rounded-md bg-[#385c98] px-4 py-2.5 text-sm font-medium text-white transition hover:bg-[#2e4d80] active:scale-[0.98]">Begin reset</button>
              </div>
            </div>
          )}

          {view === "active" && (
            <div className="w-full max-w-xl">
              <p className="text-xs font-medium uppercase tracking-[0.2em] text-[#5474ab]">Reset in progress</p>
              <h1 className="mt-3 text-4xl font-semibold tracking-[-0.04em]">Take your time.</h1>
              <div className="mt-8 rounded-lg border border-[#cad4e6] bg-[#f0f4fa] p-6">
                <p className="text-sm text-[#5670a1]">Your reset</p>
                <p className="mt-2 text-2xl font-semibold text-[#1f3357]">{reset.name}</p>
                <p className="mt-2 text-sm leading-6 text-[#52617a]">{reset.detail} · {duration}</p>
              </div>
              <p className="mt-6 text-sm leading-6 text-[#69696d]">When you are done, you can end this session or take a little longer. There is no score to chase.</p>
              <div className="mt-8 flex gap-3"><button onClick={() => setView("home")} className="rounded-md bg-[#385c98] px-4 py-2.5 text-sm font-medium text-white hover:bg-[#2e4d80]">I’m done</button><button onClick={() => setView("reset")} className="rounded-md border border-[#d6d5d0] bg-white px-4 py-2.5 text-sm text-[#5f5f63] hover:border-[#a6a5a1]">Change plan</button></div>
            </div>
          )}

          {view === "note" && (
            <div className="w-full max-w-xl">
              <button onClick={() => setView("home")} className="mb-8 inline-flex items-center gap-1 text-sm text-[#68686b] hover:text-[#385c98]"><ChevronLeft className="h-4 w-4" /> Back</button>
              <p className="text-xs font-medium uppercase tracking-[0.2em] text-[#5474ab]">A note for yourself</p>
              <h1 className="mt-3 text-3xl font-semibold tracking-[-0.04em] sm:text-4xl">What do you want to remember?</h1>
              <textarea value={note} onChange={(event) => { setNote(event.target.value); setSaved(false); }} placeholder="One line is enough." className="mt-8 min-h-36 w-full rounded-md border border-[#d6d5d0] bg-white px-4 py-3 text-sm leading-6 outline-none placeholder:text-[#9a9995] focus:border-[#385c98]" />
              <div className="mt-4 flex items-center justify-between"><p className="text-xs text-[#7b7a77]">Stored only in this browser session.</p><button onClick={() => note.trim() && setSaved(true)} disabled={!note.trim()} className="rounded-md bg-[#385c98] px-4 py-2 text-sm font-medium text-white disabled:opacity-40">{saved ? <span className="inline-flex items-center gap-1"><Check className="h-4 w-4" /> Saved</span> : "Save note"}</button></div>
            </div>
          )}
        </section>

        <footer className="border-t border-[#deddd8] pt-5 text-xs text-[#7b7a77]">No tracking · no screen capture · no device monitoring</footer>
      </div>
    </main>
  );
}
