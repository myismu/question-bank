import type { PageResult, PreviewQuestion, PublicQuestionsDTO } from "./types"
import { request } from "@/http/axios"
import { mapToPreviewQuestion } from "./types"

export async function getQuestionsById(bankId: string): Promise<PreviewQuestion[]> {
  const res = await request<{ code: number, message: string, data: PublicQuestionsDTO[] }>({
    url: "/api/public/questions/getQuestionsById",
    method: "GET",
    params: { id: bankId }
  })
  return (res.data || []).map(mapToPreviewQuestion)
}
// export async function getQuestionsByPage(bankId: string): Promise<PublicQuestionsDTO[]> {
//   const res = await request<ApiResponseData<PublicQuestionsDTO[]>>({
//     url: "/api/public/questions/getQuestionsByPage",
//     method: "GET",
//     params: { id: bankId }
//   })
//   return res.data
// }

export async function getQuestionsByPage(bankId: string): Promise<PageResult<PreviewQuestion>> {
  const res = await request<{ code: number, message: string, data: PageResult<PublicQuestionsDTO> }>({
    url: "/api/public/questions/getQuestionsByPage",
    method: "GET",
    params: { id: bankId }
  })
  // 转换records
  const pageData = res.data
  return {
    ...pageData,
    records: (pageData?.records || []).map(mapToPreviewQuestion)
  }
}
