import { describe, expect, it, vi } from "vitest";
import { sum } from "../src/sum";

describe("pokedex", () => {
  it("should size of pokemons equals to 358", () => {
    expect(sum(2, 3)).toEqual(5);
  });
});
