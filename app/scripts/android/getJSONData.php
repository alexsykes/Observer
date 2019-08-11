<?
//these are the server details
//the username is root by default in case of xampp
//password is nothing by default
//and lastly we have the database named android. if your database name is different you have to change it 
$servername = "localhost";
$username = "monster_android";
$password = "mU@09hQQwkKY";
$database = "monster_joom";
 
 
$id = $_GET["id"];

//creating a new connection object using mysqli 
$conn = new mysqli($servername, $username, $password, $database);
 
//if there is some error connecting to the database
//with die we will stop the further execution by displaying a message causing the error 
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
 

//this is our sql query 
$sql = "SELECT courselist, numsections, numlaps, club, date, eventname, location FROM up93k_entryman_trial WHERE `id` = '$id'";

$result = $conn->query($sql);

//creating arrays for storing the data 
$trialDetails = array(); 
$trialResults = array();
$courseEntry  = array();
$data = array();

while (  $row = $result->fetch_assoc()) 
{
	$trialDetails['trial details'][] = $row;
	//$trialDetails[] = $row;
	$courselist = $row['courselist'];
}

	$courseArray = explode(',',$courselist);
	$numCourses = sizeof($courseArray);
	$courselist = implode("','",$courseArray);
	
// Get Course numbers

$selectCourseEntryCount = "Select COUNT(DISTINCT `number`) as `count`, `course` AS `coursename` FROM up93k_entryman_entry WHERE `trialid` = '$id' AND number > 0 GROUP BY course ORDER BY FIELD(`course`,'$courselist');";

$courseEntryCount = $conn->query($selectCourseEntryCount);	

while( $row = $courseEntryCount->fetch_assoc())
{
	$courseEntry['entry count'][] = $row;
	//$courseEntry[] = $row;
}
	

$selectResultsQuery = "SELECT DATE_FORMAT(r.created, '%d %M %Y') AS created, e.id AS id, e.number  AS rider, e.course AS course, CONCAT(e.firstname,' ',e.lastname)  AS name, e.class AS class, CONCAT(e.make,' ',e.size) AS machine, r.total, r.cleans, r.ones, r.twos, r.threes, r.fives, r.missed, r.sectionscores as sectionscores, r.scores AS scores, e.trialid FROM up93k_entryman_entry e LEFT JOIN up93k_entryman_result r ON e.id = r.entryid WHERE e.trialid = '$id' ORDER BY FIELD(e.course,'$courselist'),  missed ASC, total, cleans DESC, ones DESC, twos DESC, threes DESC, scores";

$results = $conn->query($selectResultsQuery);	

while($row = $results->fetch_assoc())
{
	$trialResults['results'][] = $row;
	//$trialResults[] = $row;
}

//echo "Results: ".json_encode($trialResults); 

$trial = json_encode($trialDetails);
$results = json_encode($trialResults); 
$data[] = $trialDetails;
$data[] = $courseEntry;
$data[] = $trialResults;

echo json_encode($data);
	
	

