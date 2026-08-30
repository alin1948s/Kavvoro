# Generated handoff packages

Acest director este rezervat pachetelor temporare de predare. Conținutul său
nu este sursă canonică și nu se versioneză, cu excepția acestui manifest.

Folosește direct sursele versionate:

- aplicație și teste: `app/`;
- artă sursă: `art/` și `figma-assets/`;
- capturi QA curate: `screenshots/`;
- materiale Google Play: `store-assets/`;
- documentație și decizii: `docs/`.

Un handoff nou se generează într-un director temporar sau în acest folder,
se validează, se livrează separat și nu se adaugă în Git. Nu copia repository-ul
în interiorul lui și nu comite arhive ZIP/APK/AAB.
