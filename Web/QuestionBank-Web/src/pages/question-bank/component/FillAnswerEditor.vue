<script setup lang="ts">
import { ref, watch } from "vue"

const props = defineProps<{
  answer: string[][]
}>()

const emit = defineEmits(["update:answer"])

const inputCache = ref<string[]>([])

// 初始化 input 缓存
watch(
  () => props.answer,
  (val) => {
    inputCache.value = val.map(group => group.join(", "))
  },
  { immediate: true, deep: true }
)

function onInputBlur(index: number) {
  const raw = inputCache.value[index]
  const split = raw.split(",").map(s => s.trim()).filter(Boolean)
  const newAnswer = [...props.answer]
  newAnswer[index] = split
  emit("update:answer", newAnswer)
}

function addBlank() {
  const newAnswer = [...props.answer, []]
  emit("update:answer", newAnswer)
  inputCache.value.push("")
}

function removeBlank() {
  if (props.answer.length <= 1) return
  const newAnswer = props.answer.slice(0, -1)
  emit("update:answer", newAnswer)
  inputCache.value.pop()
}
</script>

<template>
  <div class="fill">
    <div
      v-for="(answerGroup, index) in answer"
      :key="index"
      class="mb-2"
    >
      <el-form-item :label="`第 ${index + 1} 空答案`">
        <el-input
          v-model="inputCache[index]"
          placeholder="用英文逗号分隔多个正确答案"
          @blur="onInputBlur(index)"
        />
      </el-form-item>
    </div>
    <el-button type="primary" @click="addBlank">
      添加空
    </el-button>
    <el-button type="danger" @click="removeBlank" :disabled="answer.length <= 1">
      删除最后一个空
    </el-button>
  </div>
</template>

<style>
.fill {
  width: 40%;
}
.el-input {
  margin-bottom: 2%;
}
</style>
