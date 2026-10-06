# Screenshot evidence

Acest director conține numai capturi QA curate și intenționat păstrate, grupate
după flow sau ecran. Scripturile care le produc sunt în
`tools/screenshot-capture/`.

Home includes a phone portrait capture at 1080×2400 px / 420 dpi and a tablet
landscape capture at 1920×1200 px / 240 dpi. Landscape is stored beside the
portrait Home matrix because it is a distinct native layout profile.

Age Check screenshots show phone and tablet portrait layouts; the saved profile
contains only the resolved age group. Home landscape remains a separate native
tablet profile.

Reguli:

- adaugă capturile într-un subdirector descriptiv, nu în rădăcina proiectului;
- păstrează doar matricea relevantă de dispozitive/rezoluții și elimină cadrele
  intermediare înainte de commit;
- numele trebuie să descrie ecranul și profilul testat;
- dump-urile ANR, logurile `scrcpy` și capturile exploratorii rămân locale și
  sunt ignorate de Git.
