import test from 'node:test'
import assert from 'node:assert/strict'
import { parseWeeksList } from '../src/utils/schedule.js'

test('parseWeeksList keeps odd-week semantics', () => {
  assert.deepEqual(parseWeeksList('1-16周(单)'), [1, 3, 5, 7, 9, 11, 13, 15])
})

test('parseWeeksList keeps even-week semantics with mixed delimiters', () => {
  assert.deepEqual(parseWeeksList('1-4周（双），7(单)，8'), [2, 4, 7, 8])
})

test('parseWeeksList deduplicates and sorts parsed weeks', () => {
  assert.deepEqual(parseWeeksList('3,1-3,2'), [1, 2, 3])
})
