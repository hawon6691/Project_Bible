## 📝 협업 컨벤션

**1️⃣ 이슈 제목 컨벤션**

- 형식: `간결하게 작성 (GitHub 라벨로 관리, 라벨 꼭 붙이기!)`
- 라벨 종류
  `chore`: 코드 정리, 포맷팅, 의존성 업데이트 등
  `feature`: 완전히 새로운 기능 개발
  `BugFix`: 버그 발생, 작동하지 않거나 오류가 있는 문제
  `docs`: 문서 수정
  `refactor`: 기존 코드의 동작은 유지하면서 구조, 가독성, 유지보수성을 개선하는 작업

**2️⃣ 브랜치명 컨벤션**

- 형식: `[태그]/#[이슈번호]/[작업내용-간단요약]`

**3️⃣ 커밋 메시지 컨벤션**

- 형식**:** `[태그]: (#이슈번호) 제목 (75자 이내)`
- 커밋 태그 정의
  - `feat`: 새로운 기능 추가
  - `fix`: 버그 수정
  - `docs`: 문서 수정
  - `style`: 코드 포맷팅, 세미콜론 누락 등 (기능/동작 변화 없음)
  - `refactor`: 코드 리팩토링. (기능 변화 없음)
  - `test`: 테스트 코드 추가/수정
  - `chore`: 빌드 업무 수정, 패키지 매니저 변경 등
  - `conflict`: 충돌 해결

**4️⃣ PR 컨벤션**

- 형식: `이슈명을 조금 더 디테일하게 (GitHub 라벨로 관리, 라벨 꼭 붙이기!)`

**5️⃣ 코드 컨벤션**

- 이슈, PR 템플릿
  **이슈 템플릿(기능추가용)**

  ```markdown
  ---
  name: Feature Template
  about: 기능 추가 이슈 템플릿
  title: ""
  labels: "✨ feature"
  assignees: ""
  ---

  ## ✨ 구현할 기능

  - 추가하려는 기능에 대해 간결하게 설명해주세요

  ## 📝 작업 상세 내용

  - [ ] TODO
  - [ ] TODO
  - [ ] TODO
  ```

  **이슈 템플릿(버그용)**

  ```markdown
  ---
  name: Bug Report Template
  about: 버그 리포트 이슈 템플릿
  title: ""
  labels: "\U0001F41E BugFix"
  assignees: ""
  ---

  ## 🐞 버그 설명

  - 어떤 버그인지 간결하게 설명해주세요

  ## 🔎 발생 이유

  - (가능하면) Given-When-Then 형식으로 서술해주세요

  ## 🤔 기대한 동작

  - 예상했던 정상적인 결과가 어떤 것이었는지 설명해주세요
  ```

  **이슈 템플릿(커스텀용)**

  ```markdown
  ---
  name: "Custom Issue Template "
  about: 기타 자유 양식 이슈 템플릿
  title: ""
  labels: ""
  assignees: ""
  ---

  ## 🎯 목적

  - 이 이슈를 통해 달성하고자 하는 목표를 간단히 적어주세요

  ## 🛠️ 작업 항목

  - [ ] TODO
  - [ ] TODO
  - [ ] TODO
  ```

  **PR 템플릿**

  ```markdown
  ## #️⃣ 연관된 이슈

  - #이슈번호

  ## 📝 작업 내용

  - 이번 PR에서 작업한 내용을 간략히 설명해주세요

  ## 💬 리뷰 요구사항 (선택)

  - 리뷰어가 특별히 봐주었으면 하는 부분이 있다면 작성해주세요
  ```

- Prettier 코드
  ```jsx
  {
    "singleQuote": true,
    "trailingComma": "all",
    "printWidth": 100,
    "tabWidth": 2,
    "semi": true,
    "arrowParens": "always"
  }
  ```
