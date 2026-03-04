import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const 저장소이미지경로 = path.resolve(__dirname, "../../img");
const 빌드산출물경로 = path.resolve(__dirname, "../dist");
const 대상경로목록 = [
  path.join(빌드산출물경로, "images"),
  path.join(빌드산출물경로, "img"),
];

async function 경로존재여부(경로) {
  try {
    await fs.access(경로);
    return true;
  } catch {
    return false;
  }
}

async function 실행() {
  const 이미지경로존재 = await 경로존재여부(저장소이미지경로);
  if (!이미지경로존재) {
    console.warn(`[이미지 복사] 저장소 이미지 폴더가 없어 건너뜁니다: ${저장소이미지경로}`);
    return;
  }

  const dist존재 = await 경로존재여부(빌드산출물경로);
  if (!dist존재) {
    throw new Error(`[이미지 복사] dist 폴더를 찾지 못했습니다: ${빌드산출물경로}`);
  }

  for (const 대상경로 of 대상경로목록) {
    await fs.mkdir(대상경로, { recursive: true });
    await fs.cp(저장소이미지경로, 대상경로, { recursive: true, force: true });
    console.log(`[이미지 복사] 완료: ${저장소이미지경로} -> ${대상경로}`);
  }
}

실행().catch((오류) => {
  console.error(오류);
  process.exit(1);
});
