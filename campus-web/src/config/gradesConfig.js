export const GRADES_CONFIG = {
  TEXT: {
    PAGE_TITLE: '成绩查询',
    PAGE_SUBTITLE: '选择学年与学期后查询成绩，可重新提交任务检查成绩是否有更新。',
    ACADEMIC_YEAR_LABEL: '学年',
    SEMESTER_LABEL: '学期',
    VIEW_LABEL: '视图',
    SORT_LABEL: '排序',
    CURRENT_FILTER_LABEL: '当前筛选',
    RECORD_COUNT_LABEL: '记录数',
    LOADING: '正在加载成绩数据...',
    EMPTY_TITLE: '暂无成绩',
    EMPTY_DESCRIPTION: '当前筛选条件下暂无成绩记录。',
    SCORE_LABEL: '成绩',
    COURSE_CODE_EMPTY: '暂无课程代码',
    QUERYING_BUTTON_TEXT: '查询中...',
    SUBMITTING_BUTTON_TEXT: '提交中...',
    QUERY_BUTTON_TIP: '查询当前筛选条件下已获取的成绩数据。',
    SUBMIT_BUTTON_TIP: '从教务系统重新获取最新成绩。提交后请稍等，再点击“查询成绩”查看结果。'
  },
  VIEW_MODE: {
    CARD: 'card',
    TABLE: 'table'
  },
  SORT_MODE: {
    DEFAULT: 'default',
    SCORE_DESC: 'scoreDesc'
  },
  DEFAULT_SEMESTER_INDEX: 2,
  SEMESTER_OPTIONS: [
    { label: '3', value: '3', description: '学期上 / 秋季' },
    { label: '6', value: '6', description: '其他 / 小学期' },
    { label: '12', value: '12', description: '学期下 / 春季' }
  ],
  VIEW_OPTIONS: [
    { label: '卡片视图', value: 'card' },
    { label: '表格视图', value: 'table' }
  ],
  SORT_OPTIONS: [
    { label: '默认排序', value: 'default' },
    { label: '成绩降序', value: 'scoreDesc' }
  ],
  CARD_META_FIELDS: [
    { label: '课程代码', key: 'courseCode' },
    { label: '绩点', key: 'gpa' },
    { label: '学分', key: 'credit' },
    { label: '教师', key: 'teacher' },
    { label: '课程性质', key: 'courseNature' },
    { label: '考核性质', key: 'examNature' }
  ],
  TABLE_COLUMNS: [
    { label: '课程名称', key: 'courseName', className: 'col-course' },
    { label: '课程代码', key: 'courseCode', className: 'col-code' },
    { label: '成绩', key: 'score', className: 'col-score' },
    { label: '绩点', key: 'gpa', className: 'col-gpa' },
    { label: '学分', key: 'credit', className: 'col-credit' },
    { label: '教师', key: 'teacher', className: 'col-teacher' },
    { label: '课程性质', key: 'courseNature', className: 'col-nature' },
    { label: '考核性质', key: 'examNature', className: 'col-exam' }
  ],
  UI: {
    SHELL_MAX_WIDTH: 1120,
    PANEL_RADIUS: 18,
    CONTROL_RADIUS: 12,
    SCORE_RADIUS: 16,
    TABLE_MIN_WIDTH: 980,
    PANEL_BG: 'rgba(255, 255, 255, 0.82)',
    PANEL_BORDER: 'rgba(225, 232, 244, 0.9)',
    PANEL_SHADOW: '0 10px 30px rgba(20, 32, 51, 0.06)',
    PANEL_BLUR: 14,
    PRIMARY_COLOR: '#4f86f7',
    PRIMARY_SURFACE: '#eef4ff',
    PRIMARY_TEXT: '#2f67da',
    SECONDARY_SURFACE: '#edf3ff',
    SECONDARY_TEXT: '#376ddc',
    TEXT_MAIN: '#142033',
    TEXT_SUB: '#6f7f99',
    TEXT_MUTED: '#7f8da5',
    TEXT_TABLE: '#22324b',
    TABLE_HEAD_BG: 'rgba(244, 247, 253, 0.96)',
    TABLE_ROW_BG: 'rgba(255, 255, 255, 0.75)',
    TABLE_ROW_HOVER_BG: 'rgba(241, 246, 255, 0.9)',
    META_ITEM_BG: 'rgba(246, 249, 255, 0.88)',
    META_ITEM_BORDER: 'rgba(223, 232, 247, 0.92)',
    SCORE_PILL_BG: 'linear-gradient(135deg, rgba(79, 134, 247, 0.14), rgba(111, 160, 255, 0.22))',
    SCORE_LABEL_COLOR: '#6f89c7'
  },
  YEAR_RANGE_BEFORE: 2,
  YEAR_RANGE_AFTER: 1,
  EMPTY_TEXT: '当前筛选条件下暂无成绩记录。',
  SUBMITTING_TEXT: '已提交成绩查询任务，请稍后刷新查看结果。',
  QUERY_BUTTON_TEXT: '查询成绩',
  SUBMIT_BUTTON_TEXT: '获取最新成绩'
}
