export const SCHEDULE_CONFIG = {
  TEXT: {
    PREV_WEEK_TITLE: '上一周',
    NEXT_WEEK_TITLE: '下一周',
    THIS_WEEK_BUTTON: '本周',
    FULL_TERM_BUTTON: '全学期',
    REFRESH_BUTTON: '刷新',
    CURRENT_WEEK_LABEL: '第 {week} 周',
    FULL_TERM_LABEL: '全学期课表',
    PERIOD_HEADER: '节',
    LOADING_TEXT: '加载课表中…',
    SYNCING_TITLE: '数据同步中',
    SYNCING_TEXT: '正在为获取最新课表，请稍候…',
    EMPTY_TERM_TEXT: '本学期暂无课程',
    EMPTY_WEEK_TEXT: '本周暂无课程',
    STACK_TITLE: '该时段有 {count} 门课',
    CLOSE_BUTTON: '关闭',
    FIELD_CLASSROOM: '教室',
    FIELD_TEACHER: '教师',
    FIELD_PERIOD: '节次',
    FIELD_WEEK: '周次',
    FIELD_CAMPUS: '校区',
    WEEKDAY_PREFIX: '周'
  },
  SEMESTER_START_DATE: '2026-07-05',
  SEMESTER_START_RULES: {
    SPRING: { month: 3, day: 1 },
    AUTUMN: { month: 9, day: 1 }
  },
  MAX_WEEK: 25,
  TOTAL_PERIODS: 14,
  DESKTOP_ROW_HEIGHT: 58,
  MOBILE_ROW_HEIGHT: 52,
  MOBILE_BREAKPOINT: 768,
  SMALL_MOBILE_BREAKPOINT: 360,
  EMPTY_SCHEDULE_VALUES: ['', '[]', '{}'],
  DAYS: ['一', '二', '三', '四', '五', '六', '日'],
  PERIOD_TIME: [
    '8:00-8:45', '8:55-9:40', '10:00-10:45', '10:55-11:40',
    '12:10-12:55', '13:05-13:50', '14:00-14:45', '14:55-15:40',
    '15:50-16:35', '16:55-17:40', '17:50-18:35', '19:20-20:05',
    '20:15-21:00', '21:10-21:55'
  ],
  COURSE_COLORS: [
    ['#54719f', '#eef3fb'],
    ['#4f837c', '#edf8f6'],
    ['#a06d4e', '#fbf1e9'],
    ['#7869a6', '#f2f0fa'],
    ['#a76687', '#fbf0f5'],
    ['#4d8292', '#edf7fa'],
    ['#9b7a3f', '#faf5e8'],
    ['#9a5f61', '#fbefef'],
    ['#4f8171', '#edf8f3'],
    ['#626fa4', '#f0f2fb'],
    ['#748a4a', '#f4f8eb'],
    ['#8066a1', '#f4f0fa']
  ],
  DEFAULT_COURSE_COLOR: '#54719f'
}
