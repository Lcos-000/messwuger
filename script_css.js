const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

const styleBlock = "\n/* ---- Toggle Switch ---- */\n.toggle-switch {\n  position: relative;\n  display: inline-block;\n  width: 44px;\n  height: 24px;\n  flex-shrink: 0;\n}\n.toggle-switch input {\n  opacity: 0;\n  width: 0;\n  height: 0;\n}\n.toggle-slider {\n  position: absolute;\n  cursor: pointer;\n  top: 0;\n  left: 0;\n  right: 0;\n  bottom: 0;\n  background-color: #cbd5e1;\n  transition: .3s cubic-bezier(0.4, 0.0, 0.2, 1);\n  border-radius: 24px;\n}\n.toggle-slider:before {\n  position: absolute;\n  content: '';\n  height: 18px;\n  width: 18px;\n  left: 3px;\n  bottom: 3px;\n  background-color: white;\n  transition: .3s cubic-bezier(0.4, 0.0, 0.2, 1);\n  border-radius: 50%;\n  box-shadow: 0 2px 4px rgba(0,0,0,0.15);\n}\ninput:checked + .toggle-slider {\n  background-color: var(--hero-theme-start);\n}\ninput:checked + .toggle-slider:before {\n  transform: translateX(20px);\n}\n.font-setting-block {\n  padding-top: 4px;\n}\n";

content = content.replace('/* ---- Action rows ---- */', styleBlock + '\n/* ---- Action rows ---- */');

fs.writeFileSync(path, content, 'utf8');
console.log('Toggle switch CSS added.');
