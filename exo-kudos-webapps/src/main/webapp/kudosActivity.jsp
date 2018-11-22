<!--
  Lack in eXo API, we import CSS here: https://jira.exoplatform.org/browse/PLF-8100
-->
<link href='/exo-kudos/css/kudos-v1.0.0-M01.css' rel="stylesheet">
<script type="text/javascript">
  window.kudoAppMaximized = '<%=request.getAttribute("kudoAppMaximized") == null || ((String[])request.getAttribute("kudoAppMaximized")).length == 0 ? "false" : ((String[])request.getAttribute("kudoAppMaximized"))[0]%>';
</script>
<div id="KudosActivityApp"></div>
