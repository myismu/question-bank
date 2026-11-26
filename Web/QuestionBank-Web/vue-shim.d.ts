declare module "*.vue" {
  import type { DefineComponent } from "vue"

  // 为 .vue 单文件组件提供通用类型声明，使用 object 替代空对象类型 {} 以满足严格 TS 校验
  const component: DefineComponent<object, object, any>
  export default component
}
