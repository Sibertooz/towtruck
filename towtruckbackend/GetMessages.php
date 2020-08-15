 <?php
	if($_SERVER['REQUEST_METHOD']=='POST'){
		include 'DatabaseConfig.php';
		
		$conn = new mysqli($HostName, $HostUser, $HostPass, $DatabaseName);
		$from_email = $_POST['from_email'];
		$to_email = $_POST['to_email'];
		$data = $_POST['data'];

		if ($conn->connect_error){
			die("Connection failed: " . $conn->connect_error);
		}
		
		$sql = "SELECT SQL_NO_CACHE * FROM messages WHERE (from_email='$from_email' AND to_email='$to_email' AND data > $data) OR (from_email='$to_email' AND to_email='$from_email' AND data > $data) ORDER BY id";
		$result = $conn->query($sql);

		if ($result->num_rows > 0) {
			while($row[] = $result->fetch_assoc()) {
				$tem = $row;
				$json = json_encode($tem);
			}
		} else {
			echo "No Results Found.";
		}
			
		echo $json;
		$conn->close();
	}
?>