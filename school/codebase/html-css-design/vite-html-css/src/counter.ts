export function setupCounter() {
  const element = document.querySelector<HTMLButtonElement>("#counter")!;
  let counter = 0;
  const setCounter = (count: number) => {
    counter = count;
    element.innerHTML = `likes: ${counter}`;
  };
  element.addEventListener("click", () => setCounter(counter + 1));
  setCounter(0);
}
