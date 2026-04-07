# Spring Plus 과제

## 📋 프로젝트 소개
Spring Boot 기반의 일정 관리 서비스입니다.

## 🛠 기술 스택
- Java 17
- Spring Boot 3.3.3
- Spring Security
- Spring Data JPA
- QueryDSL
- JWT
- H2 Database

## ✅ 구현 기능

### Level 1
| 기능 | 설명 |
|------|------|
| 1. @Transactional | 할 일 저장 시 readOnly 오류 수정 |
| 2. JWT nickname | User에 nickname 추가 및 JWT claim에 포함 |
| 3. JPQL 조건 검색 | weather, 수정일 기간으로 할 일 검색 |
| 4. 컨트롤러 테스트 | 예외 발생 시 400 반환 테스트 수정 |
| 5. AOP | changeUserRole() 실행 전 로깅 동작하도록 수정 |

### Level 2
| 기능 | 설명 |
|------|------|
| 6. JPA Cascade | 할 일 생성 시 담당자 자동 등록 |
| 7. N+1 해결 | JOIN FETCH 적용으로 N+1 문제 해결 |
| 8. QueryDSL | findByIdWithUser를 QueryDSL로 전환 |
| 9. Spring Security | Filter/ArgumentResolver를 Spring Security로 전환 |

### Level 3
| 기능 | 설명 |
|------|------|
| 10. QueryDSL 검색 | 제목, 생성일, 닉네임 조건 검색 API 구현 |

## 🔑 환경 설정

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        show_sql: true
        format_sql: true

jwt:
  secret:
    key: ${JWT_SECRET_KEY}
```

### 환경변수 설정
IntelliJ 실행 구성에서 환경변수 추가:
JWT_SECRET_KEY=발급받은_Base64_키값

## 📡 API 명세

### Auth
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /auth/signup | 회원가입 | ❌ |
| POST | /auth/signin | 로그인 | ❌ |

### Todo
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /todos | 할 일 저장 | ✅ |
| GET | /todos | 할 일 목록 조회 | ✅ |
| GET | /todos/{todoId} | 할 일 단건 조회 | ✅ |
| GET | /todos/search | 할 일 검색 | ✅ |

### Comment
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /todos/{todoId}/comments | 댓글 저장 | ✅ |
| GET | /todos/{todoId}/comments | 댓글 목록 조회 | ✅ |

### Manager
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /todos/{todoId}/managers | 담당자 등록 | ✅ |
| GET | /todos/{todoId}/managers | 담당자 목록 조회 | ✅ |
| DELETE | /todos/{todoId}/managers/{managerId} | 담당자 삭제 | ✅ |

### User
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /users/{userId} | 유저 조회 | ✅ |
| PUT | /users | 비밀번호 변경 | ✅ |

### Admin
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| PATCH | /admin/users/{userId} | 유저 권한 변경 | ✅ (ADMIN) |

## 🔍 검색 API 상세

### GET /todos/search
**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| page | int | ❌ | 페이지 번호 (기본값: 1) |
| size | int | ❌ | 페이지 크기 (기본값: 10) |
| title | String | ❌ | 제목 부분 검색 |
| startDate | LocalDateTime | ❌ | 생성일 시작 범위 |
| endDate | LocalDateTime | ❌ | 생성일 끝 범위 |
| nickname | String | ❌ | 담당자 닉네임 부분 검색 |

**Response:**
```json
{
    "content": [
        {
            "title": "할 일 제목",
            "managerCount": 1,
            "commentCount": 3
        }
    ],
    "page": {
        "size": 10,
        "number": 0,
        "totalElements": 1,
        "totalPages": 1
    }
}
```
