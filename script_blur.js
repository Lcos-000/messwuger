const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

// It's not matching properly. I'll use regex.
content = content.replace(/'--card-blur': \$\{PROFILE_VIEW_CONFIG\.CARD_BLUR\}px,/g, "'--card-blur': ${cardBlurControl.value}px,");

fs.writeFileSync(path, content, 'utf8');
console.log('Fixed card blur via regex.');
