<?php 
	$mysql_host = "localhost";
	$mysql_user = "root";
	$mysql_password = "admin";
	$mysql_database = "towtruck";

	if (!mysql_connect($mysql_host, $mysql_user, $mysql_password)) {
		echo "<h2>База недоступна!</h2>";
		exit;
	} else {
		echo "<h2>База доступна!</h2>";

		mysql_select_db($mysql_database);

		$sql = mysql_query("SELECT * FROM messages ORDER BY id");
		echo "<h3>Json ответ:</h3>";

		while($e = mysql_fetch_assoc($sql)) $output[] = $e;
		print(json_encode($output));

		$sql = mysql_query("SELECT * FROM messages ORDER BY id");
		
		echo "<h3>Табличный вид:</h3>";
		echo "<table border=\"1\" width=\"100%\" bgcolor=\"#999999\">";
		echo "<tr><td>id</td><td>from_email</td>";
		echo "<td>to_email</td><td>message</td><td>data</td></tr>";

		for($c = 0; $c < mysql_num_rows($sql); $c++) {
			$f = mysql_fetch_array($sql);
			echo "<tr><td>$f[id]</td><td>$f[from_email]</td><td>$f[to_email]</td><td>$f[message]</td><td>$f[data]</td></tr>";
		}
		echo "</tr></table>";
	}
	mysql_close();
?>