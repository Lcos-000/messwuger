const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

// Also remove static default in CSS block so it doesn't get messed up if reactivity fails
content = content.replace("--card-blur: 14px;", "");

content = content.replace("'--card-blur': \px,", "'--card-blur': \$" + "{cardBlurControl.value}px,");

fs.writeFileSync(path, content, 'utf8');
