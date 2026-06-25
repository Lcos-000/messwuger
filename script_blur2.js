const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

const oldStr = "'--card-blur': \$" + "{PROFILE_VIEW_CONFIG.CARD_BLUR}px,";
const newStr = "'--card-blur': \$" + "{cardBlurControl.value}px,";

content = content.replace(oldStr, newStr);

fs.writeFileSync(path, content, 'utf8');
console.log('Fixed card blur mapping with exact string.');
