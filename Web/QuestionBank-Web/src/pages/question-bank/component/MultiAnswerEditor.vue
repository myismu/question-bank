<script setup lang="ts">
import { computed, ref, watch } from "vue"

// 接收 props
const props = defineProps<{
  answer: string[][]
  options: Record<string, string>
}>()

// 定义事件
const emit = defineEmits<{
  (e: "update:answer", value: string[][]): void
}>()

// 将 string[][] 转成 string[]，用于绑定 checkbox-group
const localAnswer = ref<string[]>(props.answer.map(a => a[0]))

// 监听 checkbox-group 的变更，将 string[] 转回 string[][] 并通过 emit 通知父组件
watch(localAnswer, (newVal) => {
  emit("update:answer", newVal.map(val => [val]))
})

// 排序后的 options
const sortedOptions = computed(() => {
  return Object.fromEntries(
    Object.entries(props.options).sort(([a], [b]) => a.localeCompare(b))
  )
})
</script>

<template>
  <el-form-item>
    <el-checkbox-group v-model="localAnswer">
      <el-checkbox
        v-for="(text, key) in sortedOptions"
        :key="key"
        :label="key"
      >
        {{ key }}
      </el-checkbox>
    </el-checkbox-group>
  </el-form-item>
</template>
