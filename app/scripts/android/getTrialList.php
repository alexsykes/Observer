<?
//these are the server details
//the username is root by default in case of xampp
//password is nothing by default
//and lastly we have the database named android. if your database name is different you have to change it 
$servername = "localhost";
$username = "monster_android";
$password = "mU@09hQQwkKY";
$database = "monster_joom";

//creating a new connection object using mysqli 
$conn = new mysqli($servername, $username, $password, $database);

//if there is some error connecting to the database
//with die we will stop the further execution by displaying a message causing the error 
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

//if everything is fine

//creating an array for storing the data 
$trials = array(); 

//this is our sql query 
// $sql = "SELECT id, eventname, date, club FROM up93k_entryman_trial WHERE `published` = 1 AND `date` < DATE(NOW()) ORDER BY `date` DESC;";
// $sql = "SELECT id, eventname, date, club, location FROM up93k_entryman_trial WHERE `published` = 1 AND `date` > DATE(NOW()) ORDER BY `date` ASC;";

// $sql = "SELECT id, eventname, date, club, numlaps, numsections  FROM up93k_entryman_trial AS t WHERE t.date BETWEEN (NOW() - INTERVAL 90 DAY)  AND (NOW() + INTERVAL 7 DAY)  AND t.published = 1 ORDER BY `date` DESC";

$sql = "SELECT id, eventname, date, club, numlaps, numsections  FROM up93k_entryman_trial AS t WHERE t.isscoringlocked = 0 AND t.published = 1 ORDER BY `date` DESC";
//creating an statment with the query
$stmt = $conn->prepare($sql);

//executing that statment
$stmt->execute();

//binding results for that statment 
$stmt->bind_result($id, $eventname, $date, $club, $numlaps, $numsections);

//looping through all the records
while($stmt->fetch()){
	
	//pushing fetched data in an array 
	$temp = [
		'id'=>$id,
		'date'=>$date,
		'club'=>$club,
		'name'=>$eventname,
		'numlaps'=>$numlaps,
		'numsections'=>$numsections
	];
	
	//pushing the array inside the hero array 
	array_push($trials, $temp);
}

//displaying the data in json format 
echo json_encode($trials);