const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

const targetStr = "if (res.data.cardOpacity !== null && res.data.cardOpacity !== undefined) {";
const replaceStr = "if (res.data.cardBlur !== null && res.data.cardBlur !== undefined) {\n          cardBlurControl.value = Number(res.data.cardBlur)\n        }\n        if (res.data.globalFontEnabled !== null && res.data.globalFontEnabled !== undefined) {\n          globalFontEnabled.value = res.data.globalFontEnabled === 1\n        }\n        if (res.data.cardOpacity !== null && res.data.cardOpacity !== undefined) {";

content = content.replace(targetStr, replaceStr);
fs.writeFileSync(path, content, 'utf8');
console.log('Added property reads from backend.');
