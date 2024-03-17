// propertyConverter.ts

// Read the input file
const inputFile = await Deno.readTextFile(Deno.args[0]);

// Regular expression patterns to match @Property annotations
const propertyPattern2 = /@Property\((.+?)\s{4}\)/gs;

const i18nKeys: { [key: string]: string } = {};
const i18nCategories: Map<string, string> = new Map();
const i18nSubcategories: Map<string, string> = new Map();

// Process each @Property annotation
let updatedFile = inputFile;

for (const match of inputFile.matchAll(propertyPattern2)) {
    const [originalAnnotation, properties] = match;
    updateAnnotation(originalAnnotation, properties);
}

// Write the updated file with modified annotations
await Deno.writeTextFile('updatedFile.kt', updatedFile);

// Generate the en_US.lang file
const langFileLines = Object.entries(i18nKeys)
    .map(([key, value]) => `${key}=${value}`);

// Add i18nCategory and i18nSubcategory entries
for (const [key, value] of i18nCategories) {
    langFileLines.push(`${key}=${value}`);
}

for (const [key, value] of i18nSubcategories) {
    langFileLines.push(`${key}=${value}`);
}

const langFileContent = langFileLines.join('\n');

await Deno.writeTextFile('en_US.lang', langFileContent);

console.log('updatedFile.kt and en_US.lang files generated successfully!');

function updateAnnotation(originalAnnotation: string, properties: string) {
    const propMap = new Map<string, string>();
    const propRegex = /(\w+)\s*=\s*(".*?"|'.*?'|[^,\s]+)/g;
    for (const prop of properties.matchAll(propRegex)) {
        const [_, key, value] = prop;
        propMap.set(key, value.replace(/^(")|("$)/g, '').replace(/^(')|('$)/g, ''));
    }

    const name = propMap.get('name') || '';
    const category = propMap.get('category') || '';
    const subcategory = propMap.get('subcategory') || '';

    const normalizedName = name.toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_]/g, '');
    const normalizedCategory = category.toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_]/g, '');
    const normalizedSubcategory = subcategory.toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_]/g, '');
    const i18nKey = `skytils.config.${normalizedCategory}.${normalizedSubcategory}.${normalizedName}`;

    i18nKeys[i18nKey] = name;

    const i18nName = `skytils.config.${normalizedCategory}.${normalizedSubcategory}.${normalizedName}`;
    const i18nCategory = `skytils.config.${normalizedCategory}`;
    const i18nSubcategory = `skytils.config.${normalizedCategory}.${normalizedSubcategory}`;

    i18nCategories.set(i18nCategory, category);
    i18nSubcategories.set(i18nSubcategory, subcategory);

    const newAnnotation = `@Property(${properties}\n, i18nName = "${i18nName}", i18nCategory = "${i18nCategory}", i18nSubcategory = "${i18nSubcategory}")`;
    updatedFile = updatedFile.replace(originalAnnotation, newAnnotation);
}