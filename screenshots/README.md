# Screenshot evidence

Acest director conține numai capturi QA curate și intenționat păstrate, grupate
după flow sau ecran. Scripturile care le produc sunt în
`tools/screenshot-capture/`.

Home includes a phone portrait capture at 1080×2400 px / 420 dpi and a tablet
landscape capture at 1920×1200 px / 240 dpi. Landscape is stored beside the
portrait Home matrix because it is a distinct native layout profile.

Missions includes phone portrait captures at 1080×2400 px / 420 dpi for Daily
Missions and persistent Rift Challenges. They show the category tabs, mission
progress, and claimable coin rewards.

Age Check contains the full 11-profile portrait matrix: five phone sizes and
six tablet sizes, plus a dedicated native tablet landscape capture at
1920×1200 px / 240 dpi. The saved profile contains only the resolved age group.
The portrait and landscape capture scripts temporarily remove and then restore
only the age-profile preference; they preserve the rest of the app data.

Reguli:

- adaugă capturile într-un subdirector descriptiv, nu în rădăcina proiectului;
- păstrează doar matricea relevantă de dispozitive/rezoluții și elimină cadrele
  intermediare înainte de commit;
- numele trebuie să descrie ecranul și profilul testat;
- dump-urile ANR, logurile `scrcpy` și capturile exploratorii rămân locale și
  sunt ignorate de Git.
