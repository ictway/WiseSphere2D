# workspace 폴더 변경 기능 매뉴얼

## 기능
해당 기능은 기존의 사용자 폴더로만 workspace 폴더가 위치할 수 있었지만 패치를 통해 workspace 폴더의 위치를 properties 파일을 이용하여 지정 가능

## 사용방법
  * Jeus (windows에서 테스트)
    1. jeus 서버 종료
    1. Application이 설치된 폴더 위치로 이동
    - ![img001] (https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/img001.png)
    - ![img002] (https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/img002.png)
    1. WEB-INF 폴더로 이동
    - ![img003] (https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/img003.png)
    1. root.properties 수정
      * 입력하지 않았을 경우 사용자 폴더로 workspace폴더가 위치해야 한다.
    - ![img004] (https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/img004.png)
    1. jeus 재시작
  * tomcat (ubuntu에서 테스트)
    1. Tomcat 서버 종료
    1. Application이 설치된 폴더 위치로 이동
    1. WEB-INF 폴더로 이동
    1. root.properties 수정
    - ![img005] (https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/img005.png)
    1. Tomcat 재시작
    - ![img006] (https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/img006.png)
    
## class
 **DeegreeWorkspace**
  * properties 파일을 이용하여 workspace 폴더의 위치와 폴더명을 지정할 수 있다.
  * package : org.deegree.commons.config
  * method
    * getWorkspaceRoot
      * 개요 : properties 파일을 읽고 workspace 폴더의 위치를 문자열로 반환한다. (default로 사용자 파일 + .wisesphere를 반환한다.)
      * parameter : void
      * return value : String

## resource
 **root.properties**
   * location : /src/main/webapp/WEB-INF/root.properties
   * 기능
     * location 변수를 사용하여 ws엔진의 workspace 폴더의 위치를 지정 가능
     * location 변수를 공백으로 한다면 default 설정인 사용자 파일로 workspace 폴더가 위치해야 한다.
   * 참고
     * WAS에 등록 후 수정 가능 WEB-INF 폴더에 위치한다. (WAS 종료 후 수정)