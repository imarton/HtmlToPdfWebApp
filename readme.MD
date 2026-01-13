# XHTML-ből PDF generálás
## OpenHTMLtoPDF library
Az OpenHTMLtoPDF egy modern megoldás a HTML + CSS → PDF konverzióra, amely az Apache PDFBox-ra épül.
Jelen példa Java-ból, Tomcat + JSF környezetre épül. 

```
HTML + CSS
↓
OpenHTMLtoPDF (renderer)
↓
Apache PDFBox (PDF motor)
↓
PDF fájl
```

## Elv
**OpenHTMLtoPDF**: Egy XHTML/CSS renderer Java-ban, amely támogatja a modernebb CSS szabványokat is.
**Apache PDFBox**: Egy széles körben használt nyílt forráskódú Java könyvtár PDF dokumentumok kezeléséhez.

