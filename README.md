# springboot study
## 개요
- spring boot 공부를 목적으로 SpringBoot2 framework 기반의 RESTful 책검색 api 서비스

## 개발환경
- Java 8
- SpringBoot 2.2.2
- SpringSecurity 
- JPA, H2
- Redis Embedded
- Intellij Community

## 프로젝트 실행
- H2 database 설치
    - https://www.h2database.com/html/download.html
- Enable annotation processing
    - Preferences - Annotation Procesors - Enable annotation processing 체크
- 실행
    - Run -> SpringBootApiApplication
- Swagger
    - http://localhost:8080/swagger-ui.html

## 테스트
- 가입(signup)
	- curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8080/v1/signup?id=tester%40test.com&password=test&name=test'
- 로그인(signin)
	- curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8080/v1/signin?id=tester%40test.com&password=test'
- 책검색(search)
	- signin 의 response 값 중 data값을 header의 'X-AUTH-TOKEN' 으로 지정
	- curl -X GET --header 'Accept: application/json' --header 'X-AUTH-TOKEN: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4Iiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTU3OTM2NTk5NCwiZXhwIjoxNTc5MzY5NTk0fQ.lgESUWPLdtZvZ3iwD05cWBcwteQhH5K700m0UMwvPCQ' 'http://localhost:8080/v1/search/book/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8?page=1'
- 내 검색 히스토리(history)
	- signin 의 response 값 중 data값을 header의 'X-AUTH-TOKEN' 으로 지정
	- curl -X GET --header 'Accept: application/json' --header 'X-AUTH-TOKEN: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4Iiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTU3OTM2NTk5NCwiZXhwIjoxNTc5MzY5NTk0fQ.lgESUWPLdtZvZ3iwD05cWBcwteQhH5K700m0UMwvPCQ' 'http://localhost:8080/v1/search/book/history'
- 인기 키워드 목록(rank)
	-  curl -X GET --header 'Accept: application/json' 'http://localhost:8080/v1/search/book/rank'

## 주요 참고 사이트
### spring boot rest 기본 구조
- https://daddyprogrammer.org/post/series/springboot2-make-rest-api/
### redis sorted set을 이용한 랭킹 구현
- https://elinshu.tistory.com/entry/Redis-ZSetOperations-example
### restTemplate
- https://sunpil.tistory.com/295
### aync 설정
- https://heowc.dev/2018/02/10/spring-boot-async/
- https://howtodoinjava.com/spring-boot2/rest/enableasync-async-controller/

