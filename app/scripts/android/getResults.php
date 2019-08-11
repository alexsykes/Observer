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
$results = array(); 
$id = 14;

$sql = "SELECT  e.id AS id, e.number  AS rider, e.course AS course, CONCAT(e.firstname,' ',e.lastname)  AS name, e.class AS class, CONCAT(e.make,' ',e.size) AS machine, r.total AS total, r.cleans AS cleans, r.ones AS ones, r.twos AS twos, r.threes AS threes, r.fives AS fives, r.missed AS missed, r.sectionscores as sectionscores, r.scores AS scores, e.trialid FROM up93k_entryman_entry e LEFT JOIN up93k_entryman_result r ON e.id = r.entryid WHERE e.trialid = '$id' ORDER BY FIELD(e.course,'$courselist'),  missed ASC, total, cleans DESC, ones DESC, twos DESC, threes DESC, scores";

$sql = "SELECT  e.id AS id, e.number  AS rider, e.course AS course, CONCAT(e.firstname,' ',e.lastname)  AS name, e.class AS class, CONCAT(e.make,' ',e.size) AS machine, r.total AS total, r.cleans AS cleans, r.ones AS ones, r.twos AS twos, r.threes AS threes, r.fives AS fives, r.missed AS missed, r.sectionscores as sectionscores, r.scores AS scores, e.trialid FROM up93k_entryman_entry e LEFT JOIN up93k_entryman_result r ON e.id = r.entryid WHERE e.trialid = '$id' ORDER BY missed ASC, total, cleans DESC, ones DESC, twos DESC, threes DESC, scores";

 echo $sql;

//creating an statment with the query
$stmt = $conn->prepare($sql);

//executing that statment
$stmt->execute();

//binding results for that statment 
$stmt->bind_result($id, $rider, $course, $name, $class, $machine, $total, $cleans, $ones, $twos, $threes, $fives, $missed, $sectionscores, $scores);

//looping through all the records
while($stmt->fetch()){
	
	//pushing fetched data in an array 
	$temp = [
		'id'=>$id,
		'rider'=>"bbbb",
		'course'=>$course,
		'name'=>$name,
		'class'=>$class,
		'machine'=>$machine,
		'total'=>$total,
		'cleans'=>$cleans,
		'ones'=>$ones,
		'twos'=>$twos,
		'threes'=>$threes,
		'fives'=>$fives,
		'missed'=>$missed,
		'sectionscores'=>$sectionscores,
		'scores'=>$scores
	];
	
	//pushing the array inside the hero array 
	array_push($results, $temp);
}

//displaying the data in json format 
echo json_encode($results);