<script setup lang="ts">
const props = defineProps<{
  answer: string[][]
  type: string
}>()
console.log("标准答案：", props.answer)
</script>

<template>
  <div style="margin-top: 10px;">
    <strong>标准答案：</strong>
    <br>
    <template v-if="type === '多选题' || type === '单选题' || type === '判断题'">
      <span v-if="answer.length">
        <span v-for="(group, index) in answer" :key="index">
          {{ type === '判断题' ? (group[0] ? '对' : '错') : group.join(', ') }}<span v-if="index < answer.length - 1">；</span>
        </span>
      </span>

      <span v-else style="color: #999;">暂无</span>
    </template>

    <template v-else-if="type === '填空题'">
      <span v-if="answer.length" style="white-space: pre-wrap;">
        <div v-for="(blank, index) in answer" :key="index">
          空{{ index + 1 }}：{{ blank.join(' / ') }}
        </div>
      </span>
      <span v-else style="color: #999;">暂无</span>
    </template>

    <template v-else-if="type === '简答题'">
      <span style="white-space: pre-wrap;">{{ answer.flat().join('\n') || '暂无' }}</span>
    </template>

    <template v-else>
      <span style="color: #999;">未知题型</span>
    </template>
  </div>
</template>
