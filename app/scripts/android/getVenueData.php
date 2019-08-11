<?
//these are the server details
//the username is root by default in case of xampp
//password is nothing by default
//and lastly we have the database named android. if your database name is different you have to change it 
$servername = "localhost";
$username = "monster_android";
$password = "mU@09hQQwkKY";
$database = "monster_joom";
 
$id = $_GET['id'];
//creating a new connection object using mysqli 
$conn = new mysqli($servername, $username, $password, $database);
 
//if there is some error connecting to the database
//with die we will stop the further execution by displaying a message causing the error 
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
 
//if everything is fine
 
//creating an array for storing the data 
$venueDetails = array(); 
 
//this is our sql query 
$sql = "SELECT name, landowner, latitude, longitude, notes, phone, email, directions, address, postcode FROM up93k_entryman_venue WHERE `id` = $id";

//creating an statment with the query
$stmt = $conn->prepare($sql);
 
//executing that statment
$stmt->execute();
 
//binding results for that statment 
$stmt->bind_result($name, $landowner, $latitude, $longitude, $notes, $phone, $email, $directions, $address, $postcode);
 
 //pushing fetched data in an array 

 $temp = [
 'name'=>$name,
 'landowner'=>$landowner,
 'latitude'=>$latitude,
 'longitude'=>$longitude,
 'notes'=>$notes,
 'phone'=>$phone,
 'email'=>$email,
 'address'=>$address,
 'postcode'=>$postcode,
 'directions'=>$directions
 ];
 
 
 //pushing the array inside the hero array 
 array_push($venueDetails, $temp);
 
//displaying the data in json format 
echo json_encode($venueDetails);