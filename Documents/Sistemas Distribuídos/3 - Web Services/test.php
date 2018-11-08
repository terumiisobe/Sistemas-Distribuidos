<?php
$unparsed_json = file_get_contents('http://127.0.0.1:5000/searchFlights');
$json = json_decode($unparsed_json);
foreach ($json as $key => $value){
  foreach ($value as $k => $v){
          echo "$k: $v\n";
      };
    };
?>
