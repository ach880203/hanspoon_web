"""
.hanspoon0209 아래의 모든 .bak 파일을 재귀적으로 삭제합니다.
- 삭제 전 리스트를 수집하고 삭제 후 통계를 출력합니다.
- 주의: 삭제는 영구적이므로 신중히 실행하세요.
"""
import os
import sys

ROOT = r"d:\pro2\hanspoon0209"
deleted = []
for dirpath, dirnames, filenames in os.walk(ROOT):
    for fname in filenames:
        if fname.endswith('.bak'):
            full = os.path.join(dirpath, fname)
            try:
                os.remove(full)
                deleted.append(full)
            except Exception as e:
                print(f'ERROR deleting {full}: {e}', file=sys.stderr)

for p in deleted[:100]:
    print('DELETED:', p)
if len(deleted) > 100:
    print('... and', len(deleted)-100, 'more')
print('TOTAL_DELETED:', len(deleted))
