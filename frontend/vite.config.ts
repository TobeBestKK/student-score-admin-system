/*
 * @Author: LK
 * @version:
 * @Date: 2026-06-11 18:35:14
 * @LastEditors: your name
 * @LastEditTime: 2026-06-11 18:44:38
 * @Description:
 */
import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
});
