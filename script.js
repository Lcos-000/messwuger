const fs = require('fs');
const path = 'E:/develop/idea/collaborative project/messwuger/campus-web/src/views/Profile.vue';
let content = fs.readFileSync(path, 'utf8');

// 1. Add variables
content = content.replace('const cardOpacityControl = ref(PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_DEFAULT)', 'const cardOpacityControl = ref(PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_DEFAULT)\n  const cardBlurControl = ref(PROFILE_VIEW_CONFIG.CARD_BLUR_DEFAULT)\n  const globalFontEnabled = ref(false)');

// 2. Add timers
content = content.replace('let cardOpacitySaveTimer = null', 'let cardOpacitySaveTimer = null\n  let cardBlurSaveTimer = null\n  let globalFontSaveTimer = null');

// 3. Load from API
content = content.replace('if (res.data.cardOpacity !== null && res.data.cardOpacity !== undefined) {\n          cardOpacityControl.value = Number(res.data.cardOpacity)\n        }', 'if (res.data.cardOpacity !== null && res.data.cardOpacity !== undefined) {\n          cardOpacityControl.value = Number(res.data.cardOpacity)\n        }\n        if (res.data.cardBlur !== null && res.data.cardBlur !== undefined) {\n          cardBlurControl.value = Number(res.data.cardBlur)\n        }\n        if (res.data.globalFontEnabled !== null && res.data.globalFontEnabled !== undefined) {\n          globalFontEnabled.value = Boolean(res.data.globalFontEnabled)\n        }');

// 4. Save to API
content = content.replace('cardOpacity: Number(cardOpacityControl.value.toFixed(2))', 'cardOpacity: Number(cardOpacityControl.value.toFixed(2)),\n        cardBlur: Number(cardBlurControl.value),\n        globalFontEnabled: Boolean(globalFontEnabled.value)');

// 5. Add saveGlobalFont
content = content.replace('const persistProfileStyle = async', 'const saveGlobalFont = () => {\n    if (globalFontSaveTimer) {\n      clearTimeout(globalFontSaveTimer)\n    }\n    globalFontSaveTimer = setTimeout(() => {\n      saveProfileStyle()\n    }, PROFILE_VIEW_CONFIG.CARD_OPACITY_SAVE_DEBOUNCE)\n  }\n\n  const persistProfileStyle = async');

// 6. Clear timers
content = content.replace('if (cardOpacitySaveTimer) {\n      clearTimeout(cardOpacitySaveTimer)\n      cardOpacitySaveTimer = null\n    }', 'if (cardOpacitySaveTimer) {\n      clearTimeout(cardOpacitySaveTimer)\n      cardOpacitySaveTimer = null\n    }\n    if (cardBlurSaveTimer) {\n      clearTimeout(cardBlurSaveTimer)\n      cardBlurSaveTimer = null\n    }\n    if (globalFontSaveTimer) {\n      clearTimeout(globalFontSaveTimer)\n      globalFontSaveTimer = null\n    }');

// 7. Inject to style
content = content.replace('\'--card-blur\': \\px\,', '\'--card-blur\': \\px\,');

// 8. Add font family
content = content.replace('return style\n  })', 'if (globalFontEnabled.value) {\n      style[\"font-family\"] = \"\'\" + PROFILE_VIEW_CONFIG.FONT_FACE.family + \"\', -apple-system, BlinkMacSystemFont, \\\"Segoe UI\\\", Roboto, \\\"Helvetica Neue\\\", Arial, \\\"Noto Sans\\\", sans-serif\"\n    }\n\n    return style\n  })');

// 9. Watch card blur
content = content.replace('watch(cardOpacityControl, (value, oldValue) => {\n    if (!profileStyleLoaded.value || value === oldValue) return\n    if (cardOpacitySaveTimer) {\n      clearTimeout(cardOpacitySaveTimer)\n    }\n    cardOpacitySaveTimer = setTimeout(() => {\n      saveProfileStyle()\n    }, PROFILE_VIEW_CONFIG.CARD_OPACITY_SAVE_DEBOUNCE)\n  })', 'watch(cardOpacityControl, (value, oldValue) => {\n    if (!profileStyleLoaded.value || value === oldValue) return\n    if (cardOpacitySaveTimer) {\n      clearTimeout(cardOpacitySaveTimer)\n    }\n    cardOpacitySaveTimer = setTimeout(() => {\n      saveProfileStyle()\n    }, PROFILE_VIEW_CONFIG.CARD_OPACITY_SAVE_DEBOUNCE)\n  })\n\n  watch(cardBlurControl, (value, oldValue) => {\n    if (!profileStyleLoaded.value || value === oldValue) return\n    if (cardBlurSaveTimer) {\n      clearTimeout(cardBlurSaveTimer)\n    }\n    cardBlurSaveTimer = setTimeout(() => {\n      saveProfileStyle()\n    }, PROFILE_VIEW_CONFIG.CARD_OPACITY_SAVE_DEBOUNCE)\n  })');

fs.writeFileSync(path, content, 'utf8');
console.log('Profile.vue patched successfully');
