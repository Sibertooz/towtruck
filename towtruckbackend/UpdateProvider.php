<?php
	if($_SERVER['REQUEST_METHOD']=='POST'){

		include 'DatabaseConfig.php';

		$con = mysqli_connect($HostName,$HostUser,$HostPass,$DatabaseName);

		$email = $_POST['email'];
		$name = $_POST['name'];
		$phone = $_POST['phone'];

		$Sql_Query = "UPDATE users SET name = '$name', phone = '$phone' WHERE email = '$email'";

		if(mysqli_query($con,$Sql_Query)){
			echo 'Record Updated Successfully';
		} else {
			echo 'Something went wrong';
		}
	}
	mysqli_close($con);
?>