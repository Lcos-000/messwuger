export const EMPTY_CLASSROOM_CONFIG = {
  TEXT: {
    PAGE_TITLE: '空教室查询',
    PAGE_SUBTITLE: '图形化选择查询条件，提交任务后再查询结果。',
    ACADEMIC_YEAR_LABEL: '学年',
    SEMESTER_LABEL: '学期',
    DAY_LABEL: '星期',
    WEEK_LABEL: '周次',
    PERIOD_LABEL: '节次',
    CAMPUS_LABEL: '校区',
    BUILDING_LABEL: '楼栋',
    ROOM_TYPE_LABEL: '教室类型',
    SUMMARY_LABEL: '当前条件',
    STATUS_LABEL: '任务状态',
    SUBMIT_BUTTON: '提交空教室任务',
    SUBMITTING_BUTTON_TEXT: '提交中...',
    QUERYING_RESULT_BUTTON_TEXT: '查询中...',
    RESULT_QUERY_BUTTON: '查询结果',
    RESET_BUTTON: '重置选择',
    CURRENT_WEEK_BUTTON: '本周',
    WEEK_SELECTION_LABEL: '周次选择',
    PERIOD_SELECTION_LABEL: '节次选择',
    RESULT_LOADING: '正在查询空教室结果...',
    EMPTY_RESULT_TITLE: '暂无结果',
    PENDING_RESULT_TEXT: '任务已提交或正在执行，请稍后点击查询结果刷新。',
    EMPTY_STATUS: '尚未提交查询任务',
    EMPTY_STATUS_DESC: '先完成条件选择，再提交任务并刷新查看结果。'
  },
  YEAR_RANGE_BEFORE: 0,
  YEAR_RANGE_AFTER: 1,
  DEFAULT_SEMESTER_INDEX: 2,
  MAX_WEEK: 20,
  MAX_PERIOD: 14,
  SEMESTER_OPTIONS: [
    { label: '3', value: '3', description: '学期上 / 秋季' },
    { label: '6', value: '6', description: '其他 / 小学期' },
    { label: '12', value: '12', description: '学期下 / 春季' }
  ],
  DAY_OPTIONS: [
    { label: '周一', value: '1' },
    { label: '周二', value: '2' },
    { label: '周三', value: '3' },
    { label: '周四', value: '4' },
    { label: '周五', value: '5' },
    { label: '周六', value: '6' },
    { label: '周日', value: '7' }
  ],
  CAMPUS_OPTIONS: [
    { label: '南区', value: '1' },
    { label: '北区', value: '2' },
    { label: '荣昌校区', value: '3' }
  ],
  ROOM_TYPE_OPTIONS: [
    { label: '不限', value: '' }
  ],
  BUILDING_OPTIONS: {
    '1': [
      { label: '30教', value: '30' },
      { label: '31教', value: '31' },
      { label: '32教', value: '32' },
      { label: '33教', value: '33' },
      { label: '35教', value: '35' },
      { label: '36教', value: '36' },
      { label: '37教', value: '37' },
      { label: '38教', value: '38' },
      { label: '39教', value: '39' },
      { label: '40教', value: '40' },
      { label: '45教', value: '45' },
      { label: '46教', value: '46' },
      { label: '48教', value: '48' },
      { label: '96教', value: '96' },
      { label: '97教', value: '97' },
      { label: '98教', value: '98' },
      { label: 'Online Learning', value: '1001' },
      { label: '工科大楼A座', value: 'GKL-A' },
      { label: '工科大楼B座', value: 'GKL-B' },
      { label: '科技楼', value: 'KJL' },
      { label: '学生活动中心', value: 'HDZX' },
      { label: '无楼号', value: 'wlh' }
    ],
    '2': [
      { label: '01教', value: '01' },
      { label: '02教', value: '02' },
      { label: '03教', value: '03' },
      { label: '04教', value: '04' },
      { label: '05教', value: '05' },
      { label: '06教', value: '06' },
      { label: '07教', value: '07' },
      { label: '08教', value: '08' },
      { label: '09教', value: '09' },
      { label: '10教', value: '10' },
      { label: '11教', value: '11' },
      { label: '13教', value: '13' },
      { label: '14教', value: '14' },
      { label: '15教', value: '15' },
      { label: '16教', value: '16' },
      { label: '17教', value: '17' },
      { label: '19教', value: '19' },
      { label: '21教', value: '21' },
      { label: '23教', value: '23' },
      { label: '24教', value: '24' },
      { label: '25教', value: '25' },
      { label: '无楼号', value: 'wlh' }
    ],
    '3': [
      { label: '第零教学楼', value: 'RC00' },
      { label: '第一教学楼', value: 'RC01' },
      { label: '第二教学楼', value: 'RC02' },
      { label: '第三教学楼', value: 'RC03' },
      { label: '第四教学楼', value: 'RC04' },
      { label: '第五教学楼', value: 'RC05' },
      { label: '第七教学楼', value: 'RC7B' },
      { label: '第九教学楼', value: 'RC09' },
      { label: 'Online Learning', value: '1003' },
      { label: 'rc-无楼号', value: 'rwlh' },
      { label: '无楼号', value: 'wlh' }
    ]
  },
  STATUS_META: {
    RESULT_READY: {
      title: '结果已就绪',
      description: '后端已命中结果缓存，本次无需重复提交任务。',
      tone: 'success'
    },
    QUERYING: {
      title: '查询进行中',
      description: '已有同条件任务在执行，请稍后等待结果。',
      tone: 'info'
    },
    TIMEOUT: {
      title: '等待中',
      description: '本次查询尚未拿到结果，建议稍后再次查看。',
      tone: 'warn'
    },
    SUBMITTED: {
      title: '任务已提交',
      description: '已成功发起空教室查询任务，等待异步回调。',
      tone: 'primary'
    },
    FAILED: {
      title: '提交失败',
      description: '任务提交或处理失败，请稍后重试。',
      tone: 'danger'
    }
  },
  TABLE_COLUMNS: [
    { label: '楼栋', key: 'building', className: 'col-building' },
    { label: '教室代码', key: 'roomCode', className: 'col-room-code' },
    { label: '教室名称', key: 'roomName', className: 'col-room-name' },
    { label: '校区', key: 'campus', className: 'col-campus' },
    { label: '教室类型', key: 'roomType', className: 'col-room-type' },
    { label: '楼层', key: 'floor', className: 'col-floor' },
    { label: '容量', key: 'capacity', className: 'col-capacity' },
    { label: '实际容量', key: 'realCapacity', className: 'col-real-capacity' },
    { label: '备注', key: 'remark', className: 'col-remark' }
  ],
  RESULT_EMPTY_TEXT: '当前条件下暂无空教室结果。',
  UI: {
    SHELL_MAX_WIDTH: 1120,
    PANEL_RADIUS: 18,
    CONTROL_RADIUS: 12,
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
    CHIP_BG: 'rgba(247, 250, 255, 0.94)',
    CHIP_BORDER: 'rgba(214, 222, 237, 1)',
    PREVIEW_BG: 'rgba(246, 249, 255, 0.88)',
    PREVIEW_BORDER: 'rgba(223, 232, 247, 0.92)',
    TABLE_HEAD_BG: 'rgba(244, 247, 253, 0.96)',
    TABLE_ROW_BG: 'rgba(255, 255, 255, 0.75)',
    TABLE_TEXT: '#22324b'
  }
}
