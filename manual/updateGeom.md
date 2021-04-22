# updateGeom 서비스

## 기능  
해당 서비스는 사용자가 ws엔진을 통해 **DB에 저장되어 있는 레이어**들의 properties와 geometry 정보를 원하는 데이터로 수정을 하는 API  
**해당 서비스는 현재 ORACLE DB만 적용되고 있습니다.**

## API 목록
- [x] update
- [x] insert
- [x] delete
- [ ] delete(Geometry 기반)

## 패치노트
### 2021-04-22
* insert 시 SQLRecoverableException 문제 해결
  * 시퀸스 처리 부분을 select문으로 따로 처리되도록 수정
  * 시퀸스 처리 할 때 에러가 발생할 경우 로그를 통해 확인 가능하도록 수정
  
## API 명세
### update
* method : POST
* url : http://{serverIp}:{port}/ws/services/updateGeom
* request
  * action – type : String, required : true, ("update" 입력)
  *	layerId – type : String, required : true, (데이터를 변경하려는 레이어명)
  *	tableName –type: String, required : true, (데이터를 변경하려는 테이블명)
  *	wClause – type : String, required : true, (데이터를 변경하기 위한 sql의 where절)
  * srid – type : string, required : true, (좌표계 설정)
  *	json – type : String, required : true, (데이터를 변경하기 위한 geoJson, type : feature)
  *	geomName – type : String, required : false, (json의 geometry가 'geometry'로 설정되지 않았을 경우 설정)
  *	columnList – type : array, required : true, (변경하려는 properties 속성을 명시)
* response
  * 성공
    * status : 200 OK
    * Message (json)
      * message : 'success'
      * tableName : (변경된 테이블명)
      * updatedRow : (변경된 row의 개수)
  * 실패
    * status : 406
    * Message (json)
      * message : 'fail'
      * tableName : (변경을 시도한 테이블명)
      * error : error 코드 **(보안을 위해 추후 변경 바람)**
### insert
* method : POST
* url : http://{serverIp}:{port}/ws/services/updateGeom
* request
  * action – type : String, required : true, ("insert" 입력)
  *	layerId – type : String, required : true, (데이터를 변경하려는 레이어명)
  *	tableName –type: String, required : true, (데이터를 변경하려는 테이블명)
  * srid – type : string, required : true, (좌표계 설정)
  * seqName : type : string : required : false (시퀸스 명)
  * seqColName : type : string : required : false (시퀸스를 적용할 필드명) 
  *	json – type : String, required : true, (데이터를 변경하기 위한 geoJson, type : feature)
  *	geomName – type : String, required : false, (json의 geometry가 'geometry'로 설정되지 않았을 경우 설정)
  *	columnList – type : array, required : true, (변경하려는 properties 속성을 명시)
* response
  * 성공
    * status : 200 OK
    * Message (json)
      * message : 'success'
      * tableName : (변경된 테이블명)
      * insertedRow : (변경된 row의 개수)
  * 실패
    * status : 406
    * Message (json)
      * message : 'fail'
      * tableName : (변경을 시도한 테이블명)
      * error : error 코드 **(보안을 위해 추후 변경 바람)**
### delete
* method : POST
* url : http://{serverIp}:{port}/ws/services/updateGeom
* request
  * action – type : String, required : true, ("insert" 입력)
  *	layerId – type : String, required : true, (데이터를 변경하려는 레이어명)
  *	tableName –type: String, required : true, (데이터를 변경하려는 테이블명)
  * wClause – type : String, required : true, (데이터를 변경하기 위한 sql의 where절)
* response
  * 성공
    * status : 200 OK
    * Message (json)
      * message : 'success'
      * tableName : (변경된 테이블명)
      * deletedRow : (변경된 row의 개수)
  * 실패
    * status : 406
    * Message (json)
      * message : 'fail'
      * tableName : (변경을 시도한 테이블명)
      * error : error 코드 **(보안을 위해 추후 변경 바람)**
  
## class & interface
* GeometryServlet
  * HttpServlet을 상속
  * updateGeom 서비스를 접근하는 servlet
  * package : com.ictway.wisesphere.services.custom
  * method
    * dopost
      * 개요 :사용자가 post 메소드로 request를 보내면 discriminationAction 메소드에 HttpServletRequest를 파라미터로 보내  
    요청에 맞는 작업을 수행 후 수행된 결과를 사용자에게 제공합니다.
      * parameter : HttpServletRequest,HttpServletResponse
      * return value : void
    * discriminationAction
      * 개요 : request의 action 파라미터를 사용하여 switch문을 통해 적합한 서비스에 연결합니다.
      * parameter : HttpServletRequest
      * return value :  JsonObject **(사용자에게 제공될 메시지)**
* GeometryDBService
  * 사용자가 필요로 하는 서비스들의 interface입니다.
  * package : com.ictway.wisesphere.services.custom.service
  * method
    * process
      * 개요 : 수행되는 메소드의 순서를 제어합니다.
      * **각각의 클래스에서 오버로딩됩니다.**
      * parameter : void
      * return value : JsonObject
    * initFeatureConnection
      * 개요 : layerId를 이용하여 workspace에서 해당되는 레이어를 찾은 후 connId를 기반으로 DB와 연결됩니다.
      * parameter : String layerId
      * return value : void
    * getColumnList
      * 개요 : 요청 parameter인 cList를 이용하여 변경할 DB column 목록을 반환합니다.
      * parameter : String[] cList
      * return value : ArrayList<String>
    * getColumnListFromDB
      * 개요 : 사용자가 입력한 tableId를 이용하여 DB에 직접 접근하여 변경할 DB column 목록을 반환합니다.
      * 문제점 : 사용자 권한 문제로 인해 미완성입니다.
      * parameter : String tableId
      * return value : return value : ArrayList<String>
    * getSql
      * 개요 : DB에 전송할 query를 생성합니다.
      * **각각의 클래스에서 오버로딩 됩니다.**
      * parameter : void
      * return value : String
    * updateFeature
      * 개요 : DB에 query를 전송하여 feature를 사용자 요청에 맞게 수정합니다.
      * parameter : String sql
      * return value : int (성공 실패 여부)
    * closeConnection
      * 개요 : DB 연결 해제
      * parameter : void
      * return value : void
  * 하위 클래스
    * UpdateOracleService
      * 레이어 요소를 업데이트하는 서비스입니다.
      * package : com.ictway.wisesphere.services.custom.service
      * method
        * process
          * GeometryDBService의 process를 오버로딩
          * parameter
            * String layerId (레이어명)
            * String tableName (테이블명)
            * String wClause (where절)
            * String json (geojson)
            * String srid (좌표계)
            * String geomName (geometry column명)
            * String[] cList (column목록)
          * **동일한 파라미터는 앞으로 생략**
        * initFeatureConnection
          * 개요 : overriding
        * getSql
          * 상속받은 getSql을 overloading
          * parameter : String layerId,String wClause,String json,String geomName,ArrayList<String> columnList,String srid
            * columList : getColumnList에서 반환된 변경할 column목록
        * updateFeature
          * 개요 : overriding
        * getColumnList
          * 개요 : overriding
        * ColumnListFromDB
          * 개요 : overriding
        * closeConnection
          * 개요 : overriding
    * InsertOracleService
      * 레이어 요소를 추가하는 서비스입니다.
      * package : com.ictway.wisesphere.services.custom.service
      * method
        * process
          * GeometryDBService의 process를 오버로딩
          * parameter : String layerId,String tableName,String json,String srid,String geomName,String[] cList, String seqName, String seqColName
            * seqName : sequence명
            * seqColName : sequence를 사용하는 table의 column명
          * **동일한 파라미터는 앞으로 생략**
        * initFeatureConnection
          * 개요 : overriding
        * getSql
          * 상속받은 getSql을 overloading
          * parameter : String layerId,String json,String geomName,ArrayList<String> columnList,String seqName,String seqColName,String srid
        * updateFeature
          * 개요 : overriding
        * getColumnList
          * 개요 : overriding
        * ColumnListFromDB
          * 개요 : overriding
        * closeConnection
          * 개요 : overriding
    * DeleteOracleService
      * 레이어 요소를 삭제하는 서비스입니다.
      * package : com.ictway.wisesphere.services.custom.service
      * method
        * process
          * GeometryDBService의 process를 오버로딩
          * parameter : String layerId,String tableName,String wClause
          * **동일한 파라미터는 앞으로 생략**
        * initFeatureConnection
          * 개요 : overriding
        * getSql
          * 상속받은 getSql을 overloading
          * parameter : String layerId,String wClause
        * updateFeature
          * 개요 : overriding
        * getColumnList
          * 개요 : overriding
        * ColumnListFromDB
          * 개요 : overriding
        * closeConnection
          * 개요 : overriding
    * DeleteOracleGeomService
      * 레이어 요소를 geometry 정보를 이용하여 삭제하는 서비스입니다.
      * ** 권한문제로 현재 미완성 ** 
      * package : com.ictway.wisesphere.services.custom.service
      * method
        * process
          * GeometryDBService의 process를 오버로딩
          * parameter : String layerId,String json,String geomName
          * **동일한 파라미터는 앞으로 생략**
        * initFeatureConnection
          * 개요 : overriding
        * getSql
          * 개요 : overriding
        * updateFeature
          * 개요 : overriding
        * getColumnList
          * 개요 : overriding
        * ColumnListFromDB
          * 개요 : overriding
        * closeConnection
          * 개요 : overriding
          
## Test Case
## update
### point
    var url = " http://192.168.0.137:8808/wise-sphere-2d/services/updateGeom ";
    var params = {
	    action:"update",
	    layerId:"rider_4326",
	    tableName:"rider_4326",
	    wClause:"ID='r03'",
	    srid: '4326',
	    json:'{ "type": "Feature", "properties": { "id": "r03", "latitude": 136.57504119873, "longitude": 39.395057678222699, "time": "2021-03-15T14:20:54" }, "geometry": { "type": "Point", "coordinates": [ 124.675041198730469, 35.385057678222656 ] } }',
    columnList : ['latitude','longitude']};
    $.post(url, params);
### line
    var url = "http://192.168.0.47:8380/ws/services/updateGeom";
    var params = {
	    action:"update",
	    layerId:"line",
	    tableName:"line_5186",
	    wClause : "GID=1",
	    srid:'5186',
	    geomName:"geom",
	    json:'{ "type": "Feature", "properties": {"id":"test02","name":"test033"}, "geometry": { "type": "LineString", "coordinates": [ [ 125.90557861328125, 34.23907530202184 ], [ 	125.408447265625, 36.59127365634205 ], [ 126.32305908203125, 36.71907231552909 ] ] } }',
	    columnList : ['id','name']
    };
    $.post(url, params);
### polygon
    var url = " http://192.168.0.137:8808/wise-sphere-2d/services/updateGeom ";
    var params = {
    	action:"update",
    	layerId:"korea_4326",
    	tableName:"korea_4326",
    	wClause:"GID_0='KOR'",
    	srid : '4326',
	   json:'{"id":"layerId0","type":"Feature","properties":{"NAME_0":"kor19_test"},"geometry":{"coordinates":[[[125.97303456,37.55291035],[125.96802104075867,38.55547579329156],[125.18857116,38.55213956],[126.99303456,37.55291035]]],"type":"Polygon"}}',columnList : ['GID_0','NAME_0']};
    $.post(url, params);
## insert
### point
    var url = "http://192.168.0.47:8380/ws/services/updateGeom";
    var params = {
        action:"insert",
        layerId:"point",
        tableName:"point_5186",
        srid:'5186',
        seqName:"SEQPOINT",
        seqColName:"name",
        json:'{ "type": "Feature", "properties": {"name":"test01"}, "geometry": { "type": "Point", "coordinates": [ 127.111572265625, 36.14718209972376 ] } }',
        columnList : ['name']
    };
    $.post(url, params);
### line
    var url = "http://192.168.0.47:8380/ws/services/updateGeom";
    var params = {
        action:"insert",
        layerId:"line",
        tableName:"line_5186",
        srid:'5186',
        seqName:"SEQLINE",
        seqColName:"GID",
        geomName:"geom",
        json:'{ "type": "Feature", "properties": {"id":"c03","name":"test03"}, "geometry": { "type": "LineString","coordinates": [ [ 126.90557861328125, 34.23907530202184 ], [ 125.408447265625, 34.59127365634205 ], [ 124.32305908203125, 36.71907231552909 ] ] } }',
        columnList : ['id','name']
    };
    $.post(url, params);
### polygon
    var url = "http://192.168.0.47:8380/ws/services/updateGeom";
    var params = {
        action:"insert",
        layerId:"polygon",
        tableName:"polygon_5186",
        srid:'5186',
        seqName:"SEQLINE",
        seqColName:"GID",
        geomName:"geom",
        json:'{ "type": "Feature", "properties": {"id":"c02","name":"test01"}, "geometry": { "type": "Polygon", "coordinates": [ [ [ 127.51693725585938, 37.03983207971425 ], [ 127.33978271484375, 36.855449936136495 ], [ 124.64053344726562, 36.8510544475565 ], [ 126.51693725585938, 37.03983207971425 ] ] ] } }',
        columnList : ['id','name']
    };
    $.post(url, params);
## delete
### point
    var url = "http://192.168.0.137:8808/wise-sphere-2d/services/updateGeom";
    var params = {
        action:"delete",
        layerId:"rider_4326",
        tableName:"rider_4326",
        wClause:"id='c02'"
    };
    $.post(url, params);
### line
    var url = "http://192.168.0.137:8808/wise-sphere-2d/services/updateGeom";
    var params = {
        action:"delete",
        layerId:"road_4326",
        tableName:”road_4326”,
        wClause:"UFID='105261'"
    };
    $.post(url, params);
### polygon
    var url = "http://192.168.0.137:8808/wise-sphere-2d/services/updateGeom";
    var params = {
        action:"delete",
        layerId:"korea_4326",
        tableName:”korea_4326”,
        wClause:"GID_0='test01'"
    };
    $.post(url, params);



