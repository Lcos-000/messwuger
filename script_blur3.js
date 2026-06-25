const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

content = content.replace("'--card-blur': \px,", "'--card-blur': ${cardBlurControl.value}px,");
// Ah, the original has a backslash before $ which caused syntax issues. Let's fix it by exact string search again.
content = content.replace("'--card-blur': \px,", "'--card-blur': \${cardBlurControl.value}px,");

fs.writeFileSync(path, content, 'utf8');
