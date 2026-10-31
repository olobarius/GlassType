# GlassType

**Android-Tastatur mit einstellbarer Transparenz, integrierter Spracheingabe und „Blick unter die Tastatur“.**
Basiert auf [LeanType](https://github.com/LeanBitLab/LeanType) (Fork von [HeliBoard](https://github.com/Helium314/HeliBoard) / OpenBoard / AOSP LatinIME) und übernimmt dessen kompletten Funktionsumfang, inklusive Wischeingabe, schwebender Tastatur, Split-Layout für Foldables, Zwischenablage und Emoji-Suche.

*English summary: GlassType is a LeanType/HeliBoard based Android keyboard that adds an adjustable transparent mode (home screen wallpaper shining through, pixel-aligned, or real see-through), built-in voice typing via the system speech recognizer, a hold-to-peek button to look behind the keyboard, and quick toggles for transparency and "keyboard over app" mode.*

Zielgeräte: Samsung Galaxy Z Fold 3 und Galaxy S23 Ultra (läuft auf Android 6.0 und neuer).

> **Status:** Testversion. Auf einem Galaxy Z Fold 3 (Android 15) getestet und funktionierend: Transparenz („Startbildschirm scheint durch“ und „Echte Durchsicht“), Transparenz-Schnellschalter, Tastatur über der App, Auge (unter die Tastatur schauen), integrierte Spracheingabe sowie schwebende Tastatur mit Transparenz, Auge und Verschieben. Fehler bitte als [Issue](https://github.com/olobarius/GlassType/issues) melden.

---

## Inhalt

- [Download und Installation](#download-und-installation)
- [Ersteinrichtung](#ersteinrichtung)
- [Neue Funktionen im Überblick](#neue-funktionen-im-überblick)
- [Transparenz-Modus](#transparenz-modus)
- [Schnellschalter Transparenz](#schnellschalter-transparenz)
- [Tastatur über der App (nicht hochschieben)](#tastatur-über-der-app-nicht-hochschieben)
- [Unter die Tastatur schauen (Auge)](#unter-die-tastatur-schauen-auge)
- [Integrierte Spracheingabe](#integrierte-spracheingabe)
- [Schwebende Tastatur](#schwebende-tastatur)
- [Symbolleiste: die neuen Knöpfe](#symbolleiste-die-neuen-knöpfe)
- [Bekannte Einschränkungen](#bekannte-einschränkungen)
- [Selbst bauen](#selbst-bauen)
- [Lizenz und Danksagung](#lizenz-und-danksagung)

---

## Download und Installation

1. Auf dem Handy die neueste APK unter **[Releases](https://github.com/olobarius/GlassType/releases)** öffnen und herunterladen.
2. Die APK antippen. Beim ersten Mal fragt Android nach der Erlaubnis **„Unbekannte Apps installieren“** für den Browser bzw. „Eigene Dateien“. Erlauben, dann installieren.
3. GlassType hat eine eigene Paket-ID (`de.glasstype.keyboard`) und lässt sich **parallel zu LeanType, Gboard, SwiftKey oder der Samsung-Tastatur** installieren.

> Die aktuellen Testversionen sind Debug-Builds und erscheinen als **„GlassType debug“**. Ein Update von einer älteren Testversion installiert man einfach darüber.

## Ersteinrichtung

1. **GlassType** öffnen und dem Einrichtungsassistenten folgen: Tastatur aktivieren und als Standard wählen.
   Bei Samsung alternativ: *Einstellungen → Allgemeine Verwaltung → Tastaturliste und Standard*.
2. **Wörterbuch laden:** *Einstellungen → Sprachen & Layouts* → Deutsch (bzw. weitere Sprachen) und das Wörterbuch herunterladen. Ohne Wörterbuch gibt es keine Wortvorschläge und keine Wischeingabe.
3. **Wischeingabe:** *Einstellungen → Gesten-Tippen → Gesten-Tippen aktivieren*. Die benötigte Bibliothek wird automatisch geladen (sonst „Gesten-Tippen-Bibliothek laden“).
4. **Mikrofon erlauben:** *Einstellungen → Spracheingabe → Mikrofonberechtigung* antippen und erlauben.
5. **Transparenz einschalten:** siehe [Transparenz-Modus](#transparenz-modus).

---

## Neue Funktionen im Überblick

| Funktion | Kurz erklärt | Aktivieren |
|---|---|---|
| **Transparenz-Modus** | Startbildschirm-Hintergrund scheint pixelgenau durch die Tastatur, oder echte Durchsicht auf die App dahinter. Deckkraft für Hintergrund und Tasten getrennt einstellbar. | *Einstellungen → Aussehen → Transparenz* |
| **Schnellschalter Transparenz** | Ein Tipp schaltet die Transparenz an/aus. | Tropfen-Symbol in der Symbolleiste |
| **Tastatur über der App** | Die angedockte Tastatur schiebt die App nicht hoch, sondern liegt darüber. | Ebenen-Symbol in der Symbolleiste oder *Aussehen → Transparenz* |
| **Unter die Tastatur schauen** | Auge gedrückt halten: Tastatur verschwindet, darunter Verdecktes wird sichtbar. Loslassen und weitertippen. | Auge-Symbol in der Symbolleiste (und unten in der schwebenden Tastatur) |
| **Integrierte Spracheingabe** | Spracherkennung von Google/Samsung direkt in der Tastatur, Live-Text beim Sprechen, kein Plugin nötig. | Mikrofon-Symbol; Standard-Anbieter |
| **Transparente schwebende Tastatur** | Transparenz und Auge-Knopf funktionieren auch im schwebenden Modus. | Symbol „Schwebend“ in der Symbolleiste |

---

## Transparenz-Modus

### Was er macht

Es gibt zwei Arten von Transparenz:

| Modus | Wirkung | Gut für |
|---|---|---|
| **Startbildschirm scheint durch** | Das Hintergrundbild deines Startbildschirms wird **genau an der Stelle gezeichnet, an der die Tastatur auf dem Bildschirm liegt**, als wäre die Tastatur ein Fenster auf den Desktop. Das funktioniert in jeder App. | Den „Glas-Look“ überall |
| **Echte Durchsicht** | Der Tastaturhintergrund ist wirklich durchsichtig, man sieht, was die App hinter der Tastatur zeichnet. | Zusammen mit [Tastatur über der App](#tastatur-über-der-app-nicht-hochschieben), um den App-Inhalt durch die Tastatur zu sehen |

Über dem Hintergrund liegt die Farbe deines Tastatur-Designs mit einstellbarer Deckkraft. So bleibt die Schrift lesbar, egal wie bunt das Hintergrundbild ist.

### Aktivieren

1. *Einstellungen → Aussehen*. Ganz oben steht der Bereich **Transparenz**.
2. **Transparenz-Modus** wählen: *Aus*, *Startbildschirm scheint durch* oder *Echte Durchsicht*.
3. Einstellen:
   - **Deckkraft Hintergrund** (0–100 %): wie stark die Designfarbe über dem Hintergrund liegt. 0 % = Hintergrund voll sichtbar, 100 % = undurchsichtig. Standard 35 %.
   - **Deckkraft Tasten** (0–100 %): Deckkraft der Tastenflächen. Standard 55 %. Popups und Vorschlagsfenster bleiben zur besseren Lesbarkeit immer deckend.
   - **Hintergrundunschärfe** (nur bei „Startbildschirm scheint durch“): weichgezeichneter Hintergrund, ruhigeres Schriftbild.
   - **Tastatur liegt über der App**: siehe unten.

### Zugriff auf das Hintergrundbild

Um das Hintergrundbild lesen zu dürfen, braucht GlassType eine Berechtigung. Wählst du „Startbildschirm scheint durch“ und der Zugriff fehlt, zeigt GlassType einen Dialog:

- **„Zugriff erlauben“** fragt zuerst nach **„Fotos und Videos“**. Bitte **„Alle erlauben“** wählen. Auf Samsung-Geräten (getestet: Galaxy Z Fold 3 mit Android 15) genügt das.
- Reicht das auf einem Gerät nicht (z. B. reines Android 14+), öffnet GlassType danach die Einstellung **„Zugriff auf alle Dateien“**.
- GlassType nutzt die Berechtigungen ausschließlich, um das Hintergrundbild zu lesen.
- **Ohne Berechtigung:** Unter *Aussehen → Transparenz → Hintergrundbild festlegen* dasselbe Bild auswählen, das du als Hintergrund des Startbildschirms nutzt. Es wird genauso bildschirmgenau ausgerichtet.

Wechselst du später dein Hintergrundbild, aktualisiert sich die Tastatur automatisch.

---

## Schnellschalter Transparenz

**Tropfen-Symbol** in der Symbolleiste über der Tastatur:

- Ein Tipp: Transparenz **aus**, nächster Tipp: wieder **an**, und zwar mit dem zuletzt gewählten Modus (Standard: „Startbildschirm scheint durch“).
- Ist das Symbol hervorgehoben, ist die Transparenz an.
- Beim Umschalten baut sich die Tastatur einmal kurz neu auf.

---

## Tastatur über der App (nicht hochschieben)

Normalerweise schiebt eine angedockte Tastatur die App nach oben bzw. verkleinert sie. Mit diesem Schalter **legt sich die Tastatur über die App**, die App behält ihre volle Größe.

- **Ebenen-Symbol** in der Symbolleiste: Tipp schaltet um, hervorgehoben = Tastatur liegt über der App.
- Dauerhaft einstellbar unter *Aussehen → Transparenz → Tastatur liegt über der App*.
- **Tipp:** Zusammen mit **Echte Durchsicht** und niedriger Hintergrund-Deckkraft sieht man den App-Inhalt durch die Tastatur.
- Gilt nur für die angedockte Tastatur. Die schwebende Tastatur liegt ohnehin immer über der App.

> Achtung: Die App scrollt das Eingabefeld dann nicht mehr automatisch über die Tastatur. Liegt das Feld darunter, hilft das [Auge](#unter-die-tastatur-schauen-auge) oder der Schalter.

---

## Unter die Tastatur schauen (Auge)

Schnell nachsehen, was gerade unter der Tastatur versteckt ist, und sofort weitertippen:

1. **Auge-Symbol gedrückt halten.**
2. Die Tastatur verschwindet bis auf das Auge. Die App zeigt für diesen Moment ihren **vollen Inhalt**, auch das, was vorher unter der Tastatur lag.
3. **Loslassen:** Die Tastatur ist sofort wieder da und du tippst weiter.

Details:

- Funktioniert **angedockt** (Auge in der Symbolleiste) und **schwebend** (zusätzlich ein Auge unten in der Leiste der schwebenden Tastatur, neben dem Schließen-Knopf).
- Angedockt meldet die Tastatur während des Haltens die Höhe 0. Ohne das wäre unter der Tastatur nichts zu sehen, weil Android die App sonst nur bis zur Tastatur zeichnet.
- Während du hältst, ist nur der Auge-Knopf berührbar. Berührungen an anderen Stellen gehen an die App, du solltest also mit einem zweiten Finger scrollen können.

---

## Integrierte Spracheingabe

GlassType nutzt die **Spracherkennung, die auf deinem Handy installiert ist** (Google oder Samsung), direkt in der Tastatur. Es wird nicht auf eine andere Tastatur umgeschaltet, und es ist kein Plugin und kein API-Schlüssel nötig.

- **Mikrofon-Symbol** in der Symbolleiste antippen und sprechen. Der Text erscheint **live** beim Sprechen.
- **Fortlaufendes Diktat:** Nach einer Sprechpause hört GlassType weiter zu, bis du das Mikrofon erneut antippst oder länger nichts sagst.
- Satzanfänge werden automatisch großgeschrieben, Leerzeichen zum vorhandenen Text werden ergänzt.
- **Sprache:** folgt der aktuellen Tastatursprache. Ändern unter *Einstellungen → Spracheingabe → Spracherkennungssprache*.
- **Anbieter wechseln:** *Einstellungen → Spracheingabe → Spracheingabeanbieter*:
  - *Integrierte Spracheingabe (Google / Samsung)*: Standard
  - *Offline-Spracheingabe (Plugin)*, *Online-AI-Spracheingabe*, *System- / Drittanbieter-Spracheingabe* (wechselt zur Sprach-Tastatur eines anderen Anbieters): wie in LeanType
- Voraussetzung: **Mikrofonberechtigung** (*Spracheingabe → Mikrofonberechtigung*) und ein installierter Spracherkennungsdienst. Auf Samsung-Geräten ist meist „Spracherkennung und -synthese von Google“ oder die Samsung-Spracheingabe vorhanden.
- In Passwortfeldern ist die Spracheingabe aus Sicherheitsgründen gesperrt.

---

## Schwebende Tastatur

- Einschalten über das Symbol **„Schwebend“** in der Symbolleiste. Unten an der schwebenden Tastatur: links **Andocken (X)**, daneben das **Auge**, in der Mitte der **Ziehgriff** zum Verschieben, rechts der **Größen-Griff**.
- **Transparenz** funktioniert auch hier. Bei „Startbildschirm scheint durch“ bleibt das Hintergrundbild beim Verschieben an seiner Bildschirmposition, die Tastatur wirkt wie ein Fenster auf den Desktop.
- Mikrofon, Auge und Transparenz-Schalter sind in der Symbolleiste genauso erreichbar wie angedockt.
- Fold 3: Unter *Aussehen → Zusammenklappbares und großes Display* gibt es eigene Höhe, Split und Abstände für den aufgeklappten Zustand (aus LeanType).

---

## Symbolleiste: die neuen Knöpfe

Neu angeheftet (immer sichtbar neben den Vorschlägen): Mikrofon, Auge, Transparenz, Tastatur über der App, Zwischenablage, Textbearbeitung, Schwebend.

| Symbol | Name | Bedienung |
|---|---|---|
| Mikrofon | Spracheingabe | Tippen: Diktat starten/stoppen |
| Auge | Unter die Tastatur schauen | **Gedrückt halten**, loslassen = zurück |
| Tropfen | Transparenz an/aus | Tippen |
| Ebenen | Tastatur über der App | Tippen |

Anpassen unter *Einstellungen → Symbolleiste → Angeheftete Tasten der Symbolleiste auswählen* bzw. *Symbolleistentasten wählen*.

> Bei einer Installation über eine **ältere Testversion** werden die neuen Knöpfe nicht automatisch angeheftet. Dann einmal in den genannten Einstellungen anheften (oder App-Daten löschen).

---

## Bekannte Einschränkungen

- **Live-Hintergründe** (animiert) gibt Android nicht an Apps heraus. Dann bitte ein Standbild über „Hintergrundbild festlegen“ wählen.
- **Fold 3:** Samsung erlaubt getrennte Hintergründe für Außen- und Innendisplay. Android liefert Apps nur einen davon, daher kann der Durchschein-Effekt auf einem der Displays nicht exakt passen. Lösung: ein eigenes Bild festlegen.
- **Echte Durchsicht** zeigt nur, was die App hinter die Tastatur zeichnet. Ohne „Tastatur über der App“ ist das meist nur eine einfarbige Fläche.
- Bei „Tastatur über der App“ und beim Auge baut die App ihr Layout kurz neu auf. Bei manchen Apps kann das kurz ruckeln.
- Die optionalen LeanType-Plugins (Whisper, OCR, Übersetzung …) sind mit GlassType nicht getestet.
- Testversionen sind Debug-Builds (etwas größer und langsamer als ein Release-Build).

---

## Selbst bauen

Voraussetzungen: JDK 17, Android SDK (Platform 36, Build-Tools 36), **NDK 28.0.13004108** (für die Wischeingabe-Bibliothek).

```bash
git clone https://github.com/olobarius/GlassType.git
cd GlassType
# local.properties: sdk.dir=/pfad/zum/Android/sdk
./gradlew assembleStandardDebug      # Windows: gradlew.bat assembleStandardDebug
```

Die APK liegt danach unter `app/build/outputs/apk/standard/debug/`.

> **Wichtig unter Windows:** Der Projektpfad darf **keine Leerzeichen** enthalten, sonst scheitert der NDK-Build.

Die wichtigsten Änderungen gegenüber LeanType im Code:

| Datei | Inhalt |
|---|---|
| `app/src/main/java/helium314/keyboard/latin/utils/KeyboardTransparency.kt` | Transparenz-Modi, bildschirmgenauer Hintergrund, Schnellschalter |
| `app/src/main/java/helium314/keyboard/latin/KeyboardPeek.kt` | „Unter die Tastatur schauen“ |
| `app/src/main/java/helium314/keyboard/latin/voice/VoiceInputManager.kt` | Integrierte Spracheingabe (Android `SpeechRecognizer`) |
| `app/src/main/java/helium314/keyboard/latin/LatinIME.kt` | Insets für Auge und „Tastatur über der App“, transparente Navigationsleiste |
| `app/src/main/java/helium314/keyboard/latin/FloatingKeyboardManager.kt` | Transparenz und Auge in der schwebenden Tastatur |

---

## Lizenz und Danksagung

GlassType steht wie LeanType und HeliBoard unter der **GNU General Public License v3.0** (siehe [LICENSE](LICENSE)). Teile aus AOSP stehen unter Apache-2.0.

- [LeanType](https://github.com/LeanBitLab/LeanType) von LeanBitLab: direkte Basis, alle dort beschriebenen Funktionen sind enthalten
- [HeliBoard](https://github.com/Helium314/HeliBoard) von Helium314
- [OpenBoard](https://github.com/openboard-team/openboard) und [AOSP LatinIME](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/)
- Icons: [Material Icons](https://fonts.google.com/icons) (Apache-2.0)
