# iHateStau

## Disclaimer: This was a student project. Due to pragmatism and pressure of time some components may not are as beautiful as they are supposed to be...
Feel free to make them beautiful again!

For praise or donations feel free to contact us at iHateStau@gmx.de

## How-to: Build and deploy
0. Optional: Besseres neuronales Netz einbinden (ist zu groß für GitHub): [Hier herunterladen](https://mega.nz/#!tFE2SI4R!5PUA6G_151mYHQRjni1fDsPlWiAhxHuBN8qF73Dx8eU) und das Verzeichnis `source/neural-net-server/model` ersetzen
1. source/neural-net-server/serve.py starten 
2. Im Source Verzeichnis bash öffnen
3. mvn clean install -f iHateStau-parent/pom.xml
4. In den Verzeichnissen iHateStau-image-preparation, iHateStau und verkehrscam-scraper liegt jeweils im /target/ Verzeichnis eine Jar-Datei (ohne original Prefix). Diese Jar-Dateien müssen alle ausgeführt werden
5. Beim ersten Deploy: Jar-Datei in spot-synchronizer/target/ ausführen um die Datenbank zu befüllen
6. Alle Services sollten laufen. Unter localhost:8080/ihatestau/info/html finden sich eine Debug Seite zum Testen.
7. Enjoy!

## Android App
Verwendete Entwicklungsumgebung: Android Studio 3.1.1

Um die App nutzen zu können wird ein Google-API-Key benötigt. Dieser ist in der AndroidManifest.xml einzutragen.
Der Key muss für die APIs "Maps SDK for Android" und "Places SDK for Android" freigeschaltet sein.
Infos: https://developers.google.com/places/web-service/get-api-key?hl=de

Im RetroFitRestClient muss die Base-URL des REST-Servers eingetragen werden.

Hinweis: Es wurde ausschließlich die Debug-APK verwendet.
