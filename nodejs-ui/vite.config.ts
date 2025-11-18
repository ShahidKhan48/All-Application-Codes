import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";
import path from "path";

export default defineConfig({
  server: {
    host: "::",
    port: 8080,
  },
  plugins: [
    react(),
    VitePWA({
      registerType: "autoUpdate",

      // 👇 disable caching
      strategies: "injectManifest", // use empty service worker
      srcDir: "src",
      filename: "sw.js",

      manifest: {
        name: "Agent App",
        short_name: "AgentApp",
        description: "Install Agnet app on your Device",
        theme_color: "#ffffff",
        background_color: "#ffffff",
        display: "standalone",
        start_url: "/",
        icons: [
          {
            src: "/ninja-logo.png",
            sizes: "192x192",
            type: "image/png",
          },
          {
            src: "/ninja-logo.png",
            sizes: "512x512",
            type: "image/png",
          },
        ],
      },
    }),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
