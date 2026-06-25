const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

// It seems step 8 failed to apply, let's re-apply it manually
content = content.replace('if (profileStyle.value.wallpaper) {\n      style[\'--page-wallpaper\'] = \linear-gradient(rgba(240, 244, 251, \), rgba(240, 244, 251, \)), url(\)\\n    }\n  \n    return style', 'if (profileStyle.value.wallpaper) {\n      style[\'--page-wallpaper\'] = \linear-gradient(rgba(240, 244, 251, \), rgba(240, 244, 251, \)), url(\)\\n    }\n\n    if (globalFontEnabled.value) {\n      style[\"font-family\"] = \"\'\" + PROFILE_VIEW_CONFIG.FONT_FACE.family + \"\', -apple-system, BlinkMacSystemFont, \\\"Segoe UI\\\", Roboto, \\\"Helvetica Neue\\\", Arial, \\\"Noto Sans\\\", sans-serif\"\n    }\n  \n    return style');

fs.writeFileSync(path, content, 'utf8');
console.log('Profile.vue patched part 8');
