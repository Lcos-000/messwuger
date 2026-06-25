const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

const targetStr = "if (profileStyle.value.wallpaper) {";
const replacementStr = if (globalFontEnabled.value) {
      style["font-family"] = "'" + PROFILE_VIEW_CONFIG.FONT_FACE.family + "', -apple-system, BlinkMacSystemFont, \\"Segoe UI\\", Roboto, \\"Helvetica Neue\\", Arial, \\"Noto Sans\\", sans-serif";
    }

    if (profileStyle.value.wallpaper) {;

content = content.replace(targetStr, replacementStr);
fs.writeFileSync(path, content, 'utf8');
console.log('Profile.vue patched part 8 again');
