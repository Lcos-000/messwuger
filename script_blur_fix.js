const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

const regex = /'--card-blur': `px`,/g;
content = content.replace(regex, "'--card-blur': \\px\,");

fs.writeFileSync(path, content, 'utf8');
