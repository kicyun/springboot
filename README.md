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

