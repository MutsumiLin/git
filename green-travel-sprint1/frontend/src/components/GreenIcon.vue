<script setup>
defineProps({ name: { type: String, default: 'leaf' } })
const paths = {
  leaf: 'M20 4c-8-1-15 2-15 8a6 6 0 0 0 6 6c6 0 9-7 9-14ZM4 21l11-11M9 16v-5m0 5h5',
  home: 'm3 10 9-7 9 7v10H3V10Zm6 10v-7h6v7',
  route: 'M6 6h10a4 4 0 0 1 0 8H8a4 4 0 0 0 0 8h10M6 3v6M3 6h6m9 13v3',
  coins: 'M20 6c0 2-4 4-8 4S4 8 4 6s4-4 8-4 8 2 8 4ZM4 6v6c0 2 4 4 8 4s8-2 8-4V6M4 12v6c0 2 4 4 8 4s8-2 8-4v-6',
  shop: 'M4 10v10h16V10M3 4h18l-1 6h-4l-2-6-2 6H8L6 4m3 16v-6h6v6',
  grid: 'M3 3h7v7H3V3Zm11 0h7v7h-7V3ZM3 14h7v7H3v-7Zm11 0h7v7h-7v-7Z',
  logout: 'M9 4H4v16h5m6-12 5 4-5 4m-7-4h12',
  arrow: 'M4 12h16m-6-6 6 6-6 6',
  plus: 'M12 5v14M5 12h14',
  refresh: 'M20 8a8 8 0 1 0 1 7M20 3v5h-5',
  info: 'M12 11v6m0-10h.01M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z',
  bus: 'M5 18V5c0-3 14-3 14 0v13H5Zm0-8h14M7 18v3m10-3v3M8 14h.01M16 14h.01',
  metro: 'M5 16V5c0-4 14-4 14 0v11l-4 4H9l-4-4Zm0-7h14M8 14h.01M16 14h.01M9 20l-3 3m9-3 3 3',
  bike: 'M9 17a4 4 0 1 1-8 0 4 4 0 0 1 8 0Zm14 0a4 4 0 1 1-8 0 4 4 0 0 1 8 0ZM5 17l5-9 5 9H5Zm5-9h7l2 9M8 5h5m3-1h3l1 4',
  cup: 'M5 4h12v12a5 5 0 0 1-10 0L5 4Zm12 2h2a3 3 0 1 1 0 6h-2M4 22h15',
  bag: 'M5 7h14l2 15H3L5 7Zm3 0V5a4 4 0 0 1 8 0v2',
}
</script>
<template><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path :d="paths[name] || paths.leaf" /></svg></template>
