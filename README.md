# WhatsApp Chat Analyzer (Android)

Android-App zum lokalen Analysieren exportierter WhatsApp-Chats (`.txt`).

## Funktionen

- Chat-Datei aus dem Dateisystem öffnen
- Gesamtanzahl Nachrichten
- Nachrichten pro Person
- Wörter pro Person
- Durchschnittliche Nachrichtenlänge
- Häufigste Wörter

## Datenschutz

- Es werden nur lokal ausgewählte `.txt`-Dateien verarbeitet.
- Keine Server-Kommunikation im App-Code.

## APK bauen (vollautomatisch)

Im Projektordner ausführen:

```powershell
.\build-shareable-apk.ps1
```

Danach liegt die teilbare APK hier:

`dist\WhatsAppChatAnalyzer-release.apk`
