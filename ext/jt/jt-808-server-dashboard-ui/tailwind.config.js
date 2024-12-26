import { nextui } from "@nextui-org/theme";

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/layouts/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./node_modules/@nextui-org/theme/dist/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
        maxWidth: {
          '8xl': '96rem',
        },
        backgroundImage: {
            'regal-blue': 'radial-gradient(50% 50% at 50% 50%, rgba(147, 83, 211, 0.8) 0%, rgba(125, 78, 255, 0) 100%)',
        }
    },
  },
  darkMode: "class",
  plugins: [nextui({
    prefix: "xc",
  })],
};
