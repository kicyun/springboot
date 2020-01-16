## springboot study
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
