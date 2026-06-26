 Web app a APK con Ionic + Capacitor
Prerequisitos

Node.js instalado
Android Studio instalado


1 — Instalar Ionic CLI
bashnpm install -g @ionic/cli

2 — Crear proyecto Ionic
bashionic start agrolink-mobile blank --type=react
cd agrolink-mobile

3 — Instalar Capacitor Android
bashnpm install @capacitor/android
npx cap add android

4 — Editar capacitor.config.ts
typescriptimport type { CapacitorConfig } from '@capacitor/cli';


const config: CapacitorConfig = {
  appId: 'com.agrolink.app',
  appName: 'AgroLink',
  webDir: 'dist',
  server: {
    url: 'https://agrolink-frontend.onrender.com', // ← tu URL desplegada
    cleartext: true
  }
};


export default config;

5 — Build y sync
bashnpm run build
npx cap sync android
npx cap open android
6 — En Android Studio
Build → Build App Bundle(s) / APK(s) → Build APK(s)
Esperar y clic en "locate" para encontrar el app-debug.apk.
