<?php

function main(){
    // waiting for client action
    while(1){
      // show initial options
      $user_input = readline(showOptions(0));

      /* Buy flight ticket */
      if($user_input == "1"){
        $user_input = readline(showOptions(1));
        if($user_input == "0")
          continue;
        buyTicket();
      }

      /* Book accomodation */
      else if($user_input == "2"){
        $user_input = readline(showOptions(2));
        if( $user_input == "0")
          continue;
        // bookRoom();
      }

      /* Buy package */
      else if($user_input == "3"){
        $user_input = readline(showOptions(3));
        if( $user_input == "0")
          continue;
        // buyPackage();
      }
  }
}

function showOptions($option){
    $cover = "\nWhat do you want to do? Press the number matching your choice.\n";
    $options = "  1 - Buy flight.\n  2 - Book hotel.\n  3 - Buy package.\n";
    $options1 = "\n**BUY FLIGHT**\nEnter the flight ID to buy. Or press 0 to go back.\n";
    $options2 = "\n**BOOK HOTEL**\nEnter the accomodation ID to buy. Or press 0 to go back.\n";
    $options3 = "\n**BUY PACKAGE**\nEnter the flight ID and the accomodation ID you want buy. Or press 0 to go back.\n";

    if($option == 0){
      echo($cover);
      echo($options);
    }
    else if($option == 1)
      echo($options1);

    else if($option == 2)
      echo($options2);

    else if($option == 3)
      echo($options3);

    else
      echo ("error in function showOptions"); //test
}

// Buy flight ticket (request information from server)
function buyTicket(){
  $unparsed_json = file_get_contents('http://127.0.0.1:5000/searchFlights');
  $json = json_decode($unparsed_json);
  // prints options on the screen
  foreach ($json as $key => $value){
    echo "\t\t--*--\n";
    // there are no tickets available
    if($key == 'Status'){
      echo "$value\n";
      return;
    }
    else{
      foreach ($value as $k => $v){
        if($k != 'quantity')
          echo "\t\t$k: $v\n";
      };
      echo "\t\t--*--\n\n";
    }
  };
  $id = readline("\nType the ticket id you want to acquire: ");
  $number = readline("\nNumber of tickets: ");

  $request = 'http://127.0.0.1:5000/buyTicket/'.$id.'/'.$number;
  $unparsed_json = file_get_contents($request);
  $json = json_decode($unparsed_json);
  foreach ($json as $key => $value) {echo "$value\n";}
}

// client interface start
main();

?>
