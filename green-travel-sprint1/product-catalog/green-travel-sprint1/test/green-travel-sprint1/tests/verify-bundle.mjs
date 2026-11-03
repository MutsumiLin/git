import { readFile, access } from 'node:fs/promises'
import { resolve } from 'node:path'

const root = resolve(process.argv[2] || '.')
const manifest = JSON.parse(await readFile(resolve(root, 'docs/team/FILE_OWNERSHIP.json'), 'utf8'))
const missing = []
const counts = {}
for (const [path, owner] of Object.entries(manifest.files)) {
  counts[owner] = (counts[owner] || 0) + 1
  try { await access(resolve(root, path)) } catch { missing.push(path) }
}
if (missing.length) {
  console.error('缺少文件：\n' + missing.join('\n'))
  process.exitCode = 1
} else {
  console.log('六份代码已合并完整：' + Object.entries(counts).map(([owner, count]) => `${owner} ${count} 个文件`).join('；'))
}
