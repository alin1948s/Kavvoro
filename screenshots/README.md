# Screenshot evidence

Acest director conține numai capturi QA curate și intenționat păstrate, grupate
după flow sau ecran. Scripturile care le produc sunt în
`tools/screenshot-capture/`.

Reguli:

- adaugă capturile într-un subdirector descriptiv, nu în rădăcina proiectului;
- păstrează doar matricea relevantă de dispozitive/rezoluții și elimină cadrele
  intermediare înainte de commit;
- numele trebuie să descrie ecranul și profilul testat;
- dump-urile ANR, logurile `scrcpy` și capturile exploratorii rămân locale și
  sunt ignorate de Git.
