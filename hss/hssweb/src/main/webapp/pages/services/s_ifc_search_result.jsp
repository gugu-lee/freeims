<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean"
	prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html"
	prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic"
	prefix="logic"%>
<%@ page import="java.util.*, org.freeims.hss.server.model.* " %>

<jsp:useBean id="resultList" type="java.util.List" scope="request"></jsp:useBean>
<jsp:useBean id="maxPages" type="java.lang.String" scope="request"></jsp:useBean>
<jsp:useBean id="currentPage" type="java.lang.String" scope="request"></jsp:useBean>
<jsp:useBean id="rowPerPage" type="java.lang.String" scope="request"></jsp:useBean>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>

<head>
<link rel="stylesheet" type="text/css" href="/hss.web.console/style/fokus_ngni.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><bean:message key="result.title" /></title>

<script type="text/javascript" language="JavaScript">
function submitForm(pageId){
	document.S_IFC_SearchForm.crtPage.value = pageId;
	document.S_IFC_SearchForm.submit();
}

function rowsPerPageChanged(){
	document.S_IFC_SearchForm.crtPage.value = 1;
	document.S_IFC_SearchForm.submit();
}
</script> 

</head>
<body>
	<table id="title-table" align="center" weight="100%" >
	<tr>
		<td align="center">
			<h1> Shared iFC - Search Results </h1> 
			<br/><br/>		
		</td>
	</tr>
	</table> <!-- title-table -->

	<table id="main-table" align="center" valign="middle">
	<tr>
		<td>
	 		<table id="result-table" class="as" border="0" cellspacing="1" align="center" style="border:2px solid #FF6600;" width="400">	
				<tr class="header">
					<td class="header" width="50"> ID-Set </td>
					<td class="header"> Name </td>
				</tr>
			
				<%
				if (resultList != null && resultList.size() > 0){
					Shared_IFC_Set s_ifc_set;
					int idx = 0;
					Iterator it = resultList.iterator();
					
					while (it.hasNext()){
						s_ifc_set = (Shared_IFC_Set) it.next();
				%>		
					<tr class="<%= idx % 2 == 0 ? "even" : "odd" %>">
						<td>
							<%= s_ifc_set.getId_set() %>
						</td>
						<td> 
							<a href="/hss.web.console/S_IFC_Load.do?id_set=<%= s_ifc_set.getId_set() %>"> 
								<%= s_ifc_set.getName() %>
							</a>	
						</td>
					</tr>
				<%		
			
						idx++;		
					} //while
				} // if
				else{	
				%>
					<tr>
						<td>
							<bean:message key="result.emptyResultSet" />
						</td>
					</tr>						
				<%
				}
				%>	
			</table>
		</td>
	</tr>		
	<tr>
		<td colspan="3" class="header">
				
			<html:form action="/S_IFC_Search">
				<table align="center">
				<tr>
					<td>
					<%
							int length = Integer.parseInt(maxPages) + 1;
							int cPage = Integer.parseInt(currentPage) + 1;
							for (int iy = 1; iy < length; iy++) {
								if (cPage != iy) {
					%>
									<a href="javascript:submitForm(<%=String.valueOf(iy)%>);"><%=iy%></a>
					<%
								} else {
					%> 
									<font style="color:#FF0000;font-weight: 600;"> <%=String.valueOf(iy)%>
									</font> 
					<% 			}
							}
					%>
					</td>
							
					<td>
						<bean:message key="result.rowsPerPage" /><br>
							<html:hidden property="crtPage"></html:hidden> 
							<html:select property="rowsPerPage" onchange="javascript:rowsPerPageChanged();">

							<option value="20"
								<%= rowPerPage.equals("20") ? "selected" : "" %> >20 </option>
							<option value="30"
								<%= rowPerPage.equals("30") ? "selected" : "" %> >30 </option>
							<option value="50"
								<%= rowPerPage.equals("50") ? "selected" : "" %> >50</option>
							<option value="100"
								<%= rowPerPage.equals("100") ? "selected" : "" %> >100</option>
							</html:select>
					</td>
				</tr>
				</table>
			</html:form>
		</td>
	</tr>
	</table>

</body>
</html>
