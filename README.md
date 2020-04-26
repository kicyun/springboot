# springboot study
## 개요
- spring boot 공부를 목적으로 SpringBoot2 framework 기반의 RESTful 책검색 api 서비스 개발

## 개발환경
- Java 8
- SpringBoot 2.2.2
- SpringSecurity 
- JPA, H2
- Redis Embedded
- Intellij 

## 프로젝트 실행
- Enable annotation processing
    - Preferences - Build, Execution, Deployment - Compiler - Annotation Processors - Enable annotation processing 체크
- 실행
    - Run -> springboot [bootRun]
- Swagger
    - http://localhost:8080/swagger-ui.html
- 트러블 슈팅
    - 디버깅 후 재 실행 시킬 때 redis 관련 에러 발싱 시 해결 방안
        - 기존 임베디드 redis-server가 동작 중인 상태이므로 "ps -ef | grep redis" 로 redis-server를 찾은 후 kill 처리 

## 주요 구현 및 적용 내용
- @Async / ComplatableFuture 를 이용한 비동기 구현 (thread pool 설정)
- ControllerAdvisor / MessageSource 를 이용한 exception 처리
- Redis를 이용한 캐싱
- Redis를 이용한 인기 키워드 목록(rank) 구현
- swagger 적용
- SpringSecurity 를 이용한 인증(JWT)
- restTemplate 을 이용한 카카오/네이버 책 검색 rest API 호출 (thread pool 설정)

## 테스트
### Swagger 를 이용한 테스트
- 가입(signup)
    - http://localhost:8080/swagger-ui.html#!/1._Sign/signupUsingPOST
- 로그인(signin)
    - http://localhost:8080/swagger-ui.html#!/1._Sign/signinUsingPOST
- 책검색(search)
    - http://localhost:8080/swagger-ui.html#!/2.Book/searchUsingGET
- 내 검색 히스토리(history)
    - http://localhost:8080/swagger-ui.html#!/2.Book/historyUsingGET
- 인기 키워드 목록(rank)
    - http://localhost:8080/swagger-ui.html#!/2.Book/rankUsingGET
### curl 을 이용한 테스트
- 가입(signup)
	- curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8080/v1/signup?id=tester%40test.com&password=test&name=test'
- 로그인(signin)
	- curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8080/v1/signin?id=tester%40test.com&password=test'
- 책검색(search)
	- signin 의 response 값 중 data값을 header의 'X-AUTH-TOKEN' 으로 지정
	- curl -X GET --header 'Accept: application/json' --header 'X-AUTH-TOKEN: X.X.X' 'http://localhost:8080/v1/search/book/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8?page=1'
- 내 검색 히스토리(history)
	- signin 의 response 값 중 data값을 header의 'X-AUTH-TOKEN' 으로 지정
	- curl -X GET --header 'Accept: application/json' --header 'X-AUTH-TOKEN: X.X.X' 'http://localhost:8080/v1/search/book/history'
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
### hystrix 설정
- https://spring.io/guides/gs/circuit-breaker/
### Jwt 토큰정보로 필터링 된 유저정보를 컨트롤러단에서 @AuthenticationPricipal 어노테이션을 통해 가져오는법
- https://velog.io/@sonaky47/Spring-Security-Jwt-%ED%86%A0%ED%81%B0%EC%A0%95%EB%B3%B4%EB%A1%9C-%ED%95%84%ED%84%B0%EB%A7%81-%EB%90%9C-%EC%9C%A0%EC%A0%80%EC%A0%95%EB%B3%B4%EB%A5%BC-%EC%BB%A8%ED%8A%B8%EB%A1%A4%EB%9F%AC%EB%8B%A8%EC%97%90%EC%84%9C-AuthenticationPricipal-%EC%96%B4%EB%85%B8%ED%85%8C%EC%9D%B4%EC%85%98%EC%9D%84-%ED%86%B5%ED%95%B4-%EA%B0%80%EC%A0%B8%EC%98%A4%EB%8A%94%EB%B2%95