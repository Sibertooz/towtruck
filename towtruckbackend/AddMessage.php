<?php
	if($_SERVER['REQUEST_METHOD']=='POST') {
		include 'DatabaseConfig.php';

		$con = mysqli_connect($HostName,$HostUser,$HostPass,$DatabaseName);
		$from_email = $_POST['from_email'];
		$to_email = $_POST['to_email'];
		$message = $_POST['message'];
		$current_time = round(microtime(1) * 1000);
		$Sql_Query = "INSERT INTO messages (from_email, to_email, message, data) values ('$from_email', '$to_email', '$message', '$current_time')";

		if(mysqli_query($con,$Sql_Query)) {
			echo 'Message Sent Successfully';
		} else {
			echo 'Something went wrong';
		}
	}
	mysqli_close($con);
?>