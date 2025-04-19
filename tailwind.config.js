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
          DEFAULT: "rgb(20 184 166 / var(--tw-bg-opacity, 1))",
          foreground: "#ffffff",
        },
        secondary: {
          DEFAULT: "rgb(240 253 250 / var(--tw-bg-opacity, 1))",
        },
        destructive: {
          DEFAULT: "rgb(244 63 94 / var(--tw-bg-opacity, 1))",
          foreground: "#ffffff",
        },
      },
    },
  },
  plugins: [require("@tailwindcss/forms")],
};
