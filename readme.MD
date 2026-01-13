# XHTML-ből PDF generálás
## flying-saucer-pdf-openpdf library
A flying-saucer egy jól bevált megoldás a HTML + CSS → PDF konverzióra.
Jelen példa Java-ból, Tomcat + JSF környezetre épül. 

```
HTML + CSS
↓
Flying Saucer (renderer)
↓
OpenPDF (PDF motor)
↓
PDF fájl
```

## Elv
**Flying Saucer**: Egy XHTML/CSS 2.1 renderer Java-ban  
**OpenPDF**: Az iText 4 open-source forkja → stabil, jogilag tiszta  
A flying-saucer-pdf-openpdf modul a kettőt köti össze.

