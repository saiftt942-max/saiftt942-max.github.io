import fs from 'node:fs';
import vm from 'node:vm';

const files = ['niyvexa/index.html', 'niyvexa/app.html'];
let checked = 0;
for (const file of files) {
  const html = fs.readFileSync(file, 'utf8');
  const scripts = [...html.matchAll(/<script\b([^>]*)>([\s\S]*?)<\/script\s*>/gi)];
  for (const [, attrs, code] of scripts) {
    if (/\bsrc\s*=/.test(attrs) || !code.trim()) continue;
    new vm.Script(code, { filename: file });
    checked++;
  }
}
if (!checked) throw new Error('No inline JavaScript blocks found.');
console.log(`Validated syntax for ${checked} inline JavaScript blocks.`);
