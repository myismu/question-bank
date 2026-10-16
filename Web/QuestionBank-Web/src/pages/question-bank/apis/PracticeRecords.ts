import type { PracticeRecordDetailDTO, PracticeRecordDTO } from "./type"
import { request } from "@/http/axios"

export async function savePracticeRecord(
  record: PracticeRecordDTO,
  details: PracticeRecordDetailDTO[]
): Promise<void> {
  await request({
    url: "/api/public/practiceRecords/save",
    method: "POST",
    data: { record, details }
  })
}
