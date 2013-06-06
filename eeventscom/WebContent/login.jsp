<!-- 
 *   Copyright 2013 Ant Kutschera
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
-->
<%String errorTitle = "&nbsp;";
String errorText = "Please log in";
if(request.getAttribute("FROM_LOGON_ERROR") != null){
	errorTitle = "Login Failed";
	errorText = "Email name and password are CaSe SeNsiTivE. Please try again.";
}
String email = request.getParameter("j_username");
if(email == null){
	Cookie[] cs = request.getCookies();
	if(cs != null){
		for(Cookie c : cs){
			if(c.getName().equals(new ch.maxant.scalabook.shop.ui.Login().EmailCookieName())){
				email = c.getValue();
			}
		}
	}
}
String fieldToHighlight = "j_username";
if(email != null && email.trim().length() > 0){
	fieldToHighlight = "j_password";
}%>

<html>
<head>
	<title>Events</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
 	<link href="<%=request.getContextPath() %>/resources/css/global.css" type="text/css" rel="stylesheet">
	<script type="text/javascript" src="<%=request.getContextPath() %>/javax.faces.resource/jquery/jquery.js.jsf?ln=primefaces"></script>
</head>
<body>
<div style="text-align: left;">
<table class="bodyTable">
	<tr>
		<td style="width: 1%; vertical-align: top;">
			<a href="<%=request.getContextPath()%>/index.jsf">
				<img src="<%=request.getContextPath() %>/resources/images/logo.png" />
			</a>
		</td>
		<td style="vertical-align: middle;">
			<form name="login" action="j_security_check" method="post">
			<table style="width: 100%" >
				<tr>
					<td colspan="2" style="text-align: center;" class="error smallTitle">
						<%=errorTitle %>
					</td>
				</tr>
				<tr>
					<td colspan="2" style="text-align: center;">
						<%=errorText %>
					</td>
				</tr>
				<tr>
					<td style="text-align: right; width: 50%">
						Email
					</td>
					<td style="text-align: left; width: 50%">
						<input type='text' name="j_username" value="<%=(email==null?"":email.trim()) %>" placeholder="Email" />
					</td>
				</tr>
				<tr>
					<td style="text-align: right; width: 50%">
						Password
					</td>
					<td style="text-align: left; width: 50%">
						<input type="password" name="j_password" placeholder="Password" />
					</td>
				</tr>
				<tr>
					<td style="text-align: center;" colspan="2">
						<input type="submit" value="log in" class="myButton" />
					</td>
				</tr>
				<tr>
					<td colspan="2" style="text-align: center; font-style: italic; font-size: small;">Try john@maxant.ch / asdf</td>
				</tr>
			</table>
			</form>
		</td>
	</tr>
</table>
</div>
		
<script type="text/javascript">
	var e = $("input[name='<%=fieldToHighlight%>']");
	e.focus();
	//var focusControl = document.forms["login"].elements["<%=fieldToHighlight%>"];
</script>

</body>
</html>