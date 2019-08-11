<?php
//these are the server details
//the username is root by default in case of xampp
//password is nothing by default
//and lastly we have the database named android. if your database name is different you have to change it 
$servername = "trialmonster.uk";
$username = "monster_android";
$password = "mU@09hQQwkKY";
$database = "monster_joom";
 
$trialid = $_GET['id'];
$section = $_GET['section'];
//creating a new connection object using mysqli 
$conn = new mysqli($servername, $username, $password, $database);
 
//if there is some error connecting to the database
//with die we will stop the further execution by displaying a message causing the error 
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

//creating an array for storing the data 
$result = array(); 
 

$sql = "SELECT rider, GROUP_CONCAT(score SEPARATOR '') as scores, SUM(score) AS total, COUNT(score) AS laps  FROM up93k_entryman_score s WHERE s.trialid = $trialid AND s.section = $section GROUP BY rider ORDER BY rider, lap";



//creating an statment with the query
$stmt = $conn->prepare($sql);
 
//executing that statment
$stmt->execute();
 
//binding results for that statment 
$stmt->bind_result($rider, $scores, $laps, $total);


// var_dump($stmt);
//looping through all the records
while($stmt->fetch()){
 //pushing fetched data in an array 
//$details = str_replace( '"', '/"', $details);

//$details = json_encode($details, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE); 

//$details = "Nothing to see here";
 $temp = [
 'rider'=>$rider,
 'laps'=>$laps,
 'scores'=>$scores,
 'total'=>$total
 ];
 
 //pushing the array inside the hero array 
 array_push($result, $temp);
}

//displaying the data in json format 
echo json_encode($result);
