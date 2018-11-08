<?php
include('./httpful.phar');
$url = "https://0.0.0.0:5000/searchFlights";
$response = \Httpful\Request::get($url)->send();
echo "{$response->body}"
 ?>
