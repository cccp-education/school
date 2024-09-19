import typescriptLogo from "./typescript.svg";
import viteLogo from "./vite.svg";
import bootstrapLogo from "./bootstrap.svg";

const footer = () =>
  (document.querySelector<HTMLDivElement>("#hero")!.innerHTML = `
      <div id="stack">
        <a href="https://vitejs.dev" target="_blank">
          <img src="${viteLogo}" 
                class="logo" 
                alt="Vite logo" />
        </a>
        <a href="https://www.typescriptlang.org/" target="_blank">
          <img src="${typescriptLogo}" 
                class="logo vanilla" 
                alt="TypeScript logo" />
        </a>
        <a href="https://getbootstrap.com/" target="_blank">
          <img src="${bootstrapLogo}" 
                class="logo vanilla" 
                alt="Bootstrap logo" />
        </a>
        </div>
        `);

export default footer;
