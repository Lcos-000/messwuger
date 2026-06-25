const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

// Remove static font-family from CSS
content = content.replace("font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;", "");

// Set default font-family explicitly in the dynamic style logic if globalFontEnabled is false
const searchStr = 'if (globalFontEnabled.value) {';
const replaceStr = 'style["font-family"] = globalFontEnabled.value ? ("\'" + PROFILE_VIEW_CONFIG.FONT_FACE.family + "\', -apple-system, BlinkMacSystemFont, \\"Segoe UI\\", Roboto, \\"Helvetica Neue\\", Arial, \\"Noto Sans\\", sans-serif") : "-apple-system, BlinkMacSystemFont, \'Segoe UI\', \'PingFang SC\', sans-serif";\n    if (globalFontEnabled.value) {';

content = content.replace(searchStr, replaceStr);

fs.writeFileSync(path, content, 'utf8');
console.log('Fixed font-family handling.');
