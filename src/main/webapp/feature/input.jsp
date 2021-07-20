<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<script
  src="https://code.jquery.com/jquery-2.2.4.min.js"
  integrity="sha256-BbhdlvQf/xTY9gja0Dq3HiwQF8LaCRTXxZKRutelT44="
  crossorigin="anonymous"></script>
</head>
<body>
    <!-- post 방식을 사용해서 데이터 전송 (method="post") -->
    <form action="../services/updateGeom" method="post">
        layerId : <input type="text" name="layerId"><br>
		where clause : <input type="text" name="wClause"><br>
		json : <input type="text" name="json"><br>
        <input type="submit" value="전송">
    </form>
</body>
</html>