const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

const targetStr = "'--card-blur': \px,";
const replaceStr = "'--card-blur': \px,";
content = content.replace(targetStr, replaceStr);

fs.writeFileSync(path, content, 'utf8');
