# springboot study
## 목적
인수인계 목적으로 spring boot 공부(spring boot 안 본지 1년이 지나 처음부터 다시 공부)

## 주요 참고 사이트
### spring boot rest 기본 구조
https://daddyprogrammer.org/post/series/springboot2-make-rest-api/
### redis sorted set을 이용한 랭킹 구현
https://elinshu.tistory.com/entry/Redis-ZSetOperations-example

## 환경 
Java8 인수 인계 받을 프로젝트가 Java8 으로 구성됨.

## 가상 환경 설정
## Install jenv
brew install jenv

설치 후 ~/.bash_profile 에 아래 코드를 추가하여 jenv 를 초기화
```
# Init jenv
if which jenv > /dev/null; then eval "$(jenv init -)"; fi
```

```bash
$ source ~/.bash_profile
```

Java 8 추가
jenv add /Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/

버젼 확인
```bash
$ jenv versions
```

Java 8 설정
```bash
$ jenv local oracle64-1.8.0.241
$ java -version
```

## H2 download
https://www.h2database.com/html/download.html
'''bash
$ /bin/h2.sh
'''

## swagger
http://localhost:8080/swagger-ui.html
