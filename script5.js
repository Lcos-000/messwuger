const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

const headEndStr = "</style>";
const fontFaceStr = 
@font-face {
  font-family: 'CustomFont';
  src: url('/fonts/custom-font.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}
</style>
;

content = content.replace("</style>", fontFaceStr);
fs.writeFileSync(path, content, 'utf8');
console.log('Profile.vue font-face injected');
