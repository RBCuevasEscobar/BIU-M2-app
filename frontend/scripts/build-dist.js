#!/usr/bin/env node
/**
 * build-dist.js
 * -------------
 * Genera el directorio /dist listo para despliegue en Azure Static Web Apps.
 *
 * Pasos:
 *   1. Limpia y crea dist/
 *   2. Copia todos los .html del raíz → dist/
 *   3. Copia js/ completo              → dist/js/
 *   4. Compila Tailwind CSS            → dist/css/output.css
 *
 * Sin dependencias externas — solo Node.js built-ins (fs, path, child_process).
 */

'use strict';

const fs   = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// ─── Rutas ────────────────────────────────────────────────────────────────────
const ROOT = path.resolve(__dirname, '..');           // frontend/
const DIST = path.join(ROOT, 'dist');                 // frontend/dist/

// ─── Helpers ──────────────────────────────────────────────────────────────────

/** Crea un directorio (y padres) si no existe. */
function mkdirp(dir) {
    fs.mkdirSync(dir, { recursive: true });
}

/** Copia un archivo de src a dest, creando el directorio destino si hace falta. */
function copyFile(src, dest) {
    mkdirp(path.dirname(dest));
    fs.copyFileSync(src, dest);
}

/**
 * Copia recursivamente todos los archivos de srcDir a destDir.
 * @param {string} srcDir  - Directorio origen.
 * @param {string} destDir - Directorio destino.
 */
function copyDir(srcDir, destDir) {
    const entries = fs.readdirSync(srcDir, { withFileTypes: true });
    for (const entry of entries) {
        const srcPath  = path.join(srcDir, entry.name);
        const destPath = path.join(destDir, entry.name);
        if (entry.isDirectory()) {
            copyDir(srcPath, destPath);
        } else {
            copyFile(srcPath, destPath);
        }
    }
}

// ─── Paso 1: Limpiar y crear dist/ ────────────────────────────────────────────
console.log('\n📦  Build iniciado — generando dist/\n');

if (fs.existsSync(DIST)) {
    fs.rmSync(DIST, { recursive: true, force: true });
    console.log('  🗑   dist/ anterior eliminado');
}
mkdirp(DIST);
console.log('  ✅  dist/ creado');

// ─── Paso 2: Copiar archivos HTML del raíz ────────────────────────────────────
const htmlFiles = fs.readdirSync(ROOT).filter(f => f.endsWith('.html'));

if (htmlFiles.length === 0) {
    console.error('\n❌  ERROR: No se encontraron archivos .html en el raíz del frontend.');
    process.exit(1);
}

for (const file of htmlFiles) {
    copyFile(path.join(ROOT, file), path.join(DIST, file));
}
console.log(`  ✅  ${htmlFiles.length} archivos HTML copiados → dist/`);

// ─── Paso 3: Copiar directorio js/ ────────────────────────────────────────────
const jsSrc  = path.join(ROOT, 'js');
const jsDest = path.join(DIST, 'js');

if (!fs.existsSync(jsSrc)) {
    console.error('\n❌  ERROR: No se encontró el directorio js/ en el frontend.');
    process.exit(1);
}

copyDir(jsSrc, jsDest);
const jsCount = fs.readdirSync(jsDest).length;
console.log(`  ✅  ${jsCount} archivos JS copiados → dist/js/`);

// ─── Paso 4: Compilar Tailwind CSS → dist/css/output.css ─────────────────────
const cssDist = path.join(DIST, 'css');
mkdirp(cssDist);

const cssIn  = path.join(ROOT, 'css', 'styles.css');
const cssOut = path.join(cssDist, 'output.css');

if (!fs.existsSync(cssIn)) {
    console.error('\n❌  ERROR: No se encontró css/styles.css.');
    process.exit(1);
}

console.log('  ⏳  Compilando Tailwind CSS...');
try {
    execSync(
        `npx @tailwindcss/cli -i "${cssIn}" -o "${cssOut}" --minify`,
        { cwd: ROOT, stdio: 'inherit' }
    );
} catch (err) {
    console.error('\n❌  ERROR al compilar Tailwind CSS:', err.message);
    process.exit(1);
}

// Verificar que el CSS resultante no esté vacío
const cssSize = fs.statSync(cssOut).size;
if (cssSize === 0) {
    console.error('\n❌  ERROR: dist/css/output.css está vacío. Revisa la config de Tailwind.');
    process.exit(1);
}
console.log(`  ✅  Tailwind compilado → dist/css/output.css (${(cssSize / 1024).toFixed(1)} KB)`);

// ─── Paso 5: Copiar staticwebapp.config.json (si existe) ─────────────────────
const swaConfig = path.join(ROOT, 'staticwebapp.config.json');
if (fs.existsSync(swaConfig)) {
    copyFile(swaConfig, path.join(DIST, 'staticwebapp.config.json'));
    console.log('  ✅  staticwebapp.config.json copiado → dist/');
}

// ─── Resumen final ────────────────────────────────────────────────────────────
console.log('\n✨  Build completado exitosamente.\n');
console.log('  Contenido de dist/:');

function listDist(dir, prefix = '    ') {
    const items = fs.readdirSync(dir, { withFileTypes: true });
    for (const item of items) {
        if (item.isDirectory()) {
            console.log(`${prefix}📁 ${item.name}/`);
            listDist(path.join(dir, item.name), prefix + '  ');
        } else {
            const size = fs.statSync(path.join(dir, item.name)).size;
            console.log(`${prefix}📄 ${item.name} (${(size / 1024).toFixed(1)} KB)`);
        }
    }
}
listDist(DIST);
console.log('\n  🚀  Listo para desplegar en Azure Static Web Apps.\n');
