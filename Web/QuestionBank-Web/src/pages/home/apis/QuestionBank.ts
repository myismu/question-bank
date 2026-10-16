import type { PublicQuestionBankResponse, QuestionBank } from "./types"
import { request } from "@/http/axios"
import { mapToQuestionBank } from "./types"

export async function getAllPublicQuestionBanks(): Promise<QuestionBank[]> {
  const res = await request<{ code: number, message: string, data: PublicQuestionBankResponse[] }>({
    url: "/api/public/questionBank/getAllPublicQuestionBanks",
    method: "GET"
  })
  return (res.data || []).map(mapToQuestionBank)
}
