"""
복원 스크립트: 각 .java.bak 파일을 동일 경로의 .java 파일로 덮어씁니다.
백업은 남겨두며, 복원된 파일 목록과 통계를 출력합니다.
"""
import os
import shutil

ROOT = r"d:\pro2\hanspoon0209\src"
restored = 0
missing = 0
files = []
for dirpath, dirnames, filenames in os.walk(ROOT):
    for fname in filenames:
        if fname.endswith('.java.bak'):
            bak = os.path.join(dirpath, fname)
            orig = bak[:-4]  # remove .bak
            try:
                shutil.copy2(bak, orig)
                restored += 1
                files.append(orig)
            except Exception as e:
                print(f'ERROR restoring {bak}: {e}')
                missing += 1

print(f'완료: 복원된 파일 수={restored}, 오류 수={missing}')
# 출력 상위 50개 경로만 표시
for p in files[:50]:
    print('RESTORED:', p)
if len(files) > 50:
    print('... total restored files:', len(files))
