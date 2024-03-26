/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

// combine.ts
const inputText = await Deno.readTextFile("input.txt");
const outputText = await Deno.readTextFile("output.txt");

const inputLines = inputText.split("\n");
const values = outputText.split("\n");

const combined = inputLines.map((line, i) => {
    const [beginning, _] = line.split("=");
    const value = values[i] || "";
    return `${beginning}=${value}`;
});

await Deno.writeTextFile("combined.txt", combined.join("\n"));