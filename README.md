# dssa-personal-1

Program Java data mahasiswa (`src/Mahasiswa`), sekarang dilengkapi UI web (`WebServer.java`) memakai `com.sun.net.httpserver` bawaan JDK — tanpa dependency tambahan, sehingga bisa langsung dijalankan di Replit.

## Menjalankan

```bash
bash run.sh
```

Buka `http://localhost:3000` (atau port yang di-set lewat env `PORT`).

## Deploy & auto-update via Replit

1. Push repo ini ke GitHub (branch `main`).
2. Di [replit.com](https://replit.com), pilih **Create Repl** → **Import from GitHub**, lalu pilih repo ini. Replit akan mendeteksi `.replit` dan menjalankan `run.sh` secara otomatis.
3. Di halaman Repl, buka tab **Git** (ikon Git di sidebar) → **Connect to GitHub** jika belum otomatis terhubung dari langkah import.
4. Aktifkan **Auto-sync branch `main`** (nama fitur bisa berbeda tergantung versi Replit; intinya opsi "pull/sync automatically on push") supaya setiap `git push` ke `main` di GitHub otomatis menarik perubahan ke Repl, lalu Repl otomatis restart menjalankan `run.sh`.
5. Klik **Run** — webview Replit akan menampilkan UI di port 3000.

Catatan: fitur auto-sync GitHub → Replit dikonfigurasi lewat dashboard Replit (OAuth & toggle), bukan lewat file di repo ini, jadi langkah 3–4 perlu dilakukan manual di UI Replit.
