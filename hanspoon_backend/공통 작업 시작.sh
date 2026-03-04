git 활용 작업 주의사항 
폴더에 sh파일 만들지 마세요. 자꾸 중복되면서 바뀌는 파일로 인식해서 이상이 생깁니다.

처음 작업할 시 필수로 무조건 깃에서 백엔드 프론트엔드 클론해서 클론한 폴더에서만 작업하세요.
기존에 작업하던 폴더에서 작업하하는 순간 깃 또 빠이입니다..

클론을 한 상태라면 작업을 시작할 때 터미널에서 밑에 명령어 하나씩 실해한 다음 작업시작하세요(프론트, 백 공통입니다.)
git init
git checkout dev
git pull origin dev
git checkout -b han-***



 # 0) 각 저장소 폴더로 이동해서 각각 실행
  # 예시:
  cd C:\Programmer\Work\hanspoon\hanspoon_frontend
  # 또는
  cd C:\Programmer\Work\hanspoon\hanspoon_backend

  # 1) 원격 최신 받기
  git fetch origin

  # 2) dev 최신 기준으로 han-class 브랜치 생성(처음 1회)
  git checkout dev
  git pull origin dev
  git checkout -b han-class

  # 이미 han-class가 있으면(이후부터)
  # git checkout han-class
  # git rebase origin/dev

  # 3) 작업 후 커밋
  git add .
  git commit -m "feat: 작업 내용 요약"

  # 4) han-class 브랜치로 푸시
  git push -u origin han-class
  # (2회차부터는 git push 만 해도 됨)

  # 5) PR 생성용 링크 확인 (GitHub CLI 없을 때)
  git remote get-url origin
  # 브라우저에서:
  # https://github.com/<owner>/<repo>/compare/dev...han-class?expand=1

  GitHub CLI(gh) 쓰면 PR까지 터미널에서 가능:

  gh pr create --base dev --head han-class --title "작업 제목" --body "작업 설명"

  두 저장소 모두 동일하게 반복하면 됩니다.