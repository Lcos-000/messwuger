const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

const fontFaceStr = "\n@font-face {\n  font-family: 'CustomFont';\n  src: url('/fonts/custom-font.ttf') format('truetype');\n  font-weight: normal;\n  font-style: normal;\n}\n</style>";

content = content.replace("</style>", fontFaceStr);
fs.writeFileSync(path, content, 'utf8');
console.log('Profile.vue font-face injected');
