const defaultTheme = require("tailwindcss/defaultTheme");

module.exports = {
  // in prod look at shadow-cljs output file in dev look at runtime, which will change files that are actually compiled; postcss watch should be a whole lot faster
  content:
    process.env.NODE_ENV == "production"
      ? ["./public/js/main.js"]
      : ["./src/main/**/*.cljs", "./public/js/cljs-runtime/*.js"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter var", ...defaultTheme.fontFamily.sans],
      },
      colors: {
        primary: {
          DEFAULT: "#8fbc8f",
          foreground: "#ffffff",
        },
        secondary: {
          DEFAULT: "rgb(240 253 250 / var(--tw-bg-opacity, 1))",
        },
        destructive: {
          DEFAULT: "rgb(244 63 94 / var(--tw-bg-opacity, 1))",
          foreground: "#ffffff",
        },
        zombie: {
          background: "#1e1e1e", // main background
          card: "#2a2a2a", // card containers
          green: "#8fbc8f", // zombie flesh tone
          red: "#cc6666", // blood accents
          neon: "#c9f805", // radioactive green
          text: "#f1f1f1", // primary text
          transparent: "transparent",
        },
      },
    },
  },
  plugins: [require("@tailwindcss/forms")],
};
